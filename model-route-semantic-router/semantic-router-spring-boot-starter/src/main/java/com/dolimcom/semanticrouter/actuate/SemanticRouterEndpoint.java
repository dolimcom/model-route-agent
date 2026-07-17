package com.dolimcom.semanticrouter.actuate;

import com.dolimcom.semanticrouter.core.RouteSnapshotManager;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.Map;

@Endpoint(id = "semanticrouter")
public class SemanticRouterEndpoint {

    private final RouteSnapshotManager snapshotManager;

    public SemanticRouterEndpoint(RouteSnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;
    }

    @ReadOperation
    public Map<String, Object> summary() {
        RouteSnapshot snapshot = snapshotManager.currentSnapshot();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("configVersion", snapshot.configVersion());
        payload.put("datasetVersion", snapshot.datasetVersion());
        payload.put("encoderVersion", snapshot.encoderVersion());
        payload.put("loadedAt", snapshot.loadedAt());
        payload.put("routeCount", snapshot.routes().size());
        payload.put("routeIds", snapshot.routes().keySet());
        payload.put("lastReloadError", snapshotManager.lastReloadError());
        return payload;
    }
}
