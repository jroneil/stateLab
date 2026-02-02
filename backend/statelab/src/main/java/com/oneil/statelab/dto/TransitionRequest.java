package com.oneil.statelab.dto;

import com.oneil.statelab.state.WorkOrderStateMachine;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransitionRequest {
    @NotNull(message = "Action is required")
    private WorkOrderStateMachine.Action action;
    private String notes;
}
