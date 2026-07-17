package com.dolimcom.semanticrouter.encoder;

import java.util.List;

public interface LocalModelDiscoveryClient {

    List<LocalModelDescriptor> discover();
}
