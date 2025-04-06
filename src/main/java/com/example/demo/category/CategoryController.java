package com.example.demo.category;


import com.example.demo.exception.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(
        name = "Category controller",
        description = "Controller for working with categories"
)
public class CategoryController {

    private final CategoryService categoryService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

    @Operation(
            summary = "Find all categories",
            description = "Searches all categories"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All categories successfully received"),
                    @ApiResponse(responseCode = "401", description = "Authentication error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Authorization error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    )

            }
    )
    @GetMapping
    public ResponseEntity<List<CategoryDto>> findAll() {
        LOGGER.info("[GET] Request for find all categories");
        return ResponseEntity.ok(categoryService.findAll());
    }
}
