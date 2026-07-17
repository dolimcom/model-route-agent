package com.dolimcom.semanticrouter.api;

import java.util.List;

public interface SemanticEncoder {

    List<double[]> encodeAll(List<String> texts);

    default double[] encode(String text) {
        return encodeAll(List.of(text)).get(0);
    }

    String version();
}
