package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="tm_medicamento",schema = "mediconnect")
public class MedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medicamento")
    private Long id_medicamento;

    @Column(name = "nombre_medicamento")
    private String nombre_medicamento;

    @Column(name = "concentracion")
    private String concentracion;

    @Column(name = "estado_medicamento")
    private Integer estado_medicamento;
}
