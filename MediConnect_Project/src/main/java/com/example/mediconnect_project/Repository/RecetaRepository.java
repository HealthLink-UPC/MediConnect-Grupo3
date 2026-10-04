package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.RecetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetaRepository extends JpaRepository<RecetaEntity, Long> {

    @Query("SELECT r FROM RecetaEntity r WHERE r.paciente.id_paciente = :idPaciente ORDER BY r.fecha_emision DESC, r.id_receta DESC")
    List<RecetaEntity> listar_por_paciente(@Param("idPaciente") Long idPaciente);

    @Query("SELECT COUNT(r) > 0 FROM RecetaEntity r WHERE r.codigo_verificacion = :codigo")
    boolean existe_codigo(@Param("codigo") String codigo);
}
