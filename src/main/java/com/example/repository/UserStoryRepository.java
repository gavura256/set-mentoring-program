package com.example.repository;

import com.example.model.UserStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStoryRepository extends PagingAndSortingRepository<UserStory, Long>, JpaSpecificationExecutor<UserStory> {

    Optional<UserStory> findById(Long id);

    Optional<UserStory> findByJiraId(String jiraId);

    Page<UserStory> findAll(Specification specification, Pageable pageable);
}