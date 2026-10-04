package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tm_diagnostico", schema = "mediconnect")
public class DiagnosticoEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diagnostico")
    private Long id_diagnostico;

    @Column(name = "estado_diagnostico")
    private Integer estado_diagnostico;

    @ManyToOne
    @JoinColumn(name = "id_enfermedad", referencedColumnName = "id_enfermedad")
    private EnfermedadEntity enfermedad;

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id_paciente")
    private PacienteEntity paciente;
}
