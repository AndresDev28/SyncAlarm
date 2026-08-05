# PRD: Smart Alarm Sync (SyncAlarm)
> **Estado:** Ready for SDD Orchestrator  
> **Versión:** 1.0.0  

## 1. Visión General & Objetivos
- **Problema/Puntos de Dolor:** 
  - La falta de integración contextual entre las apps de alarma nativas y los calendarios personales/laborales provoca llegadas tardías en usuarios con turnos rotativos, trabajos por guardia o agendas flexibles.
  - Riesgo continuo de error humano al activar/desactivar alarmas manualmente.
  - Ineficiencia en la gestión de excepciones de última hora (cambios o cancelaciones de turnos).
- **Propuesta de Valor:** 
  - Automatización *"set-and-forget"* que analiza los calendarios del dispositivo y de Google Calendar, identifica patrones de turnos mediante un motor de reglas basado en palabras clave y ajusta las alarmas dinámicamente aplicando *offsets* personalizados de preparación.
- **Métricas de Éxito:** 
  - 100% de tasa de activación correcta de alarmas sincronizadas con eventos del calendario (local y Google Calendar).
  - Reducción a 0 en fallos de activación de alarma por olvido manual del usuario.
  - Alta fiabilidad de la ejecución del servicio en segundo plano (cero desestimaciones por el ahorro de batería del SO).

---

## 2. Usuarios y Personas
- **Trabajador con Turnos Rotativos / Horario Flexible:**
  - *Descripción:* Profesional (ej. desarrollador, personal sanitario, trabajador por turnos) cuyos horarios de inicio laboral cambian según el día o la semana y utiliza calendarios nativos o de Google.
  - *Necesidad:* Olvidarse de programar manualmente alarmas cada noche y asegurarse de que el móvil le despierte con el tiempo exacto de margen previo según su lugar o modalidad de trabajo.

---

## 3. Alcance del Proyecto

### En Alcance (MVP - Scope In)
- Integración en lectura con el calendario nativo del dispositivo Android (`CalendarProvider`).
- **Integración con Google Calendar API:** Autenticación OAuth2 / Google Sign-In con almacenamiento encriptado de credenciales.
- Motor de reglas de coincidencia por palabras clave/regex para vincular eventos a offsets específicos (ej. "Presencial" = -120 min, "Remoto" = -30 min).
- Programación exacta de alarmas usando las APIs del sistema (`AlarmManager` y `WorkManager`).
- Resolución automática de conflictos si coexisten varios eventos el mismo día en cualquier calendario conectado (priorización del evento más temprano).
- Sistema de confirmación puntual ante cancelaciones o cambios de eventos con menos de 24 horas de antelación.
- Notificación nocturna interactiva (ej. 21:00) de resumen con botones de acción directa desde la propia alerta.
- Flujo de Onboarding para la gestión de permisos críticos y exención de optimización de batería.

### Fuera de Alcance (Out of Scope - Futuras Fases)
- Integración con dispositivos iOS (exclusivo para Android Nativo / Kotlin en MVP).
- Sincronización con otros proveedores de calendario de terceros (ej. Microsoft Outlook / Exchange).
- Alarmas compartidas/colaborativas entre múltiples usuarios.
- Detección de fases de sueño profundo mediante wearables/smartwatches.
- Retos o minijuegos para apagar la alarma (Snooze avanzado).

---

## 4. Requerimientos Funcionales

- **[RF-01] Sincronización de Calendarios & Gestión Segura de Credenciales:**
  - *Descripción:* El sistema debe acceder en modo lectura a los eventos de la agenda local mediante `CalendarProvider` y/o sincronizarse con la API de Google Calendar.
  - *Reglas de Negocio:* Solicitará el permiso `READ_CALENDAR`. Para la API de Google Calendar, gestionará tokens OAuth2 almacenando el *Refresh Token* de forma encriptada mediante `EncryptedSharedPreferences` / `Jetpack Security KeyStore`. El usuario elegirá qué calendarios específicos auditar.

- **[RF-02] Motor de Reglas y Offsets Personales:**
  - *Descripción:* El usuario puede definir reglas de asociación basadas en palabras clave contenidas en el título o descripción del evento.
  - *Reglas de Negocio:* Cada regla vincula una palabra clave (ej. "Turno Mañana") con un margen temporal en minutos (*offset*, ej. despertarse 90 minutos antes del inicio del evento).

- **[RF-03] Resolución de Conflictos por Eventos Múltiples:**
  - *Descripción:* Si se detectan dos o más eventos que coincidan en el mismo día, el sistema calculará la hora de activación priorizando el evento que comience más temprano.
  - *Reglas de Negocio:* La alarma principal se ajustará al evento más próximo. Si hay un segundo evento subsiguiente, se puede disparar un aviso informativo configurable de cortesía (ej. 30 minutos antes del segundo hito).

