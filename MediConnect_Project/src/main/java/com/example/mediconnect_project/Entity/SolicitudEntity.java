package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "tm_solicitud", schema = "mediconnect")
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id_solicitud;

    @Column(name = "tipo_solicitud")
    private String tipo_solicitud;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "fecha_solicitud")
    private LocalDate fecha_solicitud;

    @Column(name = "motivo_estado")
    private String motivo_estado;

    @Column(name = "estado_solicitud")
    private Integer estado_solicitud;

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id_paciente")
    private PacienteEntity paciente;

    @ManyToOne
    @JoinColumn(name = "id_medico", referencedColumnName = "id_medico")
    private MedicoEntity medico;

    @ManyToOne
    @JoinColumn(name = "id_receta", referencedColumnName = "id_receta")
    private RecetaEntity receta;
}
