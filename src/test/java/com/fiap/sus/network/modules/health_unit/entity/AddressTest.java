package com.fiap.sus.network.modules.health_unit.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void testGettersAndSetters() {
        Address address = new Address();
        address.setStreet("Street");
        address.setNumber("123");
        address.setComplement("Comp");
        address.setNeighborhood("Neigh");
        address.setCity("City");
        address.setState("ST");
        address.setZipCode("12345");
        address.setLatitude(-23.5);
        address.setLongitude(-46.6);

        assertEquals("Street", address.getStreet());
        assertEquals("123", address.getNumber());
        assertEquals("Comp", address.getComplement());
        assertEquals("Neigh", address.getNeighborhood());
        assertEquals("City", address.getCity());
        assertEquals("ST", address.getState());
        assertEquals("12345", address.getZipCode());
        assertEquals(-23.5, address.getLatitude());
        assertEquals(-46.6, address.getLongitude());
    }
}
