package com.example.converter;

import com.example.dto.FeatureFileDto;
import com.example.dto.TestScenarioDto;
import com.example.model.FeatureFile;
import com.example.model.TestScenario;
import com.example.model.UserStory;
import com.example.repository.FeatureFileRepository;
import com.example.repository.UserStoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TestScenarioConverter extends Converter {

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private FeatureFileRepository featureFileRepository;

    public TestScenarioDto entityToDto(TestScenario entity) {
        TestScenarioDto dto = new TestScenarioDto();
        super.convert(entity, dto);
        dto.setCoverage(entity.getUserStories().stream().map(UserStory::getJiraId).collect(Collectors.toSet()));
        dto.setFeatureFile(new FeatureFileDto(entity.getFeatureFile().getFilename(), entity.getFeatureFile().getPath()));
        return dto;
    }

    public TestScenario dtoToEntity(TestScenarioDto dto) {
        TestScenario entity = new TestScenario();
        super.convert(dto, entity);

        Set<UserStory> userStorySet = new HashSet<>();
        for (String storyId : dto.getCoverage()) {
            Optional<UserStory> byJiraId = userStoryRepository.findByJiraId(storyId);
            if (byJiraId.isPresent()) {
                userStorySet.add(byJiraId.get());
            } else {
                UserStory userStory = new UserStory();
                userStory.setJiraId(storyId);
                userStory.setSummary("UNKNOWN");
                UserStory savedStory = userStoryRepository.save(userStory);
                userStorySet.add(savedStory);
            }
        }

        FeatureFile featureFile = featureFileRepository
                .findByPathAndFilename(dto.getFeatureFile().getPath(), dto.getFeatureFile().getName())
                .orElseGet(() -> featureFileRepository.save(
                        FeatureFile.builder()
                                .filename(dto.getFeatureFile().getName())
                                .path(dto.getFeatureFile().getPath())
                                .build()));
        entity.setUserStories(userStorySet);
        entity.setFeatureFile(featureFile);
        return entity;

    }
}
