package com.oneil.statelab.dto;

import com.oneil.statelab.domain.WorkOrderState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long total;
    private Map<WorkOrderState, Long> countsByState;
    private Map<WorkOrderState, java.time.LocalDateTime> oldestCreatedAtByState;
}
