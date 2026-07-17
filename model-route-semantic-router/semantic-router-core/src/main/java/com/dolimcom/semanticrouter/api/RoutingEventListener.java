package com.dolimcom.semanticrouter.api;

import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.model.SnapshotReloadResult;

public interface RoutingEventListener {

    default void onDecision(RoutingRequest request, RoutingResult result) {
    }

    default void onReload(SnapshotReloadResult result) {
    }
}
