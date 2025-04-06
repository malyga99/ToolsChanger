package com.example.demo.tool;

import com.example.demo.exception.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/v1/tools")
@RequiredArgsConstructor
@Tag(
        name = "Tool controller",
        description = "Controller for working with tools"
)
public class ToolController {

    private final ToolService toolService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolController.class);

    @Operation(
            summary = "Find all tools",
            description = "Searches all tools with pagination"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All tools successfully received"),
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
    public ResponseEntity<Page<ToolDto>> findAll(
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize
    ) {
        LOGGER.info("[GET] Request for find all tools - pageNumber: {}, pageSize: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(toolService.findAll(pageable));
    }

    @Operation(
            summary = "Find all tools of the current user",
            description = "Searches all tools of the current user with pagination"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All tools successfully received"),
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
    @GetMapping("/my")
    public ResponseEntity<Page<ToolDto>> findMy(
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize
    ) {
        LOGGER.info("[GET] Request for find all tools of the current user - pageNumber: {}, pageSize: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(toolService.findMy(pageable));
    }

    @Operation(
            summary = "Find tool by ID"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Tool successfully received"),
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
    @GetMapping("/{id}")
    public ResponseEntity<ToolDto> findById(
            @PathVariable("id") @Parameter(description = "Tool ID", example = "1", required = true) Long id
    ) {
        LOGGER.info("[GET] request for find tool by ID: {}", id);
        return ResponseEntity.ok(toolService.findById(id));

    }

    @Operation(
            summary = "Search tools by criteria",
            description = "Searches all tools by provided filters with pagination"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All tools successfully received"),
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
    @GetMapping("/search")
    public ResponseEntity<Page<ToolDto>> search(
            @RequestParam(value = "description", required = false) @Parameter(description = "Tool description", example = "Description") String description,
            @RequestParam(value = "manufacturer", required = false) @Parameter(description = "Manufacturer id", example = "1") Long manufacturer,
            @RequestParam(value = "category", required = false) @Parameter(description = "Category id", example = "1") Long category,
            @RequestParam(value = "type", required = false) @Parameter(description = "Tool type", example = "EXCHANGE") String type,
            @RequestParam(value = "condition", required = false) @Parameter(description = "Tool condition", example = "RENT") String condition,
            @RequestParam(value = "gte", required = false) @Parameter(description = "Minimum price", example = "1000") BigDecimal gte,
            @RequestParam(value = "lte", required = false) @Parameter(description = "Maximum price", example = "5000") BigDecimal lte,
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize
    ) throws IOException {
        LOGGER.info("[GET] Request for search tools with filters - description: {}, manufacturer: {}, category: {}, type: {}, condition: {}, price range: {} - {}. pageNumber: {}, pageSize: {}",
                description, manufacturer, category, type, condition, gte, lte, pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(toolService.search(description, manufacturer, category, type, condition, gte, lte, pageable));

    }

    @Operation(
            summary = "Creates a new tool",
            description = "Creates a new tool with the provided data and uploads the provided files"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Tool created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ToolDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error or invalid files",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Manufacturer or Category not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
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
    @PostMapping
    public ResponseEntity<ToolDto> create(
            @RequestPart("tool") @Valid @Parameter(description = "Data for adding a tool", required = true) ToolCreateUpdateDto toolCreateUpdateDto,
            @RequestPart("files") @Parameter(description = "List of files to be uploaded", required = true) List<MultipartFile> files
    ) {
        LOGGER.info("[POST] Request for create tool");
        ToolDto createdTool = toolService.create(toolCreateUpdateDto, files);
        return ResponseEntity.created(URI.create("/api/v1/tools/" + createdTool.getId())).body(createdTool);
    }

    @Operation(
            summary = "Updates a new tool",
            description = "Updates a new tool with the provided data, uploads and deletes the provided files"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Tool updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ToolDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error or invalid files",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Tool, Manufacturer or Category not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
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
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable("id") @Parameter(description = "Tool ID", example = "1", required = true) Long id,
            @RequestPart("tool") @Valid @Parameter(description = "Data for updating a tool", required = true) ToolCreateUpdateDto toolCreateUpdateDto,
            @RequestPart(value = "files") @Parameter(description = "List of files to be uploaded", required = true) List<MultipartFile> files,
            @RequestPart(value = "filesToDelete") @Parameter(description = "List of files to be deleted", required = true) List<String> filesToDelete
    ) {
        LOGGER.debug("[PUT] Request for update tool by ID: {}", id);
        toolService.update(id, toolCreateUpdateDto, files, filesToDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Deletes tool",
            description = "Deletes a tool by ID. Only the owner of the tool can delete it"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Tool deleted successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ToolDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Tool not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Authorization error or user is not the owner of the tool",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    )
            }
    )

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") @Parameter(description = "Tool ID", example = "1", required = true) Long id
    ) {
        LOGGER.debug("[DELETE] Request for delete tool by ID: {}", id);
        toolService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
