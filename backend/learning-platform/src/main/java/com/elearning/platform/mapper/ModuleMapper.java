package com.elearning.platform.mapper;

import com.elearning.platform.dto.response.ModuleDetailResponse;
import com.elearning.platform.dto.response.ModuleResponse;
import com.elearning.platform.entity.Module;
import com.elearning.platform.entity.User;
import com.elearning.platform.services.ProgressCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModuleMapper {

    private final ContentMapper contentMapper;
    private final ProgressCalculatorService progressCalculatorService;

    public ModuleResponse toResponse(
            Module module,
            User user
    ) {

        return ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .orderNumber(module.getOrderNumber())
                .progress(
                        progressCalculatorService.getModuleProgress(
                                module,
                                user
                        )
                )
                .build();
    }

    public ModuleDetailResponse toDetailResponse(
            Module module,
            User user
    ) {

        return ModuleDetailResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .orderNumber(module.getOrderNumber())
                .progress(
                        progressCalculatorService.getModuleProgress(
                                module,
                                user
                        )
                )
                .contents(
                        module.getContents()
                                .stream()
                                .map(content ->
                                        contentMapper.toResponse(
                                                content,
                                                user
                                        )
                                )
                                .toList()
                )
                .build();
    }

}