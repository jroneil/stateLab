package com.oneil.statelab.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_order_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID workOrderId;

    @Enumerated(EnumType.STRING)
    private WorkOrderState fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderState toState;

    private String notes;

    private String action;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime occurredAt;
}
