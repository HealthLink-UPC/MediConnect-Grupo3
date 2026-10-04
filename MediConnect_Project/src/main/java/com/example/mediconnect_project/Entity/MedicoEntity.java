package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tm_medico", schema = "mediconnect")
public class MedicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medico")
    private Long id_medico;

    @Column(name = "especialidad")
    private String especialidad;

    @Column(name = "cmp")
    private String cmp;

    @Column(name = "centro_atencion")
    private String centro_atencion;

    @Column(name = "estado_medico")
    private Integer estado_medico;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private UsuarioEntity usuario;
}
