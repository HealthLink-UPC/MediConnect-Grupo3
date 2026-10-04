package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.SensibilidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensibilidadRepository extends JpaRepository<SensibilidadEntity, Long> {

    @Query("SELECT s FROM SensibilidadEntity s WHERE s.paciente.id_paciente = :idPaciente AND s.estado_sensibilidad = 1 ORDER BY s.id_sensibilidad")
    List<SensibilidadEntity> listar_activas_por_paciente(@Param("idPaciente") Long idPaciente);

    @Query("SELECT s FROM SensibilidadEntity s WHERE s.paciente.id_paciente = :idPaciente AND s.alergia.id_alergia = :idAlergia")
    Optional<SensibilidadEntity> buscar_registro(@Param("idPaciente") Long idPaciente, @Param("idAlergia") Long idAlergia);
}
