package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Long> {

    @Query("SELECT r FROM RolEntity r WHERE r.nombre_rol = :nombre")
    Optional<RolEntity> buscar_por_nombre(@Param("nombre") String nombre);

    @Query("SELECT r FROM RolEntity r WHERE r.estado_rol = 1 AND r.nombre_rol IN :nombres ORDER BY r.id_rol")
    List<RolEntity> listar_activos_por_nombres(@Param("nombres") Collection<String> nombres);
}
