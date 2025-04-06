package com.example.demo.tool;

import com.example.demo.user.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

    Page<Tool> findByOwner(Pageable pageable, User user);

    Page<Tool> findAllByIdIn(Iterable<Long> ids, Pageable pageable);

    @Query("SELECT t FROM Tool t LEFT JOIN FETCH t.photos WHERE t.id = :id")
    Optional<Tool> findByIdWithPhotos(@Param("id") Long id);

}
