package com.elearning.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingResponse {

    private Integer position;

    private Long userId;

    private String firstName;

    private String lastName;

    private Integer totalPoints;

}