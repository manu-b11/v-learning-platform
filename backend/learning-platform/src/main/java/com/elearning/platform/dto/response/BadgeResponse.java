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
public class BadgeResponse {

    private Long id;

    private String name;

    private String description;

    private String icon;

    private Integer requiredPoints;

}