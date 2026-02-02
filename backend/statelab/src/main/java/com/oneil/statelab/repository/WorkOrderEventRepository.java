package com.oneil.statelab.repository;

import com.oneil.statelab.domain.WorkOrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkOrderEventRepository extends JpaRepository<WorkOrderEvent, UUID> {
    List<WorkOrderEvent> findByWorkOrderIdOrderByOccurredAtDesc(UUID workOrderId);
}
