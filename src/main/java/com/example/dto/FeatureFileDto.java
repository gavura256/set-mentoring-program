package com.example.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeatureFileDto {
    @NotNull
    @ApiModelProperty(notes = "Feature file name", example = "LoginEventForSiftValidation.feature", required = true)
    private String name;
    @NotNull
    @ApiModelProperty(notes = "Feature file path", example = "/automation/educator-ui-automation/src/main/resources/features/sift/", required = true)
    private String path;
}
