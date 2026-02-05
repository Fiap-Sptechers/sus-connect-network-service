package com.fiap.sus.network;

import com.fiap.sus.network.core.config.AppConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
@SpringBootApplication
@EnableConfigurationProperties(AppConfigProperties.class)
public class SusConnectNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SusConnectNetworkApplication.class, args);
    }

}
