package com.oneil.statelab.dto;

import com.oneil.statelab.domain.WorkOrderState;
import com.oneil.statelab.state.WorkOrderStateMachine;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WorkOrderResponse {
    private UUID id;
    private String title;
    private String description;
    private WorkOrderState state;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WorkOrderStateMachine.Action> allowedActions;
}
