package com.modelroute;

import com.modelroute.config.ModelRouteProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ModelRouteProperties.class)
public class ModelRouteApplication {
/*老吴老吴老吴老吴*/
    public static void main(String[] args) {
        SpringApplication.run(ModelRouteApplication.class, args);
    }
}
