package com.example.service;

import com.example.converter.TestScenarioConverter;
import com.example.converter.UserStoryConverter;
import com.example.dto.TestScenarioDto;
import com.example.dto.UserStoryDto;
import com.example.exception.CreateException;
import com.example.model.TestScenario;
import com.example.model.UserStory;
import com.example.repository.TestScenarioRepository;
import com.example.repository.UserStoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TestScenarioService {

    @Autowired
    private TestScenarioRepository testScenarioRepository;

    @Autowired
    private TestScenarioConverter testScenarioConverter;

    @Transactional
    public TestScenarioDto save(TestScenarioDto dto) {
        if (testScenarioRepository.findBySummary(dto.getSummary()).isPresent()) {
            throw new CreateException("Can't add test scenario, another scenario with summary " + dto.getSummary() + " already exists");
        }
        TestScenario entity = testScenarioRepository.save(testScenarioConverter.dtoToEntity(dto));
        return testScenarioConverter.entityToDto(entity);
    }

    public List<TestScenarioDto> findAll() {
        return StreamSupport.stream(testScenarioRepository.findAll().spliterator(), false)
                .map(testScenario -> testScenarioConverter.entityToDto(testScenario))
                .collect(Collectors.toList());
    }
}
