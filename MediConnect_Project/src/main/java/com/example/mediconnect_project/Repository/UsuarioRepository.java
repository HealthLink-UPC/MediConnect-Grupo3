package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @Query("SELECT u FROM UsuarioEntity u WHERE u.correo_usuario = :correo")
    UsuarioEntity buscar_por_correo(@Param("correo") String correo);

    @Query("SELECT COUNT(u) > 0 FROM UsuarioEntity u WHERE u.correo_usuario = :correo")
    boolean existe_correo(@Param("correo") String correo);

    @Query("SELECT COUNT(u) > 0 FROM UsuarioEntity u WHERE u.correo_usuario = :correo AND u.id_usuario <> :idUsuario")
    boolean existe_correo_de_otro(@Param("correo") String correo, @Param("idUsuario") Long idUsuario);

    @Query("SELECT COUNT(u) > 0 FROM UsuarioEntity u WHERE u.dni = :dni")
    boolean existe_dni(@Param("dni") String dni);
}
