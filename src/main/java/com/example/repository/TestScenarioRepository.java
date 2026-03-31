package com.example.repository;

import com.example.model.TestScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestScenarioRepository extends PagingAndSortingRepository<TestScenario, Long>, JpaSpecificationExecutor<TestScenario> {

    Optional<TestScenario> findById(Long id);

    Optional<TestScenario> findBySummary(String summary);

    Page<TestScenario> findAll(Specification specification, Pageable pageable);
}