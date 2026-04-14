package com.bookshop.repository;

import com.bookshop.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.product")
    Page<Booking> findAllWithFetch(Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.product WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithFetch(@Param("userId") Long userId);
}
