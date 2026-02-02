package com.oneil.statelab.controller;

import com.oneil.statelab.domain.WorkOrder;
import com.oneil.statelab.domain.WorkOrderEvent;
import com.oneil.statelab.domain.WorkOrderState;
import com.oneil.statelab.dto.DashboardStatsResponse;
import com.oneil.statelab.dto.CreateWorkOrderRequest;
import com.oneil.statelab.dto.TransitionRequest;
import com.oneil.statelab.dto.WorkOrderResponse;
import com.oneil.statelab.service.WorkOrderService;
import com.oneil.statelab.state.WorkOrderStateMachine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "ETag")
public class WorkOrderController {

    private final WorkOrderService service;
    private final WorkOrderStateMachine stateMachine;

    @PostMapping
    public ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody CreateWorkOrderRequest request) {
        WorkOrder wo = service.create(request.getTitle(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED)
                .eTag(wo.getVersion().toString())
                .body(toResponse(wo));
    }

    @GetMapping
    public List<WorkOrderResponse> list(@RequestParam(required = false) WorkOrderState state) {
        return service.list(state).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> getById(@PathVariable UUID id, WebRequest request) {
        WorkOrder wo = service.findById(id);

        String etag = wo.getVersion().toString();
        if (request.checkNotModified(etag)) {
            return null;
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .body(toResponse(wo));
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<WorkOrderResponse> transition(
            @PathVariable UUID id,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @Valid @RequestBody TransitionRequest request) {

        WorkOrder current = service.findById(id);

        // Optimistic locking check via ETag/If-Match
        if (ifMatch != null) {
            String currentEtag = "\"" + current.getVersion() + "\"";
            String providedEtag = ifMatch.replace("W/", "");
            if (!providedEtag.equals(currentEtag) && !providedEtag.equals(current.getVersion().toString())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        WorkOrder wo = service.transition(id, request.getAction(), request.getNotes());
        return ResponseEntity.ok()
                .eTag(wo.getVersion().toString())
                .body(toResponse(wo));
    }

    @GetMapping("/{id}/events")
    public List<WorkOrderEvent> getEvents(@PathVariable UUID id) {
        return service.getHistory(id);
    }

    @GetMapping("/stats")
    public DashboardStatsResponse getStats() {
        return service.getDashboardStats();
    }

    private WorkOrderResponse toResponse(WorkOrder wo) {
        return WorkOrderResponse.builder()
                .id(wo.getId())
                .title(wo.getTitle())
                .description(wo.getDescription())
                .state(wo.getState())
                .version(wo.getVersion())
                .createdAt(wo.getCreatedAt())
                .updatedAt(wo.getUpdatedAt())
                .allowedActions(stateMachine.getAllowedActions(wo.getState()))
                .build();
    }
}
