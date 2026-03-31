package com.example.repository;

import com.example.model.FeatureFile;
import com.example.model.TestScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeatureFileRepository extends PagingAndSortingRepository<FeatureFile, Long>, JpaSpecificationExecutor<FeatureFile> {

    Optional<FeatureFile> findById(Long id);

    Optional<FeatureFile> findByPathAndFilename(String path, String filename);

    Page<FeatureFile> findAll(Specification specification, Pageable pageable);
}