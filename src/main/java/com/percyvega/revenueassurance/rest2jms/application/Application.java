package com.percyvega.revenueassurance.rest2jms.application;

import com.percyvega.revenueassurance.rest2jms.model.Carrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;

@Configuration
@EnableAutoConfiguration
@ComponentScan("com.percyvega.revenueassurance.rest2jms")
@PropertySource({"application.properties", "sensitive.properties"})
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.debug("Starting main(" + Arrays.toString(args) + ")");

        SpringApplication.run(Application.class, args);

        for (Carrier carrier : Carrier.values()) {
            new CarrierPickUpThread(carrier).start();
        }
    }
}
