package com.example.demo.openId;

import com.example.demo.exception.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/openid")
@RequiredArgsConstructor
@Tag(
        name = "OpenID controller",
        description = "Handles OpenID authentication and token exchange"
)
public class OpenIdController {

    private final OpenIdService openIdService;
    private final static Logger LOGGER = LoggerFactory.getLogger(OpenIdController.class);

    @Operation(
            summary = "Exchange authorization code for JWT token",
            description = "Receives authorization code and state, and returns a JWT token after successful validation and token exchange"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved JWT token"),
                    @ApiResponse(responseCode = "400", description = "Validation exception (e.g invalid state or ID token)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseError.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<String> getJwtToken(
            @RequestParam(name = "authCode") @Parameter(description = "Authorization Code", required = true) String authCode,
            @RequestParam(name = "state") @Parameter(description = "State", required = true) String state
    ) {
        LOGGER.info("[POST] Request for get JWT token from Google with Auth Code");
        String jwtToken = openIdService.getJwtToken(authCode, state);
        return ResponseEntity.ok(jwtToken);
    }
}
