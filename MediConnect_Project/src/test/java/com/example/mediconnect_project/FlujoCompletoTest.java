package com.example.mediconnect_project;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba el flujo completo de MediConnect contra la base de datos real, con token JWT y permisos por rol.
 * Es @Transactional: todo se revierte al terminar cada prueba y no queda ningún dato.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FlujoCompletoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    // Tablas cuyos registros deben quedar en la auditoría (tm_rol se crea al arrancar, sin usuario)
    private static final List<String> TABLAS_AUDITADAS = List.of(
            "tm_usuario", "tm_medico", "tm_paciente", "tm_enfermedad", "tm_alergia", "tm_medicamento",
            "tm_diagnostico", "tm_sensibilidad", "tm_receta", "tm_solicitud", "tm_pedido", "tm_tratamiento");

    private final String sufijo = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));

    @Test
    void flujoCompleto_registro_solicitud_aprobacion_rechazo_y_permisos() throws Exception {
        Map<String, Long> registrosAntes = contarRegistros();
        Map<String, Long> auditoriaAntes = contarAuditoria();

        // ---- Roles disponibles para el registro (públicos; se crean al arrancar la aplicación)
        mockMvc.perform(get("/roles/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // ---- US01: registro (público) con id_rol
        // Paciente con datos incompletos -> 400
        enviar(post("/auth/register/paciente"), null, "{\"correo_usuario\":\"y" + sufijo + "@test.pe\",\"clave_usuario\":\"clave123\"}", 400);
        // Médico sin especialidad, CMP ni centro de atención -> 400
        enviar(post("/auth/register/medico"), null, registro("z" + sufijo + "@test.pe", "5" + sufijo + "00", "96" + sufijo + "00", ""), 400);
        // El endpoint anterior que mezclaba ambos ya no existe
        enviar(post("/auth/register"), null, registro("w" + sufijo + "@test.pe", "8" + sufijo + "00", "99" + sufijo + "00", ""), 401);

        String datosMedico = ",\"especialidad\":\"Cardiología\",\"cmp\":\"C" + sufijo + "\",\"centro_atencion\":\"Clínica Test\"";
        enviar(post("/auth/register/medico"), null, registro("medico" + sufijo + "@test.pe", "1" + sufijo + "00", "91" + sufijo + "00", datosMedico), 200);
        enviar(post("/auth/register/paciente"), null, registro("paciente" + sufijo + "@test.pe", "2" + sufijo + "00", "92" + sufijo + "00", ""), 200);
        enviar(post("/auth/register/paciente"), null, registro("paciente2" + sufijo + "@test.pe", "6" + sufijo + "00", "97" + sufijo + "00", ""), 200);
        enviar(post("/auth/register/medico"), null, registro("medico2" + sufijo + "@test.pe", "7" + sufijo + "00", "98" + sufijo + "00",
                ",\"especialidad\":\"Geriatría\",\"cmp\":\"D" + sufijo + "\",\"centro_atencion\":\"Clínica Test\""), 200);

        // Correo y CMP repetidos -> error claro
        enviar(post("/auth/register/paciente"), null, registro("paciente" + sufijo + "@test.pe", "3" + sufijo + "00", "93" + sufijo + "00", ""), 400);
        enviar(post("/auth/register/medico"), null, registro("otro" + sufijo + "@test.pe", "4" + sufijo + "00", "94" + sufijo + "00", datosMedico), 400);

        // ---- US02: login (público) -> token con usuario, rol y perfil
        MvcResult loginMedico = login("medico" + sufijo + "@test.pe");
        MvcResult loginMedico2 = login("medico2" + sufijo + "@test.pe");
        MvcResult loginPaciente = login("paciente" + sufijo + "@test.pe");
        MvcResult loginPaciente2 = login("paciente2" + sufijo + "@test.pe");

        int idMedico = campo(loginMedico, "$.id_medico");
        int idUsuarioMedico = campo(loginMedico, "$.id_usuario");
        int idPaciente = campo(loginPaciente, "$.id_paciente");
        int idUsuarioPaciente = campo(loginPaciente, "$.id_usuario");
        int idPaciente2 = campo(loginPaciente2, "$.id_paciente");
        int idUsuarioPaciente2 = campo(loginPaciente2, "$.id_usuario");

        String tokenMedico = texto(loginMedico, "$.token");
        String tokenMedico2 = texto(loginMedico2, "$.token");
        String tokenPaciente = texto(loginPaciente, "$.token");
        String tokenPaciente2 = texto(loginPaciente2, "$.token");
        assertTrue(tokenPaciente.split("\\.").length == 3, "El login debe devolver un JWT");

        // Clave incorrecta -> 401
        enviar(post("/auth/login"), null,
                "{\"correo_usuario\":\"paciente" + sufijo + "@test.pe\",\"clave_usuario\":\"incorrecta\"}", 401);

        // ---- Token obligatorio: sin token o con token inválido -> 401; Swagger sigue público
        mockMvc.perform(get("/medicos/listar")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/catalogo/alergias").header("Authorization", "Bearer abc.def.ghi")).andExpect(status().isUnauthorized());
        String swagger = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Swagger muestra para el paciente solo sus datos y para el médico, además, los profesionales
        java.util.Map<?, ?> camposPaciente = JsonPath.read(swagger, "$.components.schemas.RegistroPacienteDTO.properties");
        java.util.Map<?, ?> camposMedico = JsonPath.read(swagger, "$.components.schemas.RegistroMedicoDTO.properties");
        assertTrue(camposPaciente.containsKey("correo_usuario") && !camposPaciente.containsKey("especialidad")
                && !camposPaciente.containsKey("cmp") && !camposPaciente.containsKey("id_rol"),
                "El registro del paciente no debe mostrar campos de médico ni el rol");
        assertTrue(camposMedico.containsKey("especialidad") && camposMedico.containsKey("cmp")
                && camposMedico.containsKey("centro_atencion"), "El registro del médico debe pedir sus datos profesionales");

        mockMvc.perform(con(get("/medicos/listar"), tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // ---- Catálogos: cualquier usuario con sesión puede cargarlos
        int idMedicamento = campo(enviar(post("/catalogo/medicamentos"), tokenMedico,
                "{\"nombre_medicamento\":\"Losartán TEST\",\"concentracion\":\"50 mg\"}", 200), "$.id_medicamento");
        int idAlergia = campo(enviar(post("/catalogo/alergias"), tokenPaciente,
                "{\"nombre_alergia\":\"Penicilina TEST\"}", 200), "$.id_alergia");
        int idEnfermedad = campo(enviar(post("/catalogo/enfermedades"), tokenPaciente,
                "{\"nombre_enfermedad\":\"Hipertensión TEST\"}", 200), "$.id_enfermedad");
        enviar(post("/catalogo/alergias"), tokenPaciente, "{\"nombre_alergia\":\"Penicilina TEST\"}", 400);

        // ---- US03: perfil (cada usuario solo el suyo)
        enviar(put("/usuarios/" + idUsuarioPaciente), tokenPaciente,
                "{\"correo_usuario\":\"paciente" + sufijo + "@test.pe\",\"telefono\":\"95" + sufijo + "00\"}", 200);
        enviar(put("/usuarios/" + idUsuarioPaciente), tokenPaciente,
                "{\"correo_usuario\":\"paciente" + sufijo + "@test.pe\",\"telefono\":\"\"}", 400);
        enviar(put("/usuarios/" + idUsuarioPaciente), tokenPaciente2,
                "{\"correo_usuario\":\"paciente" + sufijo + "@test.pe\",\"telefono\":\"95" + sufijo + "00\"}", 403);
        mockMvc.perform(con(get("/usuarios/" + idUsuarioPaciente), tokenPaciente2)).andExpect(status().isForbidden());
        mockMvc.perform(con(get("/usuarios/" + idUsuarioPaciente), tokenPaciente)).andExpect(status().isOk());

        // ---- US04 y US05: ficha clínica
        // Recién registrado: los datos clínicos aún no existen (la ficha no muestra el valor interno)
        String fichaVacia = mockMvc.perform(con(get("/pacientes/" + idPaciente + "/ficha"), tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("No se registran alergias reportadas"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(!fichaVacia.contains("SIN REGISTRAR"), "La ficha no debe mostrar el valor interno 'sin registrar'");

        String datosClinicos = "{\"tipo_sangre\":\"O+\",\"peso\":70.5,\"talla\":1.70}";
        enviar(put("/pacientes/" + idPaciente + "/datos-clinicos"), tokenPaciente2, datosClinicos, 403); // otro paciente
        enviar(put("/pacientes/" + idPaciente + "/datos-clinicos"), tokenMedico, datosClinicos, 403);    // un médico no edita la ficha
        enviar(put("/pacientes/" + idPaciente + "/datos-clinicos"), tokenPaciente, datosClinicos, 200);
        enviar(post("/pacientes/" + idPaciente + "/alergias"), tokenMedico, "{\"id_alergia\":" + idAlergia + "}", 403);
        enviar(post("/pacientes/" + idPaciente + "/alergias"), tokenPaciente, "{\"id_alergia\":" + idAlergia + "}", 200);
        enviar(post("/pacientes/" + idPaciente + "/enfermedades"), tokenPaciente, "{\"id_enfermedad\":" + idEnfermedad + "}", 200);
        // La misma alergia dos veces -> error claro (la base no permite repetirla)
        enviar(post("/pacientes/" + idPaciente + "/alergias"), tokenPaciente, "{\"id_alergia\":" + idAlergia + "}", 400);

        // La ficha la ve su paciente y cualquier médico; otro paciente no
        mockMvc.perform(con(get("/pacientes/" + idPaciente + "/ficha"), tokenPaciente2)).andExpect(status().isForbidden());
        String ficha = mockMvc.perform(con(get("/pacientes/" + idPaciente + "/ficha"), tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo_sangre").value("O+"))
                .andExpect(jsonPath("$.alergias[0]").value("Penicilina TEST"))
                .andExpect(jsonPath("$.enfermedades[0]").value("Hipertensión TEST"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(!ficha.contains("clave"), "La ficha no debe exponer la contraseña");

        // ---- US06 y US07: solicitud con medicamentos (el paciente sale del token)
        String nuevaSolicitud = "\"id_medico\":" + idMedico + ",\"id_medicamentos\":[" + idMedicamento + "," + idMedicamento + "]";
        enviar(post("/solicitudes/nueva-receta"), tokenMedico, "{" + nuevaSolicitud + "}", 403);             // un médico no solicita
        enviar(post("/solicitudes/nueva-receta"), tokenPaciente, "{\"id_medico\":" + idMedico + ",\"id_medicamentos\":[]}", 400);

        // Aunque el cuerpo traiga el id de otro paciente, la solicitud queda a nombre de quien tiene la sesión
        MvcResult creada = enviar(post("/solicitudes/nueva-receta"), tokenPaciente,
                "{\"id_paciente\":" + idPaciente2 + "," + nuevaSolicitud + "}", 200);
        int idSolicitud = campo(creada, "$.id_solicitud");
        assertEquals(idPaciente, campo(creada, "$.id_paciente"), "El paciente debe salir del token, no del cuerpo");

        // ---- US11 y US08
        mockMvc.perform(con(get("/solicitudes/paciente/" + idPaciente), tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado_solicitud").value(1))
                .andExpect(jsonPath("$[0].estado_nombre").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].tipo_solicitud").value("NUEVA_RECETA"))
                .andExpect(jsonPath("$[0].medicamentos.length()").value(1)); // el repetido se descarta
        mockMvc.perform(con(get("/solicitudes/paciente/" + idPaciente), tokenPaciente2)).andExpect(status().isForbidden());

        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "/pendientes"), tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_solicitud").value(idSolicitud));
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "/pendientes"), tokenMedico2)).andExpect(status().isForbidden());
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "/pendientes"), tokenPaciente)).andExpect(status().isForbidden());

        // Solo el médico asignado puede atenderla
        enviar(put("/solicitudes/" + idSolicitud + "/revision"), tokenPaciente, "", 403);
        enviar(put("/solicitudes/" + idSolicitud + "/revision"), tokenMedico2, "", 403);
        enviar(put("/solicitudes/" + idSolicitud + "/revision"), tokenMedico, "", 200);

        // ---- US09 y US10: aprobar y emitir receta
        String item = "{\"id_medicamento\":" + idMedicamento + ",\"dosis\":\"1 tableta\",\"frecuencia\":\"cada 12 horas\","
                + "\"fecha_inicio\":\"2026-10-01\",\"fecha_fin\":\"2026-12-31\"}";
        enviar(put("/solicitudes/" + idSolicitud + "/aprobar"), tokenPaciente, "{\"detalles\":[" + item + "]}", 403);
        enviar(put("/solicitudes/" + idSolicitud + "/aprobar"), tokenMedico2, "{\"detalles\":[" + item + "]}", 403);
        enviar(put("/solicitudes/" + idSolicitud + "/aprobar"), tokenMedico, "{\"detalles\":[]}", 400);
        enviar(put("/solicitudes/" + idSolicitud + "/aprobar"), tokenMedico, "{\"detalles\":[" + item + "," + item + "]}", 400);

        MvcResult aprobada = enviar(put("/solicitudes/" + idSolicitud + "/aprobar"), tokenMedico, "{\"detalles\":[" + item + "]}", 200);
        String codigo = texto(aprobada, "$.codigo_verificacion");
        int idReceta = campo(aprobada, "$.id_receta");
        assertEquals(10, codigo.length(), "El código de verificación debe tener 10 caracteres");

        // Aprobar de nuevo una solicitud ya atendida -> 400
        enviar(put("/solicitudes/" + idSolicitud + "/aprobar"), tokenMedico, "{\"detalles\":[" + item + "]}", 400);

        // La solicitud queda APROBADA y enlazada a su receta
        mockMvc.perform(con(get("/solicitudes/paciente/" + idPaciente), tokenPaciente))
                .andExpect(jsonPath("$[0].estado_solicitud").value(3))
                .andExpect(jsonPath("$[0].estado_nombre").value("APROBADA"))
                .andExpect(jsonPath("$[0].id_receta").value(idReceta));

        // ---- US13: historial y detalle (su paciente o un médico; otro paciente no)
        mockMvc.perform(con(get("/recetas/paciente/" + idPaciente), tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_receta").value(idReceta));
        mockMvc.perform(con(get("/recetas/paciente/" + idPaciente), tokenPaciente2)).andExpect(status().isForbidden());
        mockMvc.perform(con(get("/recetas/" + idReceta), tokenPaciente2)).andExpect(status().isForbidden());
        mockMvc.perform(con(get("/recetas/" + idReceta), tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles[0].nombre_medicamento").value("Losartán TEST"))
                .andExpect(jsonPath("$.estado_receta").value(1))
                .andExpect(jsonPath("$.estado_nombre").value("VIGENTE"));

        // ---- US12: rechazo con motivo obligatorio
        int idSolicitud2 = campo(enviar(post("/solicitudes/nueva-receta"), tokenPaciente,
                "{\"id_medico\":" + idMedico + ",\"motivo\":\"Se me acabó\",\"id_medicamentos\":[" + idMedicamento + "]}", 200),
                "$.id_solicitud");
        enviar(put("/solicitudes/" + idSolicitud2 + "/rechazar"), tokenMedico, "{\"motivo_rechazo\":\"\"}", 400);
        enviar(put("/solicitudes/" + idSolicitud2 + "/rechazar"), tokenPaciente, "{\"motivo_rechazo\":\"No me corresponde\"}", 403);
        enviar(put("/solicitudes/" + idSolicitud2 + "/rechazar"), tokenMedico, "{\"motivo_rechazo\":\"Requiere control presencial\"}", 200);
        mockMvc.perform(con(get("/solicitudes/paciente/" + idPaciente), tokenPaciente))
                .andExpect(jsonPath("$[?(@.id_solicitud==" + idSolicitud2 + ")].estado_solicitud").value(4))
                .andExpect(jsonPath("$[?(@.id_solicitud==" + idSolicitud2 + ")].motivo").value("Se me acabó"))
                .andExpect(jsonPath("$[?(@.id_solicitud==" + idSolicitud2 + ")].motivo_estado").value("Requiere control presencial"));

        // Ya no aparece en la bandeja del médico
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "/pendientes"), tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // ---- El médico también ve todas sus solicitudes (no solo las pendientes), con filtro opcional por estado
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico), tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id_solicitud==" + idSolicitud + ")].estado_nombre").value("APROBADA"))
                .andExpect(jsonPath("$[?(@.id_solicitud==" + idSolicitud2 + ")].estado_nombre").value("RECHAZADA"));
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "?estado=3"), tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id_solicitud").value(idSolicitud));
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "?estado=4"), tokenMedico))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id_solicitud").value(idSolicitud2));
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "?estado=1"), tokenMedico))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico + "?estado=9"), tokenMedico)).andExpect(status().isBadRequest());
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico), tokenMedico2)).andExpect(status().isForbidden());
        mockMvc.perform(con(get("/solicitudes/medico/" + idMedico), tokenPaciente)).andExpect(status().isForbidden());

        // ---- Renovación: se indica la receta a renovar; va al médico que la emitió con sus mismos medicamentos
        enviar(post("/solicitudes/renovacion"), tokenPaciente, "{}", 400);                                   // falta la receta
        enviar(post("/solicitudes/renovacion"), tokenPaciente, "{\"id_receta\":999999}", 404);
        enviar(post("/solicitudes/renovacion"), tokenPaciente2, "{\"id_receta\":" + idReceta + "}", 403);   // receta de otro paciente
        enviar(post("/solicitudes/renovacion"), tokenMedico, "{\"id_receta\":" + idReceta + "}", 403);      // un médico no solicita
        MvcResult renovacion = enviar(post("/solicitudes/renovacion"), tokenPaciente,
                "{\"id_receta\":" + idReceta + ",\"motivo\":\"Me queda poco\"}", 200);
        int idRenovacion = campo(renovacion, "$.id_solicitud");
        assertEquals("RENOVACION", texto(renovacion, "$.tipo_solicitud"));
        assertEquals(idReceta, campo(renovacion, "$.id_receta"));
        assertEquals(idMedico, campo(renovacion, "$.id_medico"));
        assertEquals(1, ((net.minidev.json.JSONArray) JsonPath.read(renovacion.getResponse().getContentAsString(), "$.medicamentos")).size());
        // Ya hay una renovación en trámite para esa receta
        enviar(post("/solicitudes/renovacion"), tokenPaciente, "{\"id_receta\":" + idReceta + "}", 400);
        // El médico la aprueba: se emite una receta nueva y la solicitud conserva la receta que se renovó
        MvcResult renovada = enviar(put("/solicitudes/" + idRenovacion + "/aprobar"), tokenMedico, "{\"detalles\":[" + item + "]}", 200);
        assertTrue(campo(renovada, "$.id_receta") != idReceta, "La renovación debe emitir una receta nueva");
        mockMvc.perform(con(get("/solicitudes/paciente/" + idPaciente), tokenPaciente))
                .andExpect(jsonPath("$[?(@.id_solicitud==" + idRenovacion + ")].estado_nombre").value("APROBADA"))
                .andExpect(jsonPath("$[?(@.id_solicitud==" + idRenovacion + ")].id_receta").value(idReceta));

        // ---- Bajas lógicas: lo eliminado queda inactivo y se audita quién lo eliminó y cuándo
        // Ficha: solo el paciente quita lo suyo
        mockMvc.perform(con(delete("/pacientes/" + idPaciente + "/alergias/" + idAlergia), tokenPaciente2)).andExpect(status().isForbidden());
        mockMvc.perform(con(delete("/pacientes/" + idPaciente + "/alergias/" + idAlergia), tokenMedico)).andExpect(status().isForbidden());
        mockMvc.perform(con(delete("/pacientes/" + idPaciente + "/alergias/" + idAlergia), tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alergias.length()").value(0));
        mockMvc.perform(con(delete("/pacientes/" + idPaciente + "/alergias/" + idAlergia), tokenPaciente)).andExpect(status().isNotFound());
        mockMvc.perform(con(delete("/pacientes/" + idPaciente + "/enfermedades/" + idEnfermedad), tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enfermedades.length()").value(0));
        // Volver a agregar una alergia dada de baja la reactiva
        enviar(post("/pacientes/" + idPaciente + "/alergias"), tokenPaciente, "{\"id_alergia\":" + idAlergia + "}", 200);

        // Catálogo: cualquier usuario con sesión; la baja queda a nombre de quien la hizo
        int idAlergia2 = campo(enviar(post("/catalogo/alergias"), tokenPaciente2, "{\"nombre_alergia\":\"Polen TEST\"}", 200), "$.id_alergia");
        mockMvc.perform(con(delete("/catalogo/alergias/" + idAlergia2), tokenMedico)).andExpect(status().isOk());
        mockMvc.perform(con(delete("/catalogo/alergias/" + idAlergia2), tokenMedico)).andExpect(status().isNotFound());
        mockMvc.perform(con(get("/catalogo/alergias"), tokenMedico))
                .andExpect(jsonPath("$[?(@.id_alergia==" + idAlergia2 + ")]").isEmpty());

        // Cuenta: cada usuario da de baja solo la suya y, después, deja de funcionar
        mockMvc.perform(con(delete("/usuarios/" + idUsuarioPaciente2), tokenPaciente)).andExpect(status().isForbidden());
        mockMvc.perform(con(delete("/usuarios/" + idUsuarioPaciente2), tokenPaciente2)).andExpect(status().isOk());
        mockMvc.perform(con(get("/medicos/listar"), tokenPaciente2)).andExpect(status().isUnauthorized());
        enviar(post("/auth/login"), null, "{\"correo_usuario\":\"paciente2" + sufijo + "@test.pe\",\"clave_usuario\":\"clave123\"}", 401);

        String auditoriaBajas = mockMvc.perform(con(get("/auditoria/listar"), tokenMedico)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(idUsuarioMedico, auditoriaCampo(auditoriaBajas, "tm_alergia", idAlergia2, "id_eliminacion"));
        assertTrue(auditoriaTexto(auditoriaBajas, "tm_alergia", idAlergia2, "fecha_eliminacion").contains("T"),
                "La fecha de eliminación debe incluir la hora");
        assertEquals(idUsuarioPaciente2, auditoriaCampo(auditoriaBajas, "tm_usuario", idUsuarioPaciente2, "id_eliminacion"));
        assertEquals(idUsuarioPaciente2, auditoriaCampo(auditoriaBajas, "tm_paciente", idPaciente2, "id_eliminacion"));
        // Lo quitado de la ficha del paciente: una baja de alergia y otra de enfermedad, a nombre del paciente
        net.minidev.json.JSONArray bajasFicha = JsonPath.read(auditoriaBajas,
                "$[?((@.tabla_afectada=='tm_sensibilidad' || @.tabla_afectada=='tm_diagnostico') && @.id_eliminacion != null)].id_eliminacion");
        assertEquals(2, bajasFicha.size(), "La baja de la alergia y la de la enfermedad deben quedar auditadas");
        assertTrue(bajasFicha.stream().allMatch(b -> ((Number) b).intValue() == idUsuarioPaciente));

        // ---- Auditoría: una fila por registro con la tabla, el id de la fila, quién lo hizo y cuándo
        String auditoria = mockMvc.perform(con(get("/auditoria/listar"), tokenMedico)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Quien crea su propia cuenta: id_usuario y id_registro son el mismo
        assertEquals(idUsuarioPaciente, auditoriaCampo(auditoria, "tm_usuario", idUsuarioPaciente, "id_creacion"));
        assertTrue(auditoriaTexto(auditoria, "tm_usuario", idUsuarioPaciente, "fecha_creacion").contains("T"),
                "La fecha de auditoría debe incluir la hora");
        // La edición del perfil queda en la misma fila
        assertEquals(idUsuarioPaciente, auditoriaCampo(auditoria, "tm_usuario", idUsuarioPaciente, "id_edicion"));
        // La solicitud la crea el paciente (sesión) y la atiende el médico (sesión)
        assertEquals(idUsuarioPaciente, auditoriaCampo(auditoria, "tm_solicitud", idSolicitud, "id_creacion"));
        assertEquals(idUsuarioMedico, auditoriaCampo(auditoria, "tm_solicitud", idSolicitud, "id_edicion"));
        // La receta la emite el médico
        assertEquals(idUsuarioMedico, auditoriaCampo(auditoria, "tm_receta", idReceta, "id_creacion"));
        // La ficha del paciente la modifica el propio paciente
        assertEquals(idUsuarioPaciente, auditoriaCampo(auditoria, "tm_paciente", idPaciente, "id_edicion"));
        // Los catálogos los registra quien tiene la sesión: el medicamento el médico; la alergia y la enfermedad el paciente
        assertEquals(idUsuarioMedico, auditoriaCampo(auditoria, "tm_medicamento", idMedicamento, "id_creacion"));
        assertEquals(idUsuarioPaciente, auditoriaCampo(auditoria, "tm_alergia", idAlergia, "id_creacion"));
        assertEquals(idUsuarioPaciente, auditoriaCampo(auditoria, "tm_enfermedad", idEnfermedad, "id_creacion"));
        assertTrue(auditoriaTexto(auditoria, "tm_medicamento", idMedicamento, "fecha_creacion").contains("T"),
                "La fecha de auditoría del medicamento debe incluir la hora");
        // El otro paciente no dejó ninguna modificación de la ficha de este paciente
        assertTrue(idUsuarioPaciente2 != idUsuarioPaciente);

        // ---- Todo registro nuevo, en cualquier tabla, dejó exactamente una fila de auditoría
        Map<String, Long> registrosDespues = contarRegistros();
        Map<String, Long> auditoriaDespues = contarAuditoria();
        for (String tabla : TABLAS_AUDITADAS) {
            long nuevos = registrosDespues.get(tabla) - registrosAntes.get(tabla);
            long auditados = auditoriaDespues.get(tabla) - auditoriaAntes.get(tabla);
            assertTrue(nuevos > 0, "El flujo debería haber creado registros en " + tabla);
            assertEquals(nuevos, auditados, "Cada registro nuevo de " + tabla + " debe tener su fila de auditoría");
        }
    }

    private Map<String, Long> contarRegistros() {
        Map<String, Long> conteo = new HashMap<>();
        for (String tabla : TABLAS_AUDITADAS) {
            conteo.put(tabla, jdbc.queryForObject("SELECT COUNT(*) FROM mediconnect." + tabla, Long.class));
        }
        return conteo;
    }

    private Map<String, Long> contarAuditoria() {
        Map<String, Long> conteo = new HashMap<>();
        for (String tabla : TABLAS_AUDITADAS) {
            conteo.put(tabla, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM mediconnect.tm_auditoria WHERE tabla_afectada = ?", Long.class, tabla));
        }
        return conteo;
    }

    private MockHttpServletRequestBuilder con(MockHttpServletRequestBuilder metodo, String token) {
        return token == null ? metodo : metodo.header("Authorization", "Bearer " + token);
    }

    private MvcResult enviar(MockHttpServletRequestBuilder metodo, String token, String json, int estadoEsperado) throws Exception {
        return mockMvc.perform(con(metodo, token).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(estadoEsperado))
                .andReturn();
    }

    private MvcResult login(String correo) throws Exception {
        return enviar(post("/auth/login"), null,
                "{\"correo_usuario\":\"" + correo + "\",\"clave_usuario\":\"clave123\"}", 200);
    }

    private int campo(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), ruta);
    }

    private String texto(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), ruta);
    }

    private int auditoriaCampo(String json, String tabla, int idRegistro, String campo) {
        net.minidev.json.JSONArray valores = JsonPath.read(json,
                "$[?(@.tabla_afectada=='" + tabla + "' && @.id_registro==" + idRegistro + ")]." + campo);
        assertEquals(1, valores.size(), "Debe existir una sola fila de auditoría para " + tabla + " #" + idRegistro);
        return ((Number) valores.get(0)).intValue();
    }

    private String auditoriaTexto(String json, String tabla, int idRegistro, String campo) {
        net.minidev.json.JSONArray valores = JsonPath.read(json,
                "$[?(@.tabla_afectada=='" + tabla + "' && @.id_registro==" + idRegistro + ")]." + campo);
        return String.valueOf(valores.get(0));
    }

    private String registro(String correo, String dni, String telefono, String extra) {
        return "{\"correo_usuario\":\"" + correo + "\",\"clave_usuario\":\"clave123\","
                + "\"nombre_usuario\":\"Test\",\"apellido_usuario\":\"Usuario\",\"dni\":\"" + dni.substring(0, 8) + "\","
                + "\"fecha_nacimiento\":\"1980-05-20\",\"telefono\":\"" + telefono.substring(0, 9) + "\"" + extra + "}";
    }
}
