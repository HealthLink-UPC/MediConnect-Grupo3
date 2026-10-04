package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.AuditoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaEntity, Long> {

    @Query("SELECT a FROM AuditoriaEntity a WHERE a.tabla_afectada = :tabla AND a.id_registro = :idRegistro")
    Optional<AuditoriaEntity> buscar_registro(@Param("tabla") String tabla, @Param("idRegistro") Long idRegistro);

    @Query("SELECT a FROM AuditoriaEntity a ORDER BY a.id_auditoria")
    List<AuditoriaEntity> listar_todo();
}
