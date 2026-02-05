package com.fiap.sus.network.core.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RestTemplateConfigTest {

    @Test
    void testBeans() {
        RestTemplateConfig config = new RestTemplateConfig();
        assertNotNull(config.restTemplate());
        assertNotNull(config.objectMapper());
    }
}
