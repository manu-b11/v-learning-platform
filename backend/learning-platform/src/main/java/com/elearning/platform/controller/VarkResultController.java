package com.elearning.platform.controller;

import com.elearning.platform.dto.request.SaveVarkResultRequest;
import com.elearning.platform.dto.response.VarkResultResponse;
import com.elearning.platform.services.VarkResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vark")
@RequiredArgsConstructor
public class VarkResultController {

    private final VarkResultService varkResultService;

    // Guardar resultado VARK
    @PostMapping
    public VarkResultResponse saveResult(
            @Valid @RequestBody SaveVarkResultRequest request
    ) {

        return varkResultService.saveResult(request);
    }

    // Obtener mi resultado
    @GetMapping("/me")
    public VarkResultResponse getMyResult() {

        return varkResultService.getMyResult();
    }

}