package com.example.demo.elasticsearch;

import java.math.BigDecimal;
import java.util.List;

public interface ElasticService {

    void save(ToolDocument toolDocument);

    void delete(Long toolId);

    List<Long> search(String description, Long manufacturer, Long category, String type, String condition, BigDecimal gte, BigDecimal lte);
}
