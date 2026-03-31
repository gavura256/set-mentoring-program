package com.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserStoryDto implements Serializable {
    @ApiModelProperty(notes = "JIRA ticket id", example = "PMCCP-12345", required = true)
    private String jiraId;
    @ApiModelProperty(notes = "User story summary", example = "Implement possibility to register user", required = true)
    private String summary;
}
