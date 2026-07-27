package com.elearning.platform.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Column(nullable = false)
    private Integer visualScore;

    @Column(nullable = false)
    private Integer auditoryScore;

    @Column(nullable = false)
    private Integer readingWritingScore;

    @Column(nullable = false)
    private Integer kinestheticScore;

    @Column(nullable = false, length = 30)
    private String dominantStyle;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference
    private User user;

}