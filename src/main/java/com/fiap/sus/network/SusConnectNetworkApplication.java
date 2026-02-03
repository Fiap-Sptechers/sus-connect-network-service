package com.fiap.sus.network;

import com.fiap.sus.network.core.config.AppConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableConfigurationProperties(AppConfigProperties.class)
@EnableJpaAuditing
public class SusConnectNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SusConnectNetworkApplication.class, args);
    }

}
