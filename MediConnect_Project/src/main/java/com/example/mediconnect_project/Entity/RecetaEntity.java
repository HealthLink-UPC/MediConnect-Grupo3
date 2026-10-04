package com.example.mediconnect_project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "tm_receta", schema = "mediconnect")
public class RecetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Long id_receta;

    @Column(name = "fecha_emision")
    private LocalDate fecha_emision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fecha_vencimiento;

    @Column(name = "codigo_verificacion")
    private String codigo_verificacion;

    @Column(name = "estado_receta")
    private Integer estado_receta;

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id_paciente")
    private PacienteEntity paciente;

    @ManyToOne
    @JoinColumn(name = "id_medico", referencedColumnName = "id_medico")
    private MedicoEntity medico;
}
