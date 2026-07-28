package com.elearning.platform.entity;

import com.elearning.platform.enums.LearningStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vark_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VarkResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer visualScore;

    private Integer auditoryScore;

    private Integer readingWritingScore;

    private Integer kinestheticScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LearningStyle dominantStyle;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

}