package com.example.demo.manufacturer;

import org.springframework.stereotype.Component;

@Component
public class ManufacturerMapper {

    public ManufacturerDto toDto(Manufacturer manufacturer) {
        return ManufacturerDto.builder()
                .id(manufacturer.getId())
                .name(manufacturer.getName())
                .build();
    }
}
