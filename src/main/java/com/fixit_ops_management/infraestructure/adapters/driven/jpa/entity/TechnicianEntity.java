package com.fixit_ops_management.infraestructure.adapters.driven.jpa.entity;
import com.fixit_ops_management.domain.enums.TechnicianCategory;
import com.fixit_ops_management.domain.enums.TechnicianStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "technicians")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dni;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechnicianCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechnicianStatus status;

    @Column(name = "task_count", nullable = false)
    private Integer taskCount;

    @Column(name = "current_points", nullable = false)
    private Integer currentPoints;
}