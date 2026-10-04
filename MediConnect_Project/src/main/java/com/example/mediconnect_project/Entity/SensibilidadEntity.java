package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tm_sensibilidad", schema = "mediconnect")
public class SensibilidadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sensibilidad")
    private Long id_sensibilidad;

    @Column(name = "estado_sensibilidad")
    private Integer estado_sensibilidad;

    @ManyToOne
    @JoinColumn(name = "id_paciente", referencedColumnName = "id_paciente")
    private PacienteEntity paciente;

    @ManyToOne
    @JoinColumn(name = "id_alergia", referencedColumnName = "id_alergia")
    private AlergiaEntity alergia;
}
