package com.dolimcom.semanticrouter.api;

import com.dolimcom.semanticrouter.model.RouteCorpus;

public interface RouteDefinitionProvider {

    RouteCorpus load();

    String description();
}
