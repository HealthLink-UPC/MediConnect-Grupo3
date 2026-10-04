package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="tm_enfermedad",schema = "mediconnect")
public class EnfermedadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enfermedad")
    private Long id_enfermedad;

    @Column(name="nombre_enfermedad")
    private String nombre_enfermedad;

    @Column(name="estado_enfermedad")
    private Integer estado_enfermedad;

}
