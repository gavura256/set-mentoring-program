package com.bookshop.repository;

import com.bookshop.model.StoreItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {

    Optional<StoreItem> findByProductId(Long productId);

    @Override
    @EntityGraph(attributePaths = "product")
    List<StoreItem> findAll();
}
