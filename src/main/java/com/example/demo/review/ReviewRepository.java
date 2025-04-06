package com.example.demo.review;

import com.example.demo.deal.Deal;
import com.example.demo.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsBySenderAndDeal(User sender, Deal deal);

    Page<Review> findByRecipient(Pageable pageable, User recipient);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.recipient.id = :recipientId")
    Double findAverageRatingByRecipient(@Param("recipientId") Long recipientId);
}
