package com.example.mediconnect_project.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AprobacionDTO {

    @Valid
    @NotEmpty(message = "Debe indicar al menos un medicamento para emitir la receta")
    private List<ItemRecetaDTO> detalles = new ArrayList<>();
}
