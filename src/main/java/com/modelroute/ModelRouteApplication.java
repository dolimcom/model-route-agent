package com.modelroute;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.config.FileAccessProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ModelRouteProperties.class, FileAccessProperties.class})
public class ModelRouteApplication {
/*老吴老吴老吴老吴*/
    public static void main(String[] args) {
        SpringApplication.run(ModelRouteApplication.class, args);
    }
}
