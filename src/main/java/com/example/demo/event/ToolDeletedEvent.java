package com.example.demo.event;

import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolDeletedEvent {

    private Long toolId;
}
