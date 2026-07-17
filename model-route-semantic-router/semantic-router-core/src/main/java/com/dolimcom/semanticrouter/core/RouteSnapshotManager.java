package com.dolimcom.semanticrouter.core;

import com.dolimcom.semanticrouter.api.RouteDefinitionProvider;
import com.dolimcom.semanticrouter.api.RoutingEventListener;
import com.dolimcom.semanticrouter.api.SemanticEncoder;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.model.SnapshotReloadResult;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RouteSnapshotManager {

    private final RouteDefinitionProvider provider;
    private final SemanticEncoder encoder;
    private final RouteSnapshotFactory snapshotFactory;
    private final List<RoutingEventListener> listeners;
    private final AtomicReference<RouteSnapshot> currentSnapshot = new AtomicReference<>();
    private final AtomicReference<RouteSnapshot> lastKnownGoodSnapshot = new AtomicReference<>();
    private final AtomicReference<String> lastReloadError = new AtomicReference<>();

    public RouteSnapshotManager(RouteDefinitionProvider provider, SemanticEncoder encoder, RouteSnapshotFactory snapshotFactory, List<RoutingEventListener> listeners) {
        this.provider = provider;
        this.encoder = encoder;
        this.snapshotFactory = snapshotFactory;
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
        reloadOrThrow();
    }

    public RouteSnapshot currentSnapshot() {
        return currentSnapshot.get();
    }

    public RouteSnapshot lastKnownGoodSnapshot() {
        return lastKnownGoodSnapshot.get();
    }

    public String lastReloadError() {
        return lastReloadError.get();
    }

    public SnapshotReloadResult reload() {
        try {
            RouteSnapshot snapshot = snapshotFactory.build(provider.load(), encoder);
            currentSnapshot.set(snapshot);
            lastKnownGoodSnapshot.set(snapshot);
            lastReloadError.set(null);
            SnapshotReloadResult result = new SnapshotReloadResult(true, snapshot.configVersion(), snapshot.loadedAt(), "reloaded");
            listeners.forEach(listener -> listener.onReload(result));
            return result;
        } catch (RuntimeException ex) {
            lastReloadError.set(ex.getMessage());
            SnapshotReloadResult result = new SnapshotReloadResult(false, currentSnapshot.get() == null ? null : currentSnapshot.get().configVersion(), Instant.now(), ex.getMessage());
            listeners.forEach(listener -> listener.onReload(result));
            return result;
        }
    }

    private void reloadOrThrow() {
        SnapshotReloadResult result = reload();
        if (!result.success()) {
            throw new IllegalStateException("Unable to initialize route snapshot: " + result.message());
        }
    }
}
