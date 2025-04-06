package com.example.demo.deal;

import com.example.demo.tool.Tool;
import com.example.demo.tool.ToolDto;
import com.example.demo.tool.ToolMapper;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DealMapperTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ToolMapper toolMapper;

    @InjectMocks
    private DealMapper dealMapper;

    @Test
    public void toDto_returnCorrectlyDto() {
        User owner = User.builder().lastname("Ivanov").build();
        User requester = User.builder().lastname("Ivanov2").build();
        Tool tool = Tool.builder().description("Some desc").build();
        Deal deal = Deal.builder()
                .id(1L)
                .owner(owner)
                .requester(requester)
                .tool(tool)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .status(Status.PENDING)
                .build();

        when(userMapper.toDto(owner)).thenReturn(UserDto.builder().lastname("Ivanov").build());
        when(userMapper.toDto(requester)).thenReturn(UserDto.builder().lastname("Ivanov2").build());
        when(toolMapper.toDto(tool)).thenReturn(ToolDto.builder().description("Some desc").build());

        DealDto result = dealMapper.toDto(deal);

        assertNotNull(result);
        assertEquals(deal.getId(), result.getId());
        assertEquals(owner.getLastname(), result.getOwner().getLastname());
        assertEquals(requester.getLastname(), result.getRequester().getLastname());
        assertEquals(tool.getDescription(), result.getTool().getDescription());
        assertEquals(deal.getStatus(), result.getStatus());
        assertEquals(deal.getMessage(), result.getMessage());
        assertEquals(BigDecimal.valueOf(3000L), deal.getPrice());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
    }

}