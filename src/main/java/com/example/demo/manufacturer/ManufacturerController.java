package com.example.demo.manufacturer;

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
@RequestMapping("/api/v1/manufacturers")
@RequiredArgsConstructor
@Tag(
        name = "Manufacturer controller",
        description = "Controller for working with manufacturers"
)
public class ManufacturerController {

    private final ManufacturerService manufacturerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ManufacturerController.class);

    @Operation(
            summary = "Find all manufacturers",
            description = "Searches all manufacturers"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All manufacturers successfully received"),
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
    public ResponseEntity<List<ManufacturerDto>> findAll() {
        LOGGER.info("[GET] Request for find all manufacturers");
        return ResponseEntity.ok(manufacturerService.findAll());
    }
}
