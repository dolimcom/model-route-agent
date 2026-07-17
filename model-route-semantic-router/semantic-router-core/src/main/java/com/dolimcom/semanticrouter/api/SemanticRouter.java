package com.dolimcom.semanticrouter.api;

import com.dolimcom.semanticrouter.model.RoutingRequest;
import com.dolimcom.semanticrouter.model.RoutingResult;

public interface SemanticRouter {

    RoutingResult route(RoutingRequest request);
}
