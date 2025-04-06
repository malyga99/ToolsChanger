package com.example.demo.review;

import com.example.demo.deal.Deal;
import com.example.demo.deal.DealRepository;
import com.example.demo.deal.Status;
import com.example.demo.exception.DealNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "review")
public class ReviewServiceImpl implements ReviewService {

    private final UserService userService;
    private final DealRepository dealRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Override
    @CacheEvict(key = "#result.recipient.id")
    public ReviewDto send(ReviewRequest reviewRequest) {
        LOGGER.debug("send - Creating a new review for deal with id: {}", reviewRequest.getDealId());

        User currentUser = userService.getCurrentUser();
        Deal deal = getValidatedDeal(reviewRequest.getDealId(), currentUser);
        validateReviewNotExists(currentUser, deal);

        User recipient = deal.getRequester().equals(currentUser) ? deal.getOwner() : deal.getRequester();

        Review review = Review.builder()
                .sender(currentUser)
                .recipient(recipient)
                .deal(deal)
                .rating(reviewRequest.getRating())
                .message(reviewRequest.getMessage())
                .build();

        Review savedReview = reviewRepository.save(review);
        LOGGER.debug("send - Successfully created a new review for deal with id: {}", reviewRequest.getDealId());

        return reviewMapper.toDto(savedReview);
    }

    @Override
    public Page<ReviewDto> findByUser(Long id, Pageable pageable) {
        LOGGER.debug("findByUser - Fetching all reviews by user with id: {}, pageNumber: {}, pageSize: {}", id, pageable.getPageNumber(), pageable.getPageSize());
        User user = userService.findById(id);

        Page<Review> reviews = reviewRepository.findByRecipient(pageable, user);

        LOGGER.debug("findByUser - Fetched {} reviews by user with id: {}", reviews.getContent().size(), id);
        return reviews.map(reviewMapper::toDto);
    }

    private Deal getValidatedDeal(Long dealId, User currentUser) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new DealNotFoundException("Deal with id: " + dealId + " not found"));

        if (deal.getStatus() != Status.APPROVED) {
            throw new UserDontHavePermissionException("User with id: %d cannot send review for a non-approved deal with id: %d".formatted(currentUser.getId(), dealId));
        }

        if (!deal.getRequester().equals(currentUser) && !deal.getOwner().equals(currentUser)) {
            throw new UserDontHavePermissionException("User with id: %d does not participate in the deal with id: %d".formatted(currentUser.getId(), dealId));
        }

        return deal;
    }

    @Override
    @Cacheable(key = "#id")
    public Double getAverageRatingByUser(Long id) {
        LOGGER.debug("getAverageRatingByUser - Fetching average rating by user with id: {}", id);

        User user = userService.findById(id);
        Double rating = reviewRepository.findAverageRatingByRecipient(user.getId());

        LOGGER.debug("getAverageRatingByUser - Fetched average rating: {} by user with id: {}", rating, id);
        return rating;
    }

    private void validateReviewNotExists(User currentUser, Deal deal) {
        if (reviewRepository.existsBySenderAndDeal(currentUser, deal)) {
            throw new UserDontHavePermissionException("User with id: %d has already sent a review on the deal with id: %d".formatted(currentUser.getId(), deal.getId()));
        }
    }
}
