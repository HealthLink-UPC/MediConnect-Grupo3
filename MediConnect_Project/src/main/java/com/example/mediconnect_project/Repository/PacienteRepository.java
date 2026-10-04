package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {

    @Query("SELECT p FROM PacienteEntity p WHERE p.usuario.id_usuario = :idUsuario")
    Optional<PacienteEntity> buscar_por_usuario(@Param("idUsuario") Long idUsuario);
}
