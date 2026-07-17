package com.modelroute.controller;

import com.modelroute.domain.TaskType;
import com.modelroute.dto.ModelCatalogEntry;
import com.modelroute.dto.ModelConfigurationStatusResponse;
import com.modelroute.dto.TaskModelConfigRequest;
import com.modelroute.dto.TaskModelConfigResponse;
import com.modelroute.service.ModelCatalogService;
import com.modelroute.service.RuntimeModelConfigurationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings/models")
public class SettingsController {

    private final RuntimeModelConfigurationService configurationService;
    private final ModelCatalogService catalogService;

    public SettingsController(
            RuntimeModelConfigurationService configurationService,
            ModelCatalogService catalogService) {
        this.configurationService = configurationService;
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<TaskModelConfigResponse> list() {
        return configurationService.list();
    }

    @GetMapping("/catalog")
    public List<ModelCatalogEntry> catalog() {
        return catalogService.list();
    }

    @GetMapping("/status")
    public ModelConfigurationStatusResponse status() {
        return configurationService.status();
    }

    @PutMapping("/{taskType}")
    public TaskModelConfigResponse update(
            @PathVariable TaskType taskType,
            @Valid @RequestBody TaskModelConfigRequest request) {
        return configurationService.update(taskType, request);
    }
}
