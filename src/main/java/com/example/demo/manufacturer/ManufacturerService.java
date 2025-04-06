package com.example.demo.manufacturer;

import java.util.List;

public interface ManufacturerService {

    Manufacturer findById(Long id);

    List<ManufacturerDto> findAll();
}
