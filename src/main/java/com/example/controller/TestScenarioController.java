package com.example.controller;

import com.example.dto.TestScenarioDto;
import com.example.dto.UserStoryDto;
import com.example.service.TestScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestScenarioController extends AbstractController {

    @Autowired
    private TestScenarioService testScenarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TestScenarioDto addTestScenario(@Valid @RequestBody TestScenarioDto dto) {
        return testScenarioService.save(dto);
    }

    @GetMapping
    List<TestScenarioDto> getAllTests() {
        return testScenarioService.findAll();
    }
}
