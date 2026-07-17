package com.modelroute.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "model-route.runtime")
public class RuntimeConfigProperties {

    private boolean enabled = true;
    private String modelsFile = "./config/models.local.yml";
    private String workspacesFile = "./config/workspaces.local.yml";
    private String chooserStateFile = "./config/chooser-state.local.yml";
    private long maxAttachmentBytes = 1024 * 1024;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModelsFile() {
        return modelsFile;
    }

    public void setModelsFile(String modelsFile) {
        this.modelsFile = modelsFile;
    }

    public String getWorkspacesFile() {
        return workspacesFile;
    }

    public void setWorkspacesFile(String workspacesFile) {
        this.workspacesFile = workspacesFile;
    }

    public String getChooserStateFile() {
        return chooserStateFile;
    }

    public void setChooserStateFile(String chooserStateFile) {
        this.chooserStateFile = chooserStateFile;
    }

    public long getMaxAttachmentBytes() {
        return maxAttachmentBytes;
    }

    public void setMaxAttachmentBytes(long maxAttachmentBytes) {
        this.maxAttachmentBytes = maxAttachmentBytes;
    }
}
