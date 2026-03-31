package com.example.controller;

import com.example.dto.UserStoryDto;
import com.example.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/story")
public class UserStoryController extends AbstractController{

    @Autowired
    private StoryService userStoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserStoryDto addUserStory(@Valid @RequestBody UserStoryDto dto) {
        return userStoryService.save(dto);
    }

    @GetMapping
    List<UserStoryDto> getStories() {
        return userStoryService.findAll();
    }
}
