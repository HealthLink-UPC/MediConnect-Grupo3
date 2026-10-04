package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.SolicitudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<SolicitudEntity, Long> {

    @Query("SELECT COUNT(s) > 0 FROM SolicitudEntity s WHERE s.receta.id_receta = :idReceta "
            + "AND s.tipo_solicitud = :tipo AND s.estado_solicitud IN :estados")
    boolean existe_solicitud_abierta_para_receta(@Param("idReceta") Long idReceta, @Param("tipo") String tipo,
                                                 @Param("estados") Collection<Integer> estados);

    @Query("SELECT s FROM SolicitudEntity s WHERE s.paciente.id_paciente = :idPaciente "
            + "ORDER BY s.fecha_solicitud DESC, s.id_solicitud DESC")
    List<SolicitudEntity> listar_por_paciente(@Param("idPaciente") Long idPaciente);

    @Query("SELECT s FROM SolicitudEntity s WHERE s.medico.id_medico = :idMedico "
            + "ORDER BY s.fecha_solicitud DESC, s.id_solicitud DESC")
    List<SolicitudEntity> listar_por_medico(@Param("idMedico") Long idMedico);

    @Query("SELECT s FROM SolicitudEntity s WHERE s.medico.id_medico = :idMedico AND s.estado_solicitud = :estado "
            + "ORDER BY s.fecha_solicitud DESC, s.id_solicitud DESC")
    List<SolicitudEntity> listar_por_medico_y_estado(@Param("idMedico") Long idMedico, @Param("estado") Integer estado);

    @Query("SELECT s FROM SolicitudEntity s WHERE s.medico.id_medico = :idMedico AND s.estado_solicitud IN :estados "
            + "ORDER BY s.fecha_solicitud ASC, s.id_solicitud ASC")
    List<SolicitudEntity> listar_bandeja_medico(@Param("idMedico") Long idMedico, @Param("estados") Collection<Integer> estados);
}
