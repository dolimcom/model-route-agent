package com.dolimcom.semanticrouter.policy;

import com.dolimcom.semanticrouter.model.ReasonCode;
import com.dolimcom.semanticrouter.model.RouteScore;
import com.dolimcom.semanticrouter.model.RouteSnapshot;
import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;
import com.dolimcom.semanticrouter.model.RoutingStatus;

import java.util.List;
import java.util.Optional;

public interface RoutingPolicy {

    Optional<String> resolveStaticOverride(RouteSnapshot snapshot, RoutingRequest request);

    RoutingResult decide(RouteSnapshot snapshot, RoutingRequest request, List<RouteScore> candidates, String inputHash, String inputPreview, long encodeMs, long searchMs);

    RoutingResult handleFailure(RouteSnapshot snapshot, RoutingRequest request, ReasonCode reasonCode, String inputHash, String inputPreview, long encodeMs, long searchMs);

    RoutingResult override(RouteSnapshot snapshot, RoutingRequest request, String routeId, ReasonCode reasonCode, String inputHash, String inputPreview);

    RoutingStatus statusForReason(ReasonCode reasonCode);
}
