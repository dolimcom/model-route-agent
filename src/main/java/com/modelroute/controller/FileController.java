package com.modelroute.controller;

import com.modelroute.dto.FileContentResponse;
import com.modelroute.dto.FileEntryResponse;
import com.modelroute.dto.FileRootResponse;
import com.modelroute.service.FileAccessService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileAccessService fileAccessService;

    public FileController(FileAccessService fileAccessService) {
        this.fileAccessService = fileAccessService;
    }

    @GetMapping("/roots")
    public List<FileRootResponse> roots() {
        return fileAccessService.listRoots();
    }

    @GetMapping("/{rootId}/entries")
    public List<FileEntryResponse> list(
            @PathVariable String rootId,
            @RequestParam(defaultValue = "") String path) {
        return fileAccessService.list(rootId, path);
    }

    @GetMapping("/{rootId}/content")
    public FileContentResponse read(
            @PathVariable String rootId,
            @RequestParam String path) {
        return fileAccessService.read(rootId, path);
    }

}
