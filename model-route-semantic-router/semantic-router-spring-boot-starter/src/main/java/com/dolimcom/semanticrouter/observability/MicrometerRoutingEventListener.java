package com.dolimcom.semanticrouter.observability;

import com.dolimcom.semanticrouter.api.RoutingEventListener;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.model.SnapshotReloadResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MicrometerRoutingEventListener implements RoutingEventListener {

    private final MeterRegistry meterRegistry;

    public MicrometerRoutingEventListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void onDecision(RoutingRequest request, RoutingResult result) {
        Counter.builder("semantic.router.decisions")
                .tag("status", result.status().name())
                .tag("reason", result.trace().reasonCode().name())
                .tag("route", result.routeId() == null ? "__none__" : result.routeId())
                .register(meterRegistry)
                .increment();

        Timer.builder("semantic.router.latency")
                .tag("status", result.status().name())
                .register(meterRegistry)
                .record(java.time.Duration.ofMillis(result.trace().timings().totalMs()));
    }

    @Override
    public void onReload(SnapshotReloadResult result) {
        Counter.builder("semantic.router.reload")
                .tag("success", Boolean.toString(result.success()))
                .register(meterRegistry)
                .increment();
    }
}
