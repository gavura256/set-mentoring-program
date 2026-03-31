package com.example.service;

import com.example.converter.UserStoryConverter;
import com.example.dto.UserStoryDto;
import com.example.exception.CreateException;
import com.example.model.UserStory;
import com.example.repository.UserStoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class StoryService {

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private UserStoryConverter userStoryConverter;

    @Transactional
    public UserStoryDto save(UserStoryDto dto) {
        if (userStoryRepository.findByJiraId(dto.getJiraId()).isPresent()) {
            throw new CreateException("Can't add user story, another story with Jira id " + dto.getJiraId() + " already exists");
        }
        UserStory savedStory = userStoryRepository.save(userStoryConverter.dtoToEntity(dto));
        return userStoryConverter.entityToDto(savedStory);
    }

    public List<UserStoryDto> findAll() {
        return StreamSupport.stream(userStoryRepository.findAll().spliterator(), false)
                .map(userStory -> userStoryConverter.entityToDto(userStory))
                .collect(Collectors.toList());
    }
}
