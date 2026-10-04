package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "tm_tratamiento", schema = "mediconnect")
public class TratamientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tratamiento")
    private Long id_tratamiento;

    @Column(name = "dosis")
    private String dosis;

    @Column(name = "frecuencia")
    private String frecuencia;

    @Column(name = "fecha_inicio")
    private LocalDate fecha_inicio;

    @Column(name = "fecha_fin")
    private LocalDate fecha_fin;

    @Column(name = "estado_tratamiento")
    private Integer estado_tratamiento;

    @ManyToOne
    @JoinColumn(name = "id_receta", referencedColumnName = "id_receta")
    private RecetaEntity receta;

    @ManyToOne
    @JoinColumn(name = "id_medicamento", referencedColumnName = "id_medicamento")
    private MedicamentoEntity medicamento;
}
