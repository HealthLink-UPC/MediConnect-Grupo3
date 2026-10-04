package com.example.mediconnect_project.Repository;

import com.example.mediconnect_project.Entity.TratamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TratamientoRepository extends JpaRepository<TratamientoEntity, Long> {

    @Query("SELECT t FROM TratamientoEntity t WHERE t.receta.id_receta = :idReceta AND t.estado_tratamiento = 1 ORDER BY t.id_tratamiento")
    List<TratamientoEntity> listar_por_receta(@Param("idReceta") Long idReceta);
}
