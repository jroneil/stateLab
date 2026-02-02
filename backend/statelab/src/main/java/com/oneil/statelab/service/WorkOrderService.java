package com.oneil.statelab.service;

import com.oneil.statelab.domain.WorkOrder;
import com.oneil.statelab.domain.WorkOrderEvent;
import com.oneil.statelab.domain.WorkOrderState;
import com.oneil.statelab.repository.WorkOrderEventRepository;
import com.oneil.statelab.repository.WorkOrderRepository;
import com.oneil.statelab.state.WorkOrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneil.statelab.dto.DashboardStatsResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderService {

    private final WorkOrderRepository repository;
    private final WorkOrderEventRepository eventRepository;
    private final WorkOrderStateMachine stateMachine;

    @Transactional
    public WorkOrder create(String title, String description) {
        WorkOrder workOrder = WorkOrder.builder()
                .title(title)
                .description(description)
                .state(WorkOrderState.DRAFT)
                .build();

        WorkOrder saved = repository.save(workOrder);

        logEvent(saved.getId(), "CREATE", null, WorkOrderState.DRAFT, "Created");
        return saved;
    }

    public List<WorkOrder> list(WorkOrderState state) {
        if (state != null) {
            return repository.findByState(state);
        }
        return repository.findAll();
    }

    public WorkOrder findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkOrder not found: " + id));
    }

    @Transactional
    public WorkOrder transition(UUID id, WorkOrderStateMachine.Action action, String notes) {
        WorkOrder workOrder = findById(id);

        WorkOrderState fromState = workOrder.getState();

        // 1. Validate guard rules
        stateMachine.validateGuard(workOrder, action, notes);

        // 2. Determine next state
        WorkOrderState toState = stateMachine.getNextState(fromState, action);

        // 3. Apply state
        workOrder.setState(toState);
        WorkOrder updated = repository.save(workOrder);

        // 4. Log audit event
        logEvent(id, action.name(), fromState, toState, notes);

        return updated;
    }

    public List<WorkOrderEvent> getHistory(UUID id) {
        return eventRepository.findByWorkOrderIdOrderByOccurredAtDesc(id);
    }

    public DashboardStatsResponse getDashboardStats() {
        long total = repository.count();
        Map<WorkOrderState, Long> countsByState = repository.countByState().stream()
                .collect(Collectors.toMap(
                        row -> (WorkOrderState) row[0],
                        row -> (Long) row[1]));

        Map<WorkOrderState, java.time.LocalDateTime> oldestCreatedAtByState = repository.oldestCreatedAtByState()
                .stream()
                .collect(Collectors.toMap(
                        row -> (WorkOrderState) row[0],
                        row -> (java.time.LocalDateTime) row[1]));

        return DashboardStatsResponse.builder()
                .total(total)
                .countsByState(countsByState)
                .oldestCreatedAtByState(oldestCreatedAtByState)
                .build();
    }

    private void logEvent(UUID workOrderId, String action, WorkOrderState from, WorkOrderState to, String notes) {
        WorkOrderEvent event = WorkOrderEvent.builder()
                .workOrderId(workOrderId)
                .action(action)
                .fromState(from)
                .toState(to)
                .notes(notes)
                .build();
        eventRepository.save(event);
    }
}
