package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.AlergiaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlergiaRepository extends JpaRepository<AlergiaEntity, Long> {

    @Query("SELECT a FROM AlergiaEntity a WHERE a.estado_alergia = 1 ORDER BY a.nombre_alergia")
    List<AlergiaEntity> listar_activas();

    @Query("SELECT COUNT(a) > 0 FROM AlergiaEntity a WHERE LOWER(a.nombre_alergia) = LOWER(:nombre) AND a.estado_alergia = 1")
    boolean existe_nombre(@Param("nombre") String nombre);
}
