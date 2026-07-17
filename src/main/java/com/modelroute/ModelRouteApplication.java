package com.modelroute;

import com.modelroute.config.ModelRouteProperties;
import com.modelroute.config.FileAccessProperties;
import com.modelroute.config.RuntimeConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        ModelRouteProperties.class,
        FileAccessProperties.class,
        RuntimeConfigProperties.class
})
public class ModelRouteApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication application = new SpringApplication(ModelRouteApplication.class);
        application.setHeadless(false);
        application.run(args);
    }
}
