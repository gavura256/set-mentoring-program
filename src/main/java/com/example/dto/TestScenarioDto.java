package com.example.dto;

import com.example.model.enums.Country;
import com.example.model.enums.TestScenarioPriority;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class TestScenarioDto {
    @Valid
    @NotNull
    private FeatureFileDto featureFile;
    @NotEmpty
    @ApiModelProperty(notes = "Test scenario name/summary", example = "Alert message should appear when user age less than 18y", required = true)
    private String summary;
    @ApiModelProperty(notes = "Test scenario priority")
    private TestScenarioPriority priority;
    @ApiModelProperty(notes = "Countries (localizations) covered in test scenario")
    private Set<Country> countries = new HashSet<>();
    @ApiModelProperty(notes = "Number of Data rows (iterations) for data-driven test scenarios")
    private int dataRows = 1;
    @ApiModelProperty(notes = "Is scenario manual or automated")
    private boolean isManual = false;
    @ApiModelProperty(notes = "Is scenario currently wiped (muted, turned off)")
    private boolean isWiped = false;
    @ApiModelProperty(notes = "User stories that scenario covers", dataType = "List", example = "PMCCP-12345, PMCCP-22222, PMCCP-33333")
    private Set<String> coverage = new HashSet<>();
}
