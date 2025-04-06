package com.example.demo.register;

import com.example.demo.exception.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/register")
@RequiredArgsConstructor
@Tag(
        name = "Registration controller",
        description = "Handles user registration"
)
public class RegisterController {

    private final RegisterService registerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

    @Operation(
            summary = "Registers a new user",
            description = "Creates a new user account and returns an jwt token"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Registration successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RegisterResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    ),
                    @ApiResponse(responseCode = "409", description = "User with this login already exists",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<RegisterResponse> register(
            @RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration details"
            ) RegisterRequest registerRequest
    ) {
        LOGGER.info("[POST] Register request received for login: {}", registerRequest.getLogin());
        RegisterResponse response = registerService.register(registerRequest);
        return ResponseEntity.ok(response);
    }
}
