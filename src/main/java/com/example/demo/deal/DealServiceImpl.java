package com.example.demo.deal;

import com.example.demo.exception.DealNotFoundException;
import com.example.demo.exception.ToolNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.tool.Tool;
import com.example.demo.tool.ToolRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final UserService userService;
    private final ToolRepository toolRepository;
    private final DealRepository dealRepository;
    private final DealMapper dealMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(DealServiceImpl.class);

    @Override
    public DealDto rent(RentalRequest rentalRequest) {
        LOGGER.debug("rent: Creating a new deal request for tool with id: {}", rentalRequest.getToolId());

        User renter = userService.getCurrentUser();
        Tool tool = toolRepository.findById(rentalRequest.getToolId())
                .orElseThrow(() -> new ToolNotFoundException("Tool with id: " + rentalRequest.getToolId() + " not found"));
        User owner = tool.getOwner();

        Deal deal = Deal.builder()
                .owner(owner)
                .requester(renter)
                .tool(tool)
                .message(rentalRequest.getMessage())
                .price(rentalRequest.getPrice())
                .startDate(rentalRequest.getStartDate())
                .endDate(rentalRequest.getEndDate())
                .status(Status.PENDING)
                .build();
        Deal savedDeal = dealRepository.save(deal);
        LOGGER.debug("rent: Successfully created a new deal request for tool with id: {}", rentalRequest.getToolId());

        return dealMapper.toDto(savedDeal);
    }

    @Override
    public DealDto purchase(PurchaseRequest purchaseRequest) {
        LOGGER.debug("purchase: Creating a new deal request for tool with id: {}", purchaseRequest.getToolId());

        User buyer = userService.getCurrentUser();
        Tool tool = toolRepository.findById(purchaseRequest.getToolId())
                .orElseThrow(() -> new ToolNotFoundException("Tool with id: " + purchaseRequest.getToolId() + " not found"));
        User owner = tool.getOwner();

        Deal deal = Deal.builder()
                .owner(owner)
                .requester(buyer)
                .tool(tool)
                .message(purchaseRequest.getMessage())
                .price(purchaseRequest.getPrice())
                .status(Status.PENDING)
                .build();
        Deal savedDeal = dealRepository.save(deal);
        LOGGER.debug("purchase: Successfully created a new deal request for tool with id: {}", purchaseRequest.getToolId());

        return dealMapper.toDto(savedDeal);
    }

    @Override
    public Page<DealDto> findRequestsSentToMe(Pageable pageable) {
        LOGGER.debug("findRequestsSentToMe: Fetching deal requests sent to the current user - pageNumber: {}, pageSize: {}", pageable.getPageNumber(), pageable.getPageSize());

        User owner = userService.getCurrentUser();
        Page<Deal> deals = dealRepository.findByOwner(owner, pageable);

        LOGGER.debug("findRequestsSentToMe: Fetched {} deal requests sent to the current user: {}", deals.getContent().size(), owner.getLogin());
        return deals.map(dealMapper::toDto);
    }

    @Override
    public Page<DealDto> findRequestsSentToMeByStatus(Status status, Pageable pageable) {
        LOGGER.debug("findRequestsSentToMeByStatus: Fetching deal requests sent to the current user by status - pageNumber: {}, pageSize: {}, status: {}", pageable.getPageNumber(), pageable.getPageSize(), status);

        User owner = userService.getCurrentUser();
        Page<Deal> deals = dealRepository.findByOwnerAndStatus(owner, status, pageable);

        LOGGER.debug("findRequestsSentToMeByStatus: Fetched {} deal requests sent to the current user: {} by status: {}", deals.getContent().size(), owner.getLogin(), status);
        return deals.map(dealMapper::toDto);
    }

    @Override
    public void confirm(Long id) {
        LOGGER.debug("confirm - Confirming deal request by id: {}", id);

        User owner = userService.getCurrentUser();
        Deal deal = dealRepository.findById(id)
                        .orElseThrow(() -> new DealNotFoundException("Deal with id: " + id + " not found"));
        checkUserRights(deal, owner);

        deal.setStatus(Status.APPROVED);
        LOGGER.debug("confirm - Successfully confirmed deal request by id: {}", id);
        dealRepository.save(deal);
    }

    @Override
    public void cancel(Long id) {
        LOGGER.debug("confirm - Canceling deal request by id: {}", id);

        User owner = userService.getCurrentUser();
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new DealNotFoundException("Deal with id: " + id + " not found"));
        checkUserRights(deal, owner);

        deal.setStatus(Status.REJECTED);
        LOGGER.debug("confirm - Successfully canceled deal request by id: {}", id);
        dealRepository.save(deal);
    }

    private void checkUserRights(Deal deal, User user) {
        if (!deal.getOwner().getId().equals(user.getId())) {
            throw new UserDontHavePermissionException("User with id: " + user.getId() + " cannot modify deal with id: " + deal.getId());
        }
    }
}
