# MediConnect Web (Angular)

Frontend del Web Application de MediConnect, según el diseño del informe (capítulo IV). Consume la API Spring Boot de `MediConnect_Project`.

## Cómo ejecutarlo

1. Base de datos: PostgreSQL con el script `DB/mediconnect_script.sql` (esquema `mediconnect`).
2. Backend (puerto 8080): en `MediConnect_Project` ejecutar `./mvnw spring-boot:run`.
   Incluye `Config/CorsConfig.java`, que permite al Angular (`http://localhost:4200`) llamar a la API.
3. Frontend:

```powershell
cd MediConnect_Web
npm install
npm start        # http://localhost:4200
```

Si el backend corre en otra URL (p. ej. Render), cambiar `src/app/core/environment.ts`
y añadir el origen del frontend en la propiedad `app.cors.origenes` del backend.

## Pantallas

| Rol | Rutas |
|---|---|
| Público | `/login`, `/registro` (elección de rol → paciente / médico → cuenta creada), `/recuperar` |
| Paciente | `/paciente/inicio`, `/recetas`, `/solicitudes`, `/solicitar` (renovar / nueva receta), `/historial` (ficha editable), `/perfil` |
| Médico | `/medico/bandeja`, `/revision/:id` (aprobar y emitir / rechazar), `/pacientes`, `/pacientes/:id` (ficha, solo lectura), `/recetas`, `/perfil` |

Seguridad: token JWT en `sessionStorage` (o `localStorage` con «Recordarme»), interceptor `Authorization: Bearer`,
guards por rol y cierre de sesión automático ante un 401.

## Diferencias con el diseño (por lo que soporta el backend actual)

- **Recuperar contraseña**: el backend no tiene ese endpoint; la pantalla solo informa.
- **Ficha clínica**: el backend solo permite que la edite el propio paciente (en «Mi historial»); el médico la consulta en solo lectura. Alergias y enfermedades se eligen del catálogo; no existen «reacción» ni «notas clínicas».
- **Pacientes del médico**: no hay listado propio; se arma con los pacientes que le enviaron solicitudes.
- **Emitir receta**: el médico completa dosis, frecuencia y fechas al aprobar una solicitud; no hay «emitir receta nueva» sin solicitud previa.
- **Horas / tiempo de espera / notificaciones**: la API solo guarda fechas (sin hora), por eso no se muestran.
- **Estados de receta** «Vigente / Por vencer / Vencida»: se calculan en el front con `fecha_vencimiento` (≤ 15 días = por vencer).
- **Error de conexión** (Fig. 85): aparece cuando el envío de una solicitud falla por falta de red; conserva los datos en pantalla y permite «Reintentar envío».
