package com.example.demo.deal;

import com.example.demo.exception.DealNotFoundException;
import com.example.demo.exception.ToolNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.tool.Tool;
import com.example.demo.tool.ToolDto;
import com.example.demo.tool.ToolRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DealServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealMapper dealMapper;

    @InjectMocks
    private DealServiceImpl rentalService;

    private RentalRequest rentalRequest;

    private PurchaseRequest purchaseRequest;

    private User owner;

    private User requester;

    private Tool tool;

    private Deal firstDeal;

    private Deal secondDeal;

    private DealDto firstDealDto;

    private DealDto secondDealDto;

    private Pageable mockPageable;

    private Page<Deal> mockPage;

    @BeforeEach
    public void setup() {
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
        owner = User.builder().id(1L).login("IvanIvanov@gmail.com").build();
        requester = User.builder().id(2L).login("IvanIvanov2@gmail.com").build();
        tool = Tool.builder().id(1L).owner(owner).build();
        firstDeal = Deal.builder()
                .id(1L)
                .owner(owner)
                .requester(requester)
                .tool(tool)
                .message("Some message")
                .status(Status.PENDING)
                .price(BigDecimal.valueOf(3000L))
                .build();
        secondDeal = Deal.builder()
                .id(2L)
                .owner(owner)
                .requester(requester)
                .tool(tool)
                .message("Some message")
                .status(Status.PENDING)
                .price(BigDecimal.valueOf(3000L))
                .build();
        firstDealDto = DealDto.builder()
                .id(1L)
                .owner(UserDto.builder().id(1L).lastname("Ivanov").build())
                .requester(UserDto.builder().id(2L).firstname("Ivanov2").build())
                .tool(ToolDto.builder().id(1L).build())
                .status(Status.PENDING)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .build();
        secondDealDto = DealDto.builder()
                .id(2L)
                .owner(UserDto.builder().id(1L).lastname("Ivanov").build())
                .requester(UserDto.builder().id(2L).firstname("Ivanov2").build())
                .tool(ToolDto.builder().id(1L).build())
                .status(Status.PENDING)
                .message("Some message")
                .price(BigDecimal.valueOf(3000L))
                .build();
        mockPageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(firstDeal, secondDeal), mockPageable, 2);
    }

    @Test
    public void rent_returnSavedDealRequest() {
        when(userService.getCurrentUser()).thenReturn(requester);
        when(toolRepository.findById(1L)).thenReturn(Optional.of(tool));
        when(dealRepository.save(any(Deal.class))).thenReturn(firstDeal);
        when(dealMapper.toDto(firstDeal)).thenReturn(firstDealDto);
        ArgumentCaptor<Deal> argumentCaptor = ArgumentCaptor.forClass(Deal.class);

        DealDto result = rentalService.rent(rentalRequest);

        assertNotNull(result);
        assertEquals(firstDealDto, result);

        verify(userService, times(1)).getCurrentUser();
        verify(toolRepository, times(1)).findById(1L);
        verify(dealMapper, times(1)).toDto(firstDeal);
        verify(dealRepository, times(1)).save(argumentCaptor.capture());

        Deal deal = argumentCaptor.getValue();
        assertNotNull(deal);
        assertEquals(owner, deal.getOwner());
        assertEquals(requester, deal.getRequester());
        assertEquals(tool, deal.getTool());
        assertEquals(rentalRequest.getMessage(), deal.getMessage());
        assertEquals(rentalRequest.getStartDate(), deal.getStartDate());
        assertEquals(rentalRequest.getEndDate(), deal.getEndDate());
        assertEquals(rentalRequest.getPrice(), deal.getPrice());
        assertEquals(Status.PENDING, deal.getStatus());
    }

    @Test
    public void rent_toolNotFound_throwExc() {
        when(userService.getCurrentUser()).thenReturn(requester);
        when(toolRepository.findById(1L)).thenReturn(Optional.empty());

        ToolNotFoundException toolNotFoundException = assertThrows(ToolNotFoundException.class, () -> rentalService.rent(rentalRequest));
        assertEquals("Tool with id: " + rentalRequest.getToolId() + " not found", toolNotFoundException.getMessage());

        verifyNoInteractions(dealRepository);
    }

    @Test
    public void purchase_returnSavedDealRequest() {
        when(userService.getCurrentUser()).thenReturn(requester);
        when(toolRepository.findById(1L)).thenReturn(Optional.of(tool));
        when(dealRepository.save(any(Deal.class))).thenReturn(firstDeal);
        when(dealMapper.toDto(firstDeal)).thenReturn(firstDealDto);
        ArgumentCaptor<Deal> argumentCaptor = ArgumentCaptor.forClass(Deal.class);

        DealDto result = rentalService.purchase(purchaseRequest);

        assertNotNull(result);
        assertEquals(firstDealDto, result);

        verify(userService, times(1)).getCurrentUser();
        verify(toolRepository, times(1)).findById(1L);
        verify(dealMapper, times(1)).toDto(firstDeal);
        verify(dealRepository, times(1)).save(argumentCaptor.capture());

        Deal deal = argumentCaptor.getValue();
        assertNotNull(deal);
        assertEquals(owner, deal.getOwner());
        assertEquals(requester, deal.getRequester());
        assertEquals(tool, deal.getTool());
        assertEquals(purchaseRequest.getMessage(), deal.getMessage());
        assertEquals(purchaseRequest.getPrice(), deal.getPrice());
        assertEquals(Status.PENDING, deal.getStatus());
    }

    @Test
    public void purchase_toolNotFound_throwExc() {
        when(userService.getCurrentUser()).thenReturn(requester);
        when(toolRepository.findById(1L)).thenReturn(Optional.empty());

        ToolNotFoundException toolNotFoundException = assertThrows(ToolNotFoundException.class, () -> rentalService.purchase(purchaseRequest));
        assertEquals("Tool with id: " + rentalRequest.getToolId() + " not found", toolNotFoundException.getMessage());

        verifyNoInteractions(dealRepository);
    }

    @Test
    public void findRequestSentToMe_returnTwoDealRequests() {
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findByOwner(owner, mockPageable)).thenReturn(mockPage);
        when(dealMapper.toDto(firstDeal)).thenReturn(firstDealDto);
        when(dealMapper.toDto(secondDeal)).thenReturn(secondDealDto);

        Page<DealDto> result = rentalService.findRequestsSentToMe(mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstDealDto, result.getContent().get(0));
        assertEquals(secondDealDto, result.getContent().get(1));

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findByOwner(owner, mockPageable);
        verify(dealMapper, times(1)).toDto(firstDeal);
        verify(dealMapper, times(1)).toDto(secondDeal);
    }

    @Test
    public void findRequestSentToMeByStatus_returnTwoDealRequests() {
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findByOwnerAndStatus(owner, Status.PENDING, mockPageable)).thenReturn(mockPage);
        when(dealMapper.toDto(firstDeal)).thenReturn(firstDealDto);
        when(dealMapper.toDto(secondDeal)).thenReturn(secondDealDto);

        Page<DealDto> result = rentalService.findRequestsSentToMeByStatus(Status.PENDING, mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstDealDto, result.getContent().get(0));
        assertEquals(secondDealDto, result.getContent().get(1));

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findByOwnerAndStatus(owner, Status.PENDING, mockPageable);
        verify(dealMapper, times(1)).toDto(firstDeal);
        verify(dealMapper, times(1)).toDto(secondDeal);
    }

    @Test
    public void confirm_successfullyConfirmDealRequest() {
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(firstDeal));

        rentalService.confirm(1L);

        assertEquals(Status.APPROVED, firstDeal.getStatus());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(dealRepository, times(1)).save(firstDeal);
    }

    @Test
    public void confirm_dealNotFound_throwExc() {
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findById(1L)).thenReturn(Optional.empty());

        DealNotFoundException dealNotFoundException = assertThrows(DealNotFoundException.class, () -> rentalService.confirm(1L));
        assertEquals("Deal with id: 1 not found", dealNotFoundException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    public void confirm_userDoesNotHaveRights_throwExc() {
        User owner = User.builder().id(3L).build();
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(firstDeal));

        UserDontHavePermissionException userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> rentalService.confirm(1L));
        assertEquals("User with id: %d cannot modify deal with id: %d".formatted(owner.getId(), firstDeal.getId()),
                userDontHavePermissionException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    public void cancel_successfullyCancelDealRequest() {
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(firstDeal));

        rentalService.cancel(1L);

        assertEquals(Status.REJECTED, firstDeal.getStatus());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(dealRepository, times(1)).save(firstDeal);
    }

    @Test
    public void cancel_dealNotFound_throwExc() {
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findById(1L)).thenReturn(Optional.empty());

        DealNotFoundException dealNotFoundException = assertThrows(DealNotFoundException.class, () -> rentalService.cancel(1L));
        assertEquals("Deal with id: 1 not found", dealNotFoundException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(dealRepository, never()).save(any(Deal.class));
    }

    @Test
    public void cancel_userDoesNotHaveRights_throwExc() {
        User owner = User.builder().id(3L).build();
        when(userService.getCurrentUser()).thenReturn(owner);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(firstDeal));

        UserDontHavePermissionException userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> rentalService.cancel(1L));
        assertEquals("User with id: %d cannot modify deal with id: %d".formatted(owner.getId(), firstDeal.getId()),
                userDontHavePermissionException.getMessage());

        verify(userService, times(1)).getCurrentUser();
        verify(dealRepository, times(1)).findById(1L);
        verify(dealRepository, never()).save(any(Deal.class));
    }

}