package com.elearning.platform.dto.request;

import com.elearning.platform.enums.ContentType;
import com.elearning.platform.enums.LearningStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateContentRequest {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "La URL es obligatoria")
    private String url;

    @NotNull(message = "El tipo de contenido es obligatorio")
    private ContentType type;

    @NotNull(message = "El estilo de aprendizaje es obligatorio")
    private LearningStyle learningStyle;

}