package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    @Query("SELECT p FROM PedidoEntity p WHERE p.solicitud.id_solicitud = :idSolicitud AND p.estado_pedido = 1 ORDER BY p.id_pedido")
    List<PedidoEntity> listar_por_solicitud(@Param("idSolicitud") Long idSolicitud);
}
