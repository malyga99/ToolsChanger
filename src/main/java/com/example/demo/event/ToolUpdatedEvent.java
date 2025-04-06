package com.example.demo.event;

import com.example.demo.tool.Tool;
import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolUpdatedEvent {

    private Tool updatedTool;
}
