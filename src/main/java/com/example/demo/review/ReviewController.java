package com.example.demo.review;

import com.example.demo.deal.DealDto;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(
        name = "Reviews controller",
        description = "Controller for working with reviews"
)
public class ReviewController {

    private final ReviewService reviewService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewController.class);


    @Operation(
            summary = "Send review",
            description = "Sends a review to the second participant of the deal"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Review successfully sent",
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
                    @ApiResponse(responseCode = "404", description = "Deal not found",
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
                    @ApiResponse(responseCode = "403", description = "Authorization error, " +
                            "deal not approved, review already sent or you are not a participant in the deal",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    )

            }
    )
    @PostMapping
    public ResponseEntity<ReviewDto> send(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Review details"
            ) ReviewRequest reviewRequest
    ) {
        LOGGER.info("[POST] Request to send a review for deal: {}", reviewRequest.getDealId());
        ReviewDto savedReview = reviewService.send(reviewRequest);
        return ResponseEntity.created(URI.create("/api/v1/reviews/" + savedReview.getId())).body(savedReview);
    }

    @Operation(
            summary = "Find all user reviews",
            description = "Searches all user reviews by id with pagination"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "All user reviews successfully received"),
                    @ApiResponse(responseCode = "404", description = "User not found",
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
    @GetMapping("/{id}")
    public ResponseEntity<Page<ReviewDto>> findByUser(
            @PathVariable("id") @Parameter(description = "User id", example = "1") Long id,
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize
    ) {
        LOGGER.info("[GET] Request for find all reviews by user with id: {}", id);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "createdAt");
        return ResponseEntity.ok(reviewService.findByUser(id, pageable));
    }


    @Operation(
            summary = "Returns average rating",
            description = "Returns user average rating by id"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Average rating successfully received"),
                    @ApiResponse(responseCode = "404", description = "User not found",
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
    @GetMapping("/{id}/rating")
    public ResponseEntity<Double> getAverageRatingByUser(
            @PathVariable("id") @Parameter(description = "User id", example = "1") Long id
    ) {
        LOGGER.info("[GET] Request for get average rating by user with id: {}", id);
        return ResponseEntity.ok(reviewService.getAverageRatingByUser(id));
    }
}
