package com.fiap.sus.network.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppConfigProperties {
    
    private Geocoding geocoding = new Geocoding();
    
    @Getter
    @Setter
    public static class Geocoding {
        private String url;
    }
}
