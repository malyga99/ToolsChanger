package com.example.demo.deal;

import com.example.demo.exception.DealNotFoundException;
import com.example.demo.exception.ToolNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.handler.GlobalHandler;
import com.example.demo.tool.ToolDto;
import com.example.demo.user.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DealControllerTest {

    @Mock
    private DealService dealService;

    @InjectMocks
    private DealController dealController;

    private MockMvc mockMvc;

    private RentalRequest rentalRequest;

    private PurchaseRequest purchaseRequest;

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private DealDto firstDealDto;

    private DealDto secondDealDto;

    private Pageable mockPageable;

    private Page<DealDto> mockPage;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(dealController)
                .setControllerAdvice(new GlobalHandler())
                .build();
        rentalRequest = RentalRequest.builder()
                .toolId(1L)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .build();
        purchaseRequest = PurchaseRequest.builder()
                .toolId(1L)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .build();
        firstDealDto = DealDto.builder()
                .id(1L)
                .owner(UserDto.builder().id(1L).build())
                .requester(UserDto.builder().id(2L).build())
                .tool(ToolDto.builder().id(1L).build())
                .status(Status.APPROVED)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .build();
        secondDealDto = DealDto.builder()
                .id(2L)
                .owner(UserDto.builder().id(1L).build())
                .requester(UserDto.builder().id(2L).build())
                .tool(ToolDto.builder().id(1L).build())
                .status(Status.APPROVED)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .build();
        mockPageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(firstDealDto, secondDealDto), mockPageable, 2);
    }

    @Test
    public void rent_returnCreatedDealRequest() throws Exception {
        String rentalRequestJson = objectMapper.writeValueAsString(rentalRequest);
        when(dealService.rent(rentalRequest)).thenReturn(firstDealDto);

        mockMvc.perform(post("/api/v1/deals/rental")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentalRequestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/deals/" + firstDealDto.getId()))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.owner.id").value(1))
                .andExpect(jsonPath("$.requester.id").value(2))
                .andExpect(jsonPath("$.tool.id").value(1))
                .andExpect(jsonPath("$.message").value("Some message"))
                .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(3000L)))
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());

        verify(dealService, times(1)).rent(rentalRequest);
    }

    @Test
    public void rent_invalidData_returnBadRequest() throws Exception {
        RentalRequest invalidRequest = RentalRequest.builder()
                .toolId(-1L)
                .startDate(null)
                .endDate(null)
                .message("")
                .price(BigDecimal.valueOf(-3000L))
                .build();
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/deals/rental")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.time").exists());
    }

    @Test
    public void rent_toolNotFound_returnNotFound() throws Exception {
        String rentalRequestJson = objectMapper.writeValueAsString(rentalRequest);
        when(dealService.rent(rentalRequest)).thenThrow(new ToolNotFoundException("Tool with id: 1 not found"));

        mockMvc.perform(post("/api/v1/deals/rental")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rentalRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tool with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(dealService, times(1)).rent(rentalRequest);
    }

    @Test
    public void purchase_returnCreatedDealRequest() throws Exception {
        String purchaseRequestJson = objectMapper.writeValueAsString(purchaseRequest);
        when(dealService.purchase(purchaseRequest)).thenReturn(firstDealDto);

        mockMvc.perform(post("/api/v1/deals/purchase")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseRequestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/deals/" + firstDealDto.getId()))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.owner.id").value(1))
                .andExpect(jsonPath("$.requester.id").value(2))
                .andExpect(jsonPath("$.tool.id").value(1))
                .andExpect(jsonPath("$.message").value("Some message"))
                .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(3000L)));

        verify(dealService, times(1)).purchase(purchaseRequest);
    }

    @Test
    public void purchase_invalidData_returnBadRequest() throws Exception {
        PurchaseRequest invalidRequest = PurchaseRequest.builder()
                .toolId(-1L)
                .message("")
                .price(BigDecimal.valueOf(-3000L))
                .build();
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/deals/purchase")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.time").exists());
    }

    @Test
    public void purchase_toolNotFound_returnNotFound() throws Exception {
        String purchaseRequestJson = objectMapper.writeValueAsString(purchaseRequest);
        when(dealService.purchase(purchaseRequest)).thenThrow(new ToolNotFoundException("Tool with id: 1 not found"));

        mockMvc.perform(post("/api/v1/deals/purchase")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tool with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(dealService, times(1)).purchase(purchaseRequest);
    }

    @Test
    public void findRequestSentToMe_returnTwoDealRequests() throws Exception {
        when(dealService.findRequestsSentToMe(mockPageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/deals")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].owner.id").value(1))
                .andExpect(jsonPath("$.content[0].requester.id").value(2))
                .andExpect(jsonPath("$.content[0].tool.id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].owner.id").value(1))
                .andExpect(jsonPath("$.content[1].requester.id").value(2))
                .andExpect(jsonPath("$.content[1].tool.id").value(1));

        verify(dealService, times(1)).findRequestsSentToMe(mockPageable);
    }

    @Test
    public void findRequestSentToMeByStatus_returnTwoDealRequests() throws Exception {
        when(dealService.findRequestsSentToMeByStatus(Status.APPROVED, mockPageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/deals/status")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("status", "APPROVED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].owner.id").value(1))
                .andExpect(jsonPath("$.content[0].requester.id").value(2))
                .andExpect(jsonPath("$.content[0].tool.id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].owner.id").value(1))
                .andExpect(jsonPath("$.content[1].requester.id").value(2))
                .andExpect(jsonPath("$.content[1].tool.id").value(1));

        verify(dealService, times(1)).findRequestsSentToMeByStatus(Status.APPROVED, mockPageable);
    }

    @Test
    public void confirm_successfullyConfirmDealRequest() throws Exception {
        mockMvc.perform(post("/api/v1/deals/{id}/confirm", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(dealService, times(1)).confirm(1L);
    }

    @Test
    public void confirm_dealRequestNotFound_returnNotFound() throws Exception {
        doThrow(new DealNotFoundException("Deal with id: 1 not found")).when(dealService).confirm(1L);

        mockMvc.perform(post("/api/v1/deals/{id}/confirm", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Deal with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(dealService, times(1)).confirm(1L);
    }

    @Test
    public void confirm_dontHavePermission_returnForbidden() throws Exception {
        doThrow(new UserDontHavePermissionException("User with id: 1 cannot modify deal with id: 1")).when(dealService).confirm(1L);

        mockMvc.perform(post("/api/v1/deals/{id}/confirm", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User with id: 1 cannot modify deal with id: 1"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.time").exists());

        verify(dealService, times(1)).confirm(1L);
    }

    @Test
    public void cancel_successfullyCancelDealRequest() throws Exception {
        mockMvc.perform(post("/api/v1/deals/{id}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(dealService, times(1)).cancel(1L);
    }

    @Test
    public void cancel_dealRequestNotFound_returnNotFound() throws Exception {
        doThrow(new DealNotFoundException("Deal with id: 1 not found")).when(dealService).cancel(1L);

        mockMvc.perform(post("/api/v1/deals/{id}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Deal with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(dealService, times(1)).cancel(1L);
    }

    @Test
    public void cancel_dontHavePermission_returnForbidden() throws Exception {
        doThrow(new UserDontHavePermissionException("User with id: 1 cannot modify deal with id: 1")).when(dealService).cancel(1L);

        mockMvc.perform(post("/api/v1/deals/{id}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User with id: 1 cannot modify deal with id: 1"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.time").exists());

        verify(dealService, times(1)).cancel(1L);
    }
}