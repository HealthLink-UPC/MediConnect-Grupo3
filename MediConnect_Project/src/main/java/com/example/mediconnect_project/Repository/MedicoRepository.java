package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.MedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoEntity, Long> {

    @Query("SELECT m FROM MedicoEntity m WHERE m.usuario.id_usuario = :idUsuario")
    Optional<MedicoEntity> buscar_por_usuario(@Param("idUsuario") Long idUsuario);

    @Query("SELECT COUNT(m) > 0 FROM MedicoEntity m WHERE m.cmp = :cmp")
    boolean existe_cmp(@Param("cmp") String cmp);

    @Query("SELECT m FROM MedicoEntity m WHERE m.estado_medico = 1 AND m.usuario.estado_usuario = 1 ORDER BY m.id_medico")
    List<MedicoEntity> listar_activos();
}
