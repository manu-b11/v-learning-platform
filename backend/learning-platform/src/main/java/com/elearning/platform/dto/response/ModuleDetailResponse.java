package com.elearning.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleDetailResponse {

    private Long id;

    private String title;

    private String description;

    private Integer orderNumber;

    private Integer progress;

    private List<ContentResponse> contents;

}