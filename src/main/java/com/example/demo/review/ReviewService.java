package com.example.demo.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDto send(ReviewRequest reviewRequest);

    Page<ReviewDto> findByUser(Long id, Pageable pageable);

    Double getAverageRatingByUser(Long id);
}
