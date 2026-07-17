package com.dolimcom.semanticrouter.encoder;

public record LocalModelDescriptor(
        String provider,
        String baseUrl,
        String modelName,
        boolean supportsEmbeddings
) {
}
