package com.example.mediconnect_project.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="tm_alergia",schema = "mediconnect")
public class AlergiaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alergia")
    private Long id_alergia;

    @Column(name = "nombre_alergia")
    private String nombre_alergia;

    @Column(name = "estado_alergia")
    private Integer estado_alergia;
}
