package com.modelroute.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "model-route.files")
public class FileAccessProperties {

    @NotEmpty(message = "model-route.files.allowed-roots must contain at least one root")
    @Valid
    private List<AllowedRoot> allowedRoots = new ArrayList<>();

    private long maxReadBytes = 1024 * 1024;
    private long maxWriteBytes = 1024 * 1024;

    public List<AllowedRoot> getAllowedRoots() {
        return allowedRoots;
    }

    public void setAllowedRoots(List<AllowedRoot> allowedRoots) {
        this.allowedRoots = allowedRoots;
    }

    public long getMaxReadBytes() {
        return maxReadBytes;
    }

    public void setMaxReadBytes(long maxReadBytes) {
        this.maxReadBytes = maxReadBytes;
    }

    public long getMaxWriteBytes() {
        return maxWriteBytes;
    }

    public void setMaxWriteBytes(long maxWriteBytes) {
        this.maxWriteBytes = maxWriteBytes;
    }

    public static class AllowedRoot {

        @NotBlank(message = "file root id must not be blank")
        private String id;

        @NotBlank(message = "file root path must not be blank")
        private String path;

        private boolean enabled = true;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
