package com.example.model;

import com.example.model.enums.Country;
import com.example.model.enums.TestScenarioPriority;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Data
@ToString(exclude = "userStories")
@EqualsAndHashCode(exclude = "userStories", callSuper = false)
@Entity
public class TestScenario implements Serializable {

    @Id
    @Column(name = "scenarioId", insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scenarioId;

    @Column(unique = true, nullable = false)
    private String summary;
    @Column
    @Enumerated(EnumType.STRING)
    private TestScenarioPriority priority;
    @ElementCollection(targetClass = Country.class)
    @JoinTable(name = "countries", joinColumns = @JoinColumn(name = "scenarioId"))
    @Column(name = "countryName", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Country> countries;

    @Column
    private int dataRows = 0;
    @Column
    private boolean isManual;
    @Column
    private boolean isWiped;

    @ManyToMany
    @JoinTable(name = "STORY_SCENARIO",
            joinColumns =
            @JoinColumn(name = "scenarioId", referencedColumnName = "scenarioId", insertable = false, updatable = false),
            inverseJoinColumns =
            @JoinColumn(name = "storyId", referencedColumnName = "storyId", insertable = false, updatable = false)
    )
    private Set<UserStory> userStories;

    @ManyToOne
    @JoinColumn(name = "featureFileId", nullable = false)
    private FeatureFile featureFile;
}
