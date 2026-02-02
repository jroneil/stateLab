package com.oneil.statelab.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWorkOrderRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
}
