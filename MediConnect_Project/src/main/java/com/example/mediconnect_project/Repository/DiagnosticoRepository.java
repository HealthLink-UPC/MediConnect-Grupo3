package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.DiagnosticoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosticoRepository extends JpaRepository<DiagnosticoEntity, Long> {

    @Query("SELECT d FROM DiagnosticoEntity d WHERE d.paciente.id_paciente = :idPaciente AND d.estado_diagnostico = 1 ORDER BY d.id_diagnostico")
    List<DiagnosticoEntity> listar_activos_por_paciente(@Param("idPaciente") Long idPaciente);

    @Query("SELECT d FROM DiagnosticoEntity d WHERE d.paciente.id_paciente = :idPaciente AND d.enfermedad.id_enfermedad = :idEnfermedad")
    Optional<DiagnosticoEntity> buscar_registro(@Param("idPaciente") Long idPaciente, @Param("idEnfermedad") Long idEnfermedad);
}
