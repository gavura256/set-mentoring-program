package com.example.converter;

import com.example.dto.UserStoryDto;
import com.example.model.UserStory;
import org.springframework.stereotype.Component;

@Component
public class UserStoryConverter extends Converter {

    public UserStoryDto entityToDto(UserStory userStory) {
        UserStoryDto userStoryDto = new UserStoryDto();
        super.convert(userStory, userStoryDto);
        return userStoryDto;
    }

    public UserStory dtoToEntity(UserStoryDto dto) {
        UserStory userStory = new UserStory();
        super.convert(dto, userStory);
        return userStory;
    }
}
