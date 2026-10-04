package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.MedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<MedicamentoEntity, Long> {

    @Query("SELECT m FROM MedicamentoEntity m WHERE m.estado_medicamento = 1 ORDER BY m.nombre_medicamento")
    List<MedicamentoEntity> listar_activos();

    @Query("SELECT COUNT(m) > 0 FROM MedicamentoEntity m WHERE LOWER(m.nombre_medicamento) = LOWER(:nombre) "
            + "AND LOWER(m.concentracion) = LOWER(:concentracion) AND m.estado_medicamento = 1")
    boolean existe_medicamento(@Param("nombre") String nombre, @Param("concentracion") String concentracion);
}
