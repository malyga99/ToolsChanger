package com.example.demo.manufacturer;

import com.example.demo.exception.ManufacturerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerMapper manufacturerMapper;
    private final static Logger LOGGER = LoggerFactory.getLogger(ManufacturerServiceImpl.class);

    @Override
    public List<ManufacturerDto> findAll() {
        LOGGER.debug("findAll: Fetching manufacturers");
        List<Manufacturer> manufacturers = manufacturerRepository.findAll();
        LOGGER.debug("findAll: Fetched {} manufacturers", manufacturers.size());
        return manufacturers.stream()
                .map(manufacturerMapper::toDto)
                .toList();

    }

    @Override
    public Manufacturer findById(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new ManufacturerNotFoundException("Manufacturer with id: " + id + " not found"));
        LOGGER.debug("findById: Fetched manufacturer with id: {}", id);
        return manufacturer;
    }
}
