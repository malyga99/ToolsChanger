package com.example.demo.deal;

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

import java.net.URI;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
@Tag(
        name = "Deal controller",
        description = "Controller for working with deal requests"
)
public class DealController {

    private final DealService dealService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DealController.class);

    @Operation(
            summary = "Rental request",
            description = "Sends a rental request to the owner of the tool"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Rental request successfully sent",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DealDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
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
                    @ApiResponse(responseCode = "403", description = "Authorization error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    )

            }
    )
    @PostMapping("/rental")
    public ResponseEntity<DealDto> rent(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rental request details"
            ) RentalRequest rentalRequest
    ) {
        LOGGER.info("[POST] Request for rent tool with id: {}", rentalRequest.getToolId());
        DealDto createdDeal = dealService.rent(rentalRequest);

        return ResponseEntity.created(URI.create("/api/v1/deals/" + createdDeal.getId())).body(createdDeal);
    }

    @Operation(
            summary = "Purchase request",
            description = "Sends a purchase request to the owner of the tool"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Purchase request successfully sent",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DealDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
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
                    @ApiResponse(responseCode = "403", description = "Authorization error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    )

            }
    )
    @PostMapping("/purchase")
    public ResponseEntity<DealDto> purchase(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Purchase request details"
            ) PurchaseRequest purchaseRequest
    ) {
        LOGGER.info("[POST] Request for purchase tool with id: {}", purchaseRequest.getToolId());
        DealDto createdDeal = dealService.purchase(purchaseRequest);

        return ResponseEntity.created(URI.create("/api/v1/deals/" + createdDeal.getId())).body(createdDeal);
    }

    @Operation(
            summary = "Find all deal requests to current user",
            description = "Searches all deal requests sent to the current user with pagination"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All deal requests successfully received"),
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
    public ResponseEntity<Page<DealDto>> findRequestsSentToMe(
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize
    ) {
        LOGGER.info("[GET] Request for find all deal requests to current user - pageNumber: {}, pageSize: {}", pageNumber, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(dealService.findRequestsSentToMe(pageable));
    }

    @Operation(
            summary = "Find all deal requests to current user filtered by status",
            description = "Searches all deal requests sent to the current user filtered by status with pagination"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All deal requests successfully received"),
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
    @GetMapping("/status")
    public ResponseEntity<Page<DealDto>> findRequestsSentToMeByStatus(
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize,
            @RequestParam(value = "status") @Parameter(description = "Status of deal", example = "APPROVED", required = true) Status status
    ) {
        LOGGER.info("[GET] Request for find all deal requests to current user by status - pageNumber: {}, pageSize: {}, status: {}", pageNumber, pageNumber, status);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(dealService.findRequestsSentToMeByStatus(status, pageable));
    }

    @Operation(
            summary = "Confirms the deal request"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Deal request successfully confirmed"),
                    @ApiResponse(responseCode = "404", description = "Deal request not found",
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
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(
            @PathVariable("id") @Parameter(description = "Deal request ID", example = "1", required = true) Long id
    ) {
        LOGGER.info("[POST] Request for confirm deal request with id: {}", id);
        dealService.confirm(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Cancels the deal request"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Deal request successfully canceled"),
                    @ApiResponse(responseCode = "404", description = "Deal request not found",
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
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable("id") @Parameter(description = "Deal request ID", example = "1", required = true) Long id
    ) {
        LOGGER.info("[POST] Request for cancel deal request with id: {}", id);
        dealService.cancel(id);
        return ResponseEntity.ok().build();
    }

}
