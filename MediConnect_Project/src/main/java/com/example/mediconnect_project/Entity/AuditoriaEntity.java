package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tm_auditoria", schema = "mediconnect")
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long id_auditoria;

    @Column(name = "id_registro")
    private Long id_registro;

    @Column(name = "fecha_creacion")
    private LocalDateTime fecha_creacion;

    @Column(name = "fecha_edicion")
    private LocalDateTime fecha_edicion;

    @Column(name = "fecha_eliminacion")
    private LocalDateTime fecha_eliminacion;

    @Column(name = "tabla_afectada")
    private String tabla_afectada;

    @Column(name = "estado_auditoria")
    private Integer estado_auditoria;

    @ManyToOne
    @JoinColumn(name = "id_creacion", referencedColumnName = "id_usuario")
    private UsuarioEntity creacion;

    @ManyToOne
    @JoinColumn(name = "id_edicion", referencedColumnName = "id_usuario")
    private UsuarioEntity edicion;

    @ManyToOne
    @JoinColumn(name = "id_eliminacion", referencedColumnName = "id_usuario")
    private UsuarioEntity eliminacion;
}

