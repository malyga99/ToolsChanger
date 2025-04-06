package com.example.demo.deal;

import com.example.demo.tool.ToolDto;
import com.example.demo.user.UserDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing deal requests")
public class DealDto {

    @Schema(description = "Deal request id", example = "1")
    private Long id;

    @Schema(description = "Owner of the tool", implementation = UserDto.class)
    private UserDto owner;

    @Schema(description = "Requester for a deal", implementation = UserDto.class)
    private UserDto requester;

    @Schema(description = "Tool for deal", implementation = ToolDto.class)
    private ToolDto tool;

    @Schema(description = "Price per deal", example = "3000")
    private BigDecimal price;

    @Schema(description = "Message about deal", example = "Some message")
    private String message;

    @Schema(description = "Status of deal", example = "PENDING")
    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Start of rental", example = "2023-01-01 12:00")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "End of rental", example = "2023-01-01 12:00")
    private LocalDateTime endDate;

}
