package com.example.demo.register;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration request")
public class RegisterRequest {

    @NotBlank(message = "Firstname must be filled in!")
    @Size(max = 100, message = "Maximum firstname length is 100 characters!")
    @Schema(description = "User firstname", example = "Ivan")
    private String firstname;

    @NotBlank(message = "Lastname must be filled in!")
    @Size(max = 100, message = "Maximum lastname length is 100 characters!")
    @Schema(description = "User lastname", example = "IvanIvanov")
    private String lastname;

    @NotBlank(message = "Login must be filled in!")
    @Size(min = 3, max = 100, message = "Login length must be from 3 to 100 characters!")
    @Schema(description = "User login", example = "IvanIvanov@gmail.com")
    private String login;

    @NotBlank(message = "Password must be filled in!")
    @Size(min = 3, max = 100, message = "Password length must be from 3 to 100 characters!")
    @Schema(description = "User password", example = "abcde")
    private String password;
}
