package com.example.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Data
@ToString(exclude = "testScenarios")
@EqualsAndHashCode(exclude = "testScenarios", callSuper = false)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFile implements Serializable {

    @Id
    @Column(name = "featureFileId", insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long featureFileId;

    @Column(unique = true, nullable = false)
    private String path;

    @Column(unique = true, nullable = false)
    private String filename;

    @OneToMany(mappedBy = "featureFile")
    private Set<TestScenario> testScenarios;
}
