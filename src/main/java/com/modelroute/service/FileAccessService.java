package com.modelroute.service;

import com.modelroute.config.FileAccessProperties;
import com.modelroute.config.FileAccessProperties.AllowedRoot;
import com.modelroute.dto.FileContentResponse;
import com.modelroute.dto.FileEntryResponse;
import com.modelroute.dto.FileOperationResponse;
import com.modelroute.dto.FileRootResponse;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileAccessService {

    private final WorkspaceRegistry workspaceRegistry;
    private final long maxReadBytes;
    private final long maxWriteBytes;

    public FileAccessService(FileAccessProperties properties, WorkspaceRegistry workspaceRegistry) {
        this.workspaceRegistry = workspaceRegistry;
        this.maxReadBytes = properties.getMaxReadBytes();
        this.maxWriteBytes = properties.getMaxWriteBytes();
        if (maxReadBytes < 1) {
            throw new IllegalStateException("model-route.files.max-read-bytes must be positive");
        }
        if (maxWriteBytes < 1) {
            throw new IllegalStateException("model-route.files.max-write-bytes must be positive");
        }
    }

    public List<FileRootResponse> listRoots() {
        return workspaceRegistry.list();
    }

    public List<FileEntryResponse> list(String rootId, String relativePath) {
        if (workspaceRegistry.isSingleFileRoot(rootId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Directory listing is disabled for a selected file");
        }
        Path directory = resolveExistingPath(rootId, relativePath);
        if (!Files.isDirectory(directory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path is not a directory");
        }

        try (Stream<Path> children = Files.list(directory)) {
            return children
                    .map(path -> toEntry(rootId, path))
                    .sorted(Comparator.comparing(FileEntryResponse::relativePath))
                    .toList();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to list directory", exception);
        }
    }

    public FileContentResponse read(String rootId, String relativePath) {
        Path file = resolveExistingPath(rootId, relativePath);
        if (!Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path is not a regular file");
        }

        try {
            long size = Files.size(file);
            if (size > maxReadBytes) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "File exceeds configured read limit of " + maxReadBytes + " bytes");
            }
            return new FileContentResponse(rootId, relativePathOrRoot(rootId, file), size,
                    Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file", exception);
        }
    }

    public FileSnapshot snapshot(String rootId, String relativePath) {
        Path target = resolveExistingPath(rootId, relativePath);
        if (Files.isDirectory(target)) {
            return new FileSnapshot(true, null, "directory");
        }
        FileContentResponse content = read(rootId, relativePath);
        return new FileSnapshot(false, content.content(), sha256(content.content()));
    }

    public FileOperationResponse createDirectory(String rootId, String relativePath) {
        Path directory = resolveNewPath(rootId, relativePath);
        try {
            Files.createDirectory(directory);
            return operation("CREATE_DIRECTORY", rootId, directory, null);
        } catch (FileAlreadyExistsException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Target path already exists", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create directory", exception);
        }
    }

    public FileOperationResponse createFile(String rootId, String relativePath, String content) {
        validateWriteSize(content);
        Path file = resolveNewPath(rootId, relativePath);
        try {
            Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            return operation("CREATE_FILE", rootId, file, null);
        } catch (FileAlreadyExistsException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Target path already exists", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create file", exception);
        }
    }

    public FileOperationResponse updateFile(String rootId, String relativePath, String content) {
        validateWriteSize(content);
        Path file = resolveExistingPath(rootId, relativePath);
        if (!Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path is not a regular file");
        }

        Path temporaryFile = null;
        try {
            temporaryFile = Files.createTempFile(file.getParent(), ".model-route-", ".tmp");
            Files.writeString(temporaryFile, content, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);
            replaceAtomically(temporaryFile, file);
            temporaryFile = null;
            return operation("UPDATE_FILE", rootId, file, null);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update file", exception);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // The temporary file is best-effort cleanup after a failed write.
                }
            }
        }
    }

    public FileOperationResponse rename(String rootId, String sourcePath, String targetPath) {
        Path source = resolveExistingPath(rootId, sourcePath);
        Path target = resolveNewPath(rootId, targetPath);
        if (source.equals(realRoot(rootId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The allowed root cannot be renamed");
        }

        try {
            Files.move(source, target);
            return operation("RENAME", rootId, target, relativePathOrRoot(rootId, source));
        } catch (FileAlreadyExistsException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Target path already exists", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to rename path", exception);
        }
    }

    public FileOperationResponse delete(String rootId, String relativePath) {
        Path target = resolveExistingPath(rootId, relativePath);
        if (target.equals(realRoot(rootId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The allowed root cannot be deleted");
        }

        String deletedPath = relativePathOrRoot(rootId, target);
        try {
            Files.delete(target);
            return new FileOperationResponse("DELETE", rootId, deletedPath, null, Instant.now());
        } catch (DirectoryNotEmptyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Directory is not empty; recursive deletion is disabled", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete path", exception);
        }
    }

    private FileEntryResponse toEntry(String rootId, Path path) {
        try {
            return new FileEntryResponse(
                    path.getFileName().toString(),
                    relativePathOrRoot(rootId, path),
                    Files.isDirectory(path),
                    Files.isDirectory(path) ? 0 : Files.size(path),
                    Files.getLastModifiedTime(path).toInstant());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to inspect file entry", exception);
        }
    }

    private Path resolveExistingPath(String rootId, String relativePath) {
        Path root = realRoot(rootId);
        String requestedPath = relativePath == null ? "" : relativePath;
        Path candidate = resolveRelative(root, requestedPath);
        if (!candidate.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Path is outside the allowed root");
        }
        workspaceRegistry.assertPathAllowed(rootId, candidate);
        if (!Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File or directory not found");
        }

        try {
            Path realCandidate = candidate.toRealPath();
            if (!realCandidate.startsWith(root)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Path is outside the allowed root");
            }
            workspaceRegistry.assertPathAllowed(rootId, realCandidate);
            return realCandidate;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to resolve file path", exception);
        }
    }

    private Path resolveNewPath(String rootId, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path must not be blank");
        }

        Path root = realRoot(rootId);
        Path candidate = resolveRelative(root, relativePath);
        if (!candidate.startsWith(root) || candidate.equals(root)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Path is outside the allowed root");
        }
        workspaceRegistry.assertPathAllowed(rootId, candidate);
        if (Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Target path already exists");
        }

        Path parent = candidate.getParent();
        if (parent == null || !Files.isDirectory(parent)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target parent directory not found");
        }
        try {
            Path realParent = parent.toRealPath();
            if (!realParent.startsWith(root)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Path is outside the allowed root");
            }
            return realParent.resolve(candidate.getFileName());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Unable to resolve target parent directory", exception);
        }
    }

    private AllowedRoot root(String rootId) {
        return workspaceRegistry.required(rootId);
    }

    private Path configuredRootPath(AllowedRoot root) {
        Path path = Path.of(root.getPath()).toAbsolutePath().normalize();
        if (!Files.isDirectory(path)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Configured file root is not a directory: " + root.getId());
        }
        return path;
    }

    private Path realRoot(String rootId) {
        try {
            return configuredRootPath(root(rootId)).toRealPath();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to resolve configured file root: " + rootId, exception);
        }
    }

    private Path resolveRelative(Path root, String relativePath) {
        try {
            Path requested = Path.of(relativePath);
            if (requested.isAbsolute()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Absolute paths are not allowed");
            }
            return root.resolve(requested).normalize();
        } catch (InvalidPathException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path", exception);
        }
    }

    private void validateWriteSize(String content) {
        long size = content.getBytes(StandardCharsets.UTF_8).length;
        if (size > maxWriteBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Content exceeds configured write limit of " + maxWriteBytes + " bytes");
        }
    }

    private String sha256(String content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private void replaceAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private FileOperationResponse operation(String operation, String rootId, Path path, String previousPath) {
        return new FileOperationResponse(
                operation,
                rootId,
                relativePathOrRoot(rootId, path),
                previousPath,
                Instant.now());
    }

    private String relativePathOrRoot(String rootId, Path path) {
        Path root = realRoot(rootId);
        Path relative = root.relativize(path);
        return relative.toString().replace('\\', '/');
    }
}
