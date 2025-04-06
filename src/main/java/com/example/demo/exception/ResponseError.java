package com.example.demo.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response error")
public class ResponseError {

    @Schema(description = "Error message", example = "error message")
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Time of error occurrence")
    private LocalDateTime time;

    @Schema(description = "Error status", example = "000")
    private Integer status;
}
