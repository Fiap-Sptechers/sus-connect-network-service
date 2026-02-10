package com.fiap.sus.network.modules.health_unit.mapper;

import com.fiap.sus.network.modules.health_unit.dto.HealthUnitResponse;
import com.fiap.sus.network.modules.health_unit.dto.ContactResponse;
import com.fiap.sus.network.modules.health_unit.entity.Contact;
import com.fiap.sus.network.modules.health_unit.entity.HealthUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.List;

import com.fiap.sus.network.modules.health_unit.dto.HealthUnitRequest;

@Component
@RequiredArgsConstructor
public class HealthUnitMapper {
    
    private final AddressMapper addressMapper;

    public HealthUnitResponse toDto(HealthUnit entity) {
        return toDto(entity, null);
    }

    public HealthUnitResponse toDto(HealthUnit entity, String distance) {
        if (entity == null) return null;
        
        List<ContactResponse> contacts = java.util.Collections.emptyList();
        try {
            java.util.Collection<Contact> contactsCollection = entity.getContacts();
            if (contactsCollection != null && !contactsCollection.isEmpty()) {
                Contact[] contactsArray = contactsCollection.toArray(new Contact[0]);
                
                contacts = java.util.Arrays.stream(contactsArray)
                    .filter(c -> c != null && !c.isDeleted())
                    .map(c -> new ContactResponse(c.getId(), c.getType(), c.getValue(), c.getDescription()))
                    .collect(Collectors.toList());
            }
        } catch (java.util.ConcurrentModificationException e) {
            try {
                if (entity.getContacts() != null) {
                    List<Contact> contactsCopy = new java.util.ArrayList<>(entity.getContacts());
                    contacts = contactsCopy.stream()
                        .filter(c -> c != null && !c.isDeleted())
                        .map(c -> new ContactResponse(c.getId(), c.getType(), c.getValue(), c.getDescription()))
                        .collect(Collectors.toList());
                }
            } catch (Exception e2) {
                contacts = java.util.Collections.emptyList();
            }
        } catch (Exception e) {
            contacts = java.util.Collections.emptyList();
        }
        
        return new HealthUnitResponse(
            entity.getId(), 
            entity.getName(), 
            entity.getCnpj(),
            addressMapper.toDto(entity.getAddress()),
            contacts,
            distance
        );
    }

    public HealthUnit toEntity(HealthUnitRequest request) {
        if (request == null) return null;
        HealthUnit entity = new HealthUnit();
        entity.setName(request.name());
        entity.setCnpj(request.cnpj());
        entity.setAddress(addressMapper.toEntity(request.address()));
        
        if (request.contacts() != null) {
            List<Contact> contacts = request.contacts().stream()
                .map(cReq -> {
                    Contact c = new Contact();
                    c.setType(cReq.type());
                    c.setValue(cReq.value());
                    c.setDescription(cReq.description());
                    c.setUnit(entity);
                    return c;
                })
                .collect(Collectors.toList());
            entity.setContacts(contacts);
        }
        
        return entity;
    }
}
