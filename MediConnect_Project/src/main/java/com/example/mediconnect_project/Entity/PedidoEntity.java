package com.example.mediconnect_project.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tm_pedido", schema = "mediconnect")
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id_pedido;

    @Column(name = "estado_pedido")
    private Integer estado_pedido;

    @ManyToOne
    @JoinColumn(name = "id_solicitud", referencedColumnName = "id_solicitud")
    private SolicitudEntity solicitud;

    @ManyToOne
    @JoinColumn(name = "id_medicamento", referencedColumnName = "id_medicamento")
    private MedicamentoEntity medicamento;


}
