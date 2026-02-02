package com.oneil.statelab.state;

import com.oneil.statelab.domain.WorkOrder;
import com.oneil.statelab.domain.WorkOrderState;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class WorkOrderStateMachine {

    public enum Action {
        SUBMIT,
        APPROVE,
        REJECT,
        COMPLETE,
        REVISE
    }

    public List<Action> getAllowedActions(WorkOrderState state) {
        return switch (state) {
            case DRAFT -> List.of(Action.SUBMIT);
            case SUBMITTED -> List.of(Action.APPROVE, Action.REJECT);
            case APPROVED -> List.of(Action.COMPLETE);
            case REJECTED -> List.of(Action.REVISE);
            case COMPLETED -> Collections.emptyList();
        };
    }

    public WorkOrderState getNextState(WorkOrderState currentState, Action action) {
        return switch (currentState) {
            case DRAFT -> {
                if (action == Action.SUBMIT)
                    yield WorkOrderState.SUBMITTED;
                throw invalidTransition(currentState, action);
            }
            case SUBMITTED -> {
                if (action == Action.APPROVE)
                    yield WorkOrderState.APPROVED;
                if (action == Action.REJECT)
                    yield WorkOrderState.REJECTED;
                throw invalidTransition(currentState, action);
            }
            case APPROVED -> {
                if (action == Action.COMPLETE)
                    yield WorkOrderState.COMPLETED;
                throw invalidTransition(currentState, action);
            }
            case REJECTED -> {
                if (action == Action.REVISE)
                    yield WorkOrderState.DRAFT;
                throw invalidTransition(currentState, action);
            }
            case COMPLETED -> throw invalidTransition(currentState, action);
        };
    }

    public void validateGuard(WorkOrder workOrder, Action action, String notes) {
        switch (action) {
            case SUBMIT -> {
                if (workOrder.getTitle() == null || workOrder.getTitle().isBlank()) {
                    throw new IllegalStateException("Title must be provided to submit");
                }
            }
            case REJECT -> {
                if (notes == null || notes.isBlank()) {
                    throw new IllegalStateException("Notes are required when rejecting");
                }
            }
        }
    }

    private RuntimeException invalidTransition(WorkOrderState state, Action action) {
        return new IllegalStateException("Cannot perform action " + action + " on state " + state);
    }
}
