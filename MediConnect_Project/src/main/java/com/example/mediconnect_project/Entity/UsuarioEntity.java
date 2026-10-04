package com.example.mediconnect_project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name="tm_usuario",schema = "mediconnect")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id_usuario;

    @Column(name = "correo_usuario")
    private String correo_usuario;

    @Column(name = "clave_usuario")
    private String clave_usuario;

    @Column(name = "nombre_usuario")
    private String nombre_usuario;

    @Column(name = "apellido_usuario")
    private String apellido_usuario;

    @Column(name = "dni")
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate fecha_nacimiento;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "estado_usuario")
    private Integer estado_usuario;

    @ManyToOne
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    private RolEntity rol;
}
