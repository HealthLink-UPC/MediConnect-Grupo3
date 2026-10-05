package com.example.mediconnect_project.Service;

import com.example.mediconnect_project.Dto.DatosClinicosDTO;
import com.example.mediconnect_project.Dto.FichaClinicaDto;
import com.example.mediconnect_project.Dto.PacienteAlergiaDTO;
import com.example.mediconnect_project.Dto.PacienteEnfermedadDTO;

public interface PacienteService {

    FichaClinicaDto guardar_datos_clinicos(Long idPaciente, DatosClinicosDTO datos);

    FichaClinicaDto agregar_alergia(Long idPaciente, PacienteAlergiaDTO alergia);

    FichaClinicaDto agregar_enfermedad(Long idPaciente, PacienteEnfermedadDTO enfermedad);

    // Baja lógica: el registro de la ficha queda inactivo y se audita la eliminación
    FichaClinicaDto quitar_alergia(Long idPaciente, Long idAlergia);

    FichaClinicaDto quitar_enfermedad(Long idPaciente, Long idEnfermedad);

    FichaClinicaDto obtener_ficha(Long idPaciente);
}