- **[RF-04] Manejo de Cancelaciones y Cambios de Última Hora:**
  - *Descripción:* Si el usuario cancela o altera un evento de calendario con un margen menor a 24 horas, la app re-evaluará los calendarios activos.
  - *Reglas de Negocio:* La app detectará el siguiente evento disponible más próximo e interactuará con el usuario vía notificación para confirmar si desea setear manualmente una nueva alarma bajo los parámetros por defecto.

- **[RF-05] Notificación Nocturna Interactiva de Verificación:**
  - *Descripción:* A las 21:00, el sistema enviará una notificación informativa indicando la alarma del día siguiente.
  - *Reglas de Negocio:* Incluirá el nombre del evento origen, la hora de inicio y la hora fijada para la alarma. La notificación ofrecerá **Action Buttons** interactivos para accionar sin abrir la app: `[Confirmar]` y `[Ajustar +15 min]`.

- **[RF-06] Programación Rígida de Sistema y Exención de Batería:**
  - *Descripción:* La alarma debe sonar de forma garantizada a la hora calculada, incluso en ahorro profundo de batería.
  - *Reglas de Negocio:* Implementación vía `AlarmManager.setExactAndAllowWhileIdle()` requiriendo `SCHEDULE_EXACT_ALARM`. En la fase de inicio/onboarding, la app solicitará explícitamente la exención de optimización de batería (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) para evitar la desestimación de tareas en segundo plano por parte del SO.

---

## 5. Mapeo Conceptual de Pantallas y UX

- **[Pantalla 1: Onboarding y Concesión de Permisos Críticos]:**
  - *Objetivo:* Guiar al usuario en la activación de permisos del sistema para garantizar la fiabilidad del servicio.
  - *Elementos Clave:* Solicitud de permisos de Calendario, Notificaciones, Alarmas Exactas y diálogo para ignorar optimizaciones de batería de Android.
  - *Flujo Sugerido:* Primer Inicio -> Onboarding Permisos -> Dashboard Principal.

- **[Pantalla 2: Dashboard Principal / Estado de Alarma]:**
  - *Objetivo:* Mostrar el estado de la alarma del día siguiente y la regla activa.
  - *Elementos Clave:* Reloj gigante con la próxima alarma, tarjeta del evento sincronizado (origen nativo o Google Calendar), conmutador manual (*override*).
  - *Flujo Sugerido:* Splash Screen / Onboarding -> Dashboard Principal.

- **[Pantalla 3: Configuración de Cuentas y Calendarios]:**
  - *Objetivo:* Gestionar permisos de lectura local y autenticación OAuth2 con Google.
  - *Elementos Clave:* Botón "Conectar con Google", lista de calendarios detectados con switches de activación.
  - *Flujo Sugerido:* Dashboard -> Ajustes -> Conectar Calendarios.

- **[Pantalla 4: Configuración de Reglas y Offsets]:**
  - *Objetivo:* Crear y editar los patrones de filtrado.
  - *Elementos Clave:* Formulario de entrada (Palabra Clave, Selector de Offset en minutos, Tono de Alarma), lista de reglas activas.
  - *Flujo Sugerido:* Dashboard -> Botón "Añadir Regla" -> Formulario de Reglas -> Guardar.

- **[Pantalla 5: Notificación Interactiva de Excepción / Resumen Nocturno]:**
  - *Objetivo:* Alertar o confirmar eventos desde la barra de estado de Android.
  - *Elementos Clave:* Alerta con botones `[Confirmar]` / `[Ajustar +15 min]`.

---

## 6. Requerimientos No Funcionales & Consideraciones Técnicas

- **Plataforma:** Android Nativo (Kotlin).
- **Arquitectura Interna Sugerida para SDD:** Clean Architecture + MVVM (Módulos de Dominio aislados sin dependencias de Android para facilitar TDD puro).
- **Procesamiento en Segundo Plano:** `WorkManager` para el escaneo periódico de las agendas y `AlarmManager` para el disparo de alertas exactas.
- **Integraciones y Seguridad de Terceros:** `Google Calendar API` (Google Play Services / OAuth2 PKCE), `Jetpack Security KeyStore` para encriptación de credenciales localmente.
- **Persistencia Local:** `Room DB` o `DataStore` para almacenar las reglas de usuario y preferencias de offset.
- **Permisos Críticos de Android:**
  - `android.permission.READ_CALENDAR`
  - `android.permission.SCHEDULE_EXACT_ALARM`
  - `android.permission.POST_NOTIFICATIONS`
  - `android.permission.INTERNET`
  - `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
