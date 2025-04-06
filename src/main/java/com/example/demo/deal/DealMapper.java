package com.example.demo.deal;

import com.example.demo.tool.ToolMapper;
import com.example.demo.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealMapper {

    private final UserMapper userMapper;
    private final ToolMapper toolMapper;

    public DealDto toDto(Deal deal) {
        return DealDto.builder()
                .id(deal.getId())
                .owner(userMapper.toDto(deal.getOwner()))
                .requester(userMapper.toDto(deal.getRequester()))
                .tool(toolMapper.toDto(deal.getTool()))
                .price(deal.getPrice())
                .status(deal.getStatus())
                .message(deal.getMessage())
                .startDate(deal.getStartDate())
                .endDate(deal.getEndDate())
                .build();
    }
}
