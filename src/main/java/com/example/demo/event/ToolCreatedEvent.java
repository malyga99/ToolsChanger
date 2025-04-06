package com.example.demo.event;

import com.example.demo.tool.Tool;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCreatedEvent {

    private Tool createdTool;
}
