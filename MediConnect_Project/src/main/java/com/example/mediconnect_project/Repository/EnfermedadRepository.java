package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.EnfermedadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnfermedadRepository extends JpaRepository<EnfermedadEntity, Long> {

    @Query("SELECT e FROM EnfermedadEntity e WHERE e.estado_enfermedad = 1 ORDER BY e.nombre_enfermedad")
    List<EnfermedadEntity> listar_activas();

    @Query("SELECT COUNT(e) > 0 FROM EnfermedadEntity e WHERE LOWER(e.nombre_enfermedad) = LOWER(:nombre) AND e.estado_enfermedad = 1")
    boolean existe_nombre(@Param("nombre") String nombre);
}
