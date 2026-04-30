package com.bookshop.repository;

import com.bookshop.model.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.product")
    Page<Booking> findAllWithFetch(Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.product WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithFetch(@Param("userId") Long userId);

    @Query(value = "SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.product WHERE b.user.id = :userId",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findByUserIdWithFetch(@Param("userId") Long userId, Pageable pageable);

    boolean existsByProductId(Long productId);
}
