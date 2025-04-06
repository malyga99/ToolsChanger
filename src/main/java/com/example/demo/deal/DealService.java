package com.example.demo.deal;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DealService {

    DealDto rent(RentalRequest rentalRequest);

    Page<DealDto> findRequestsSentToMe(Pageable pageable);

    void confirm(Long id);

    void cancel(Long id);

    Page<DealDto> findRequestsSentToMeByStatus(Status status, Pageable pageable);

    DealDto purchase(@Valid PurchaseRequest purchaseRequest);
}
