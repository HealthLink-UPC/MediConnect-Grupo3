package com.example.mediconnect_project.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tm_paciente", schema = "mediconnect")
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long id_paciente;

    @Column(name = "tipo_sangre")
    private String tipo_sangre;

    @Column(name = "peso")
    private Double peso;

    @Column(name = "talla")
    private Double talla;

    @Column(name = "estado_paciente")
    private Integer estado_paciente;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private UsuarioEntity usuario;
}
