package com.oneil.statelab.repository;

import com.oneil.statelab.domain.WorkOrder;
import com.oneil.statelab.domain.WorkOrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {
    List<WorkOrder> findByState(WorkOrderState state);

    @org.springframework.data.jpa.repository.Query("SELECT w.state, COUNT(w) FROM WorkOrder w GROUP BY w.state")
    List<Object[]> countByState();

    @org.springframework.data.jpa.repository.Query("SELECT w.state, MIN(w.createdAt) FROM WorkOrder w GROUP BY w.state")
    List<Object[]> oldestCreatedAtByState();
}
