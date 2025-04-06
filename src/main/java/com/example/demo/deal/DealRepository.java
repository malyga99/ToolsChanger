package com.example.demo.deal;

import com.example.demo.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Long> {

    Page<Deal> findByOwner(User owner, Pageable pageable);

    Page<Deal> findByOwnerAndStatus(User owner, Status status, Pageable pageable);
}
