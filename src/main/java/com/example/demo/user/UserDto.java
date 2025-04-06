package com.example.demo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing a user's details")
public class UserDto {

    @Schema(description = "User id", example = "1")
    private Long id;

    @Schema(description = "User firstname", example = "Ivan")
    private String firstname;

    @Schema(description = "User lastname", example = "IvanIvanov")
    private String lastname;
}
