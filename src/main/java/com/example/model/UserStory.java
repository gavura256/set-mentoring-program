package com.example.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Data
@ToString(exclude = "testScenarios")
@EqualsAndHashCode(exclude = "testScenarios", callSuper = false)
@Entity
public class UserStory implements Serializable {
    @Id
    @Column(name = "storyId", insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storyId;

    @Column(name = "jira_id", unique = true, nullable = false)
    private String jiraId;

    private String summary;

    @ManyToMany(mappedBy = "userStories")
    private Set<TestScenario> testScenarios;
}
