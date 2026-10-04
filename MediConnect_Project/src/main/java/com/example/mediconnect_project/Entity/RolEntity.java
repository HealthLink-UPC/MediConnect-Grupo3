package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="tm_rol",schema = "mediconnect")
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_rol")
    private Long id_rol;

    @Column(name = "nombre_rol")
    private String nombre_rol;

    @Column(name = "estado_rol")
    private Integer estado_rol;
}
