# HOEM — Architecture Decision Records (ADR)

**Versión:** 1.0  
**Fecha:** 2026-05-12  
**Estado:** Aprobados — pre-MVP  
**Formato:** Contexto · Decisión · Alternativas descartadas · Consecuencias

---

## Índice

| # | Decisión | Estado |
|---|---|---|
| ADR-001 | Microservicios desde el inicio | ✅ Aprobado |
| ADR-002 | Spring Cloud Gateway como API Gateway | ✅ Aprobado |
| ADR-003 | JWT stateless en el API Gateway | ✅ Aprobado |
| ADR-004 | Una sola instancia PostgreSQL con schemas separados | ✅ Aprobado |
| ADR-005 | Flyway por servicio para migraciones | ✅ Aprobado |
| ADR-006 | HTTP síncrono entre servicios con Feign + Resilience4j | ✅ Aprobado |
| ADR-007 | Polling condicional por pantalla para sincronización colaborativa | ✅ Aprobado |
| ADR-008 | Cloudflare Tunnel para exposición a internet | ✅ Aprobado |
| ADR-009 | Resend para email transaccional | ✅ Aprobado |
| ADR-010 | Cache horaria de precios ESIOS | ✅ Aprobado |
| ADR-011 | ArchUnit para forzar reglas de dependencia entre capas | ✅ Aprobado |
| ADR-012 | Zustand para estado global en React | ✅ Aprobado |
| ADR-013 | SpringDoc OpenAPI por servicio | ✅ Aprobado |
| ADR-014 | Docker restart policy `unless-stopped` | ✅ Aprobado |
| ADR-015 | Logs por servicio con `docker-compose logs` | ✅ Aprobado |

---

## ADR-001 — Microservicios desde el inicio

**Contexto**
Proyecto en solitario, 10-15h/semana, stack Java/Spring Boot ya dominado. La decisión es si empezar con un monolito y migrar después, o separar desde el inicio.

**Decisión**
Arquitectura de microservicios desde el primer día, orquestados con `docker-compose`.

**Alternativas descartadas**
- *Monolito modular primero:* evita complejidad inicial pero garantiza un rewrite costoso cuando el producto crezca. Con el stack dominado, el coste de separar ahora es bajo.
- *Monolito modular con extracción posterior:* el "después" nunca llega en proyectos en solitario.

**Consecuencias positivas**
Cada módulo (despensa, compra, energía...) es desplegable, testeable y escalable de forma independiente. Un fallo en `energy-service` no tumba `pantry-service`.

**Consecuencias negativas**
Mayor complejidad operacional desde el día 1. Más Dockerfiles, más configuración, más superficie de error en local. Mitigado con `docker-compose` simple y sin Kubernetes.

---

## ADR-002 — Spring Cloud Gateway como API Gateway

**Contexto**
Con 8 microservicios independientes, el frontend no puede llamar directamente a cada uno. Se necesita un punto de entrada único que gestione enrutamiento, autenticación y CORS de forma centralizada.

**Decisión**
Usar Spring Cloud Gateway como único punto de entrada. Valida el JWT, enruta cada petición al servicio correspondiente e inyecta `X-User-Id` y `X-Household-Id` como headers.

**Alternativas descartadas**
- *Nginx como proxy inverso:* más ligero, pero sin integración nativa con el ecosistema Spring ni validación JWT. Requeriría lógica de autenticación duplicada en cada servicio.
- *Cada servicio expuesto directamente:* el frontend tendría que gestionar 8 URLs distintas, CORS en cada servicio y autenticación distribuida. Inmantenible.
- *Kong / Traefik:* potentes, pero añaden una tecnología fuera del stack Java dominado. Coste de aprendizaje injustificado para el volumen del MVP.

**Consecuencias positivas**
Un solo lugar para cambiar rutas, añadir rate limiting o modificar la política de autenticación. El frontend solo conoce una URL: `api.hoem.dev`.

**Consecuencias negativas**
El gateway es un punto único de fallo. Mitigado con `restart: unless-stopped` y healthchecks en `docker-compose`. Si el gateway cae, todo cae — aceptable en MVP con servidor doméstico y un solo desarrollador.

---

## ADR-003 — JWT stateless en el API Gateway

**Contexto**
Cada petición que llega al gateway necesita ser autenticada. La pregunta es si el gateway llama a `auth-service` en cada petición para validar el token, o lo valida él mismo de forma local.

**Decisión**
Validación JWT stateless en el propio gateway usando la clave pública compartida via variable de entorno. El gateway nunca llama a `auth-service` para validar — solo para emitir tokens (login/registro).

**Alternativas descartadas**
- *Validación en cada microservicio:* cada servicio tendría que importar lógica de seguridad. Duplicación garantizada y superficie de error mayor.
- *Gateway llama a auth-service en cada petición:* introduce latencia en cada request y hace que `auth-service` sea un cuello de botella. Si `auth-service` cae, toda la app deja de funcionar aunque el usuario ya esté logado.

**Consecuencias positivas**
Latencia mínima en autenticación. `auth-service` solo recibe tráfico real (login, registro, refresh). Los microservicios reciben `X-User-Id` y `X-Household-Id` ya validados en el header — no necesitan saber nada de JWT.

**Consecuencias negativas**
Los tokens no se pueden revocar instantáneamente — un token robado sigue siendo válido hasta que expire (1 hora). Mitigado con refresh tokens en BD (tabla `sessions`) que sí se pueden revocar individualmente, y con el tiempo de expiración corto del access token.

---

## ADR-004 — Una sola instancia PostgreSQL con schemas separados por servicio

**Contexto**
Con 8 microservicios cada uno necesita su propio almacenamiento de datos. La pregunta es si cada servicio tiene su propia instancia de PostgreSQL o comparten una sola instancia con aislamiento lógico por schemas.

**Decisión**
Una sola instancia PostgreSQL 16 con un schema dedicado por servicio (`auth`, `pantry`, `shopping`, `tasks`, `menu`, `energy`, `notifications`). Cada servicio solo puede acceder a su propio schema — nunca al de otro. Si necesita datos de otro servicio, lo llama por HTTP.

**Alternativas descartadas**
- *Una instancia PostgreSQL por servicio:* aislamiento total real, pero 8 instancias consumirían ~1.6GB de RAM solo en bases de datos. Inviable con presupuesto cero y servidor doméstico.
- *Base de datos compartida sin schemas:* todos los servicios leen y escriben en las mismas tablas. Elimina el aislamiento por completo y acopla los servicios a nivel de datos.
- *SQLite por servicio:* más ligero, pero sin soporte real para concurrencia multi-usuario ni las capacidades de PostgreSQL necesarias para el módulo de energía.

**Consecuencias positivas**
Mismo aislamiento lógico que tener instancias separadas, con un solo contenedor que gestionar. Las migraciones Flyway de cada servicio operan sobre su propio schema sin interferir con los demás. Un único volumen Docker para backups.

**Consecuencias negativas**
Si PostgreSQL cae, todos los servicios pierden acceso a datos simultáneamente. Aceptable en MVP — mitigado con backups diarios automáticos.

---

## ADR-005 — Flyway por servicio para migraciones de base de datos

**Contexto**
El schema de cada servicio va a evolucionar con el tiempo — nuevas tablas, columnas, índices. Se necesita una forma de gestionar esos cambios de forma controlada, reproducible y sin intervención manual.

**Decisión**
Cada servicio incluye Flyway en su propio `pom.xml` y gestiona sus migraciones de forma independiente en `src/main/resources/db/migration/`. Los archivos siguen la convención `V1__init.sql`, `V2__add_column.sql`, etc. Flyway ejecuta las migraciones pendientes automáticamente al arrancar el servicio.

**Alternativas descartadas**
- *Scripts SQL manuales:* ejecutar scripts a mano en producción es fuente garantizada de errores humanos y estados inconsistentes entre entornos.
- *Liquibase:* equivalente a Flyway en funcionalidad, pero con mayor complejidad de configuración (XML/YAML en lugar de SQL plano). Sin ventaja real para este proyecto.
- *Migraciones centralizadas en un único módulo:* rompe el principio de que cada servicio es autónomo. Si un servicio cambia su schema, no debería requerir tocar un módulo compartido.

**Consecuencias positivas**
El historial completo de cambios del schema está versionado junto al código del servicio en Git. Cualquier entorno nuevo arranca con el schema correcto automáticamente al hacer `docker-compose up`. Rollbacks controlados con `V2__rollback.sql`.

**Consecuencias negativas**
Si dos servicios comparten accidentalmente una tabla (violando el principio de schema propio), Flyway no lo detecta — la disciplina la pone el desarrollador. Con un solo desarrollador esto es manejable.

---

## ADR-006 — HTTP síncrono entre servicios con Feign + Resilience4j

**Contexto**
Los microservicios necesitan comunicarse entre sí. Por ejemplo, `menu-service` necesita saber qué productos hay en despensa para generar sugerencias, o `shopping-service` necesita notificar a `pantry-service` cuando se completa una compra.

**Decisión**
Comunicación entre servicios via HTTP síncrono usando Spring Cloud OpenFeign como cliente declarativo. Resilience4j añade circuit breaker y retry automático para evitar fallos en cascada.

**Alternativas descartadas**
- *RabbitMQ / Kafka (mensajería asíncrona):* introduce un broker de mensajes como contenedor adicional, complejidad de configuración y gestión de colas. El volumen de mensajes en el MVP no justifica esta complejidad. Se reevalúa si el sistema crece.
- *RestTemplate manual:* más verboso que Feign sin ninguna ventaja. Feign permite definir un cliente HTTP con una sola interfaz Java anotada.
- *gRPC:* más eficiente en rendimiento, pero requiere definir contratos Protobuf y añade complejidad de tooling. Sin beneficio medible para el volumen del MVP.

**Consecuencias positivas**
Feign hace que llamar a otro servicio sea tan simple como llamar a un método Java local. Resilience4j garantiza que si `energy-service` cae, el resto de la app sigue funcionando con un fallback definido.

**Consecuencias negativas**
Las llamadas síncronas acoplan los tiempos de respuesta — si `pantry-service` tarda, `menu-service` espera. Aceptable en MVP. Si aparecen problemas de latencia medidos, se introduce mensajería asíncrona en una iteración posterior.

---

## ADR-007 — Polling condicional por pantalla para sincronización colaborativa

**Contexto**
Varios miembros del hogar pueden modificar simultáneamente listas de compra, tareas y despensa desde dispositivos distintos. Se necesita sincronización visible sin rediseñar la arquitectura del MVP.

**Decisión**
Polling condicional en el frontend mediante un custom hook `useInterval` que solo activa las peticiones cuando el componente está montado (pantalla abierta). Intervalos según criticidad colaborativa: lista de compra cada 5s, tareas cada 15s, despensa solo al cargar. El actor ve sus cambios al instante via optimistic UI.

**Alternativas descartadas**
- *WebSockets:* latencia real sub-segundo, pero requiere config especial en Spring Cloud Gateway, heartbeat para sobrevivir el timeout de 100s de Cloudflare, y gestión de reconexiones en React. Los servicios dejan de ser stateless. Coste injustificado antes de tener usuarios reales midiendo esa latencia.
- *Server-Sent Events (SSE):* más simple que WebSockets pero igualmente requiere conexiones persistentes y el mismo problema con Cloudflare.
- *Polling global fijo cada 10s:* genera peticiones innecesarias en módulos no colaborativos y en pantallas que el usuario no está viendo.

**Consecuencias positivas**
Los servicios permanecen stateless — sin cambios en el backend respecto a endpoints GET estándar. Testeable con MockMvc normal. Sin configuración adicional en Gateway ni Cloudflare.

**Consecuencias negativas**
Latencia máxima de 5–15s para observadores según el módulo. En escenarios de edición simultánea del mismo item se aplica last-write-wins.

**Decisión futura documentada**
Migrar a WebSockets en v2 cuando haya usuarios reales reportando la latencia como problema. El cambio afecta al frontend (hook → cliente WS), al Gateway (config proxy WS) y a los servicios colaborativos (handler + session registry). No requiere rediseño del modelo de datos.

---

## ADR-008 — Cloudflare Tunnel para exposición a internet

**Contexto**
El servidor es un PC doméstico con IP dinámica, sin IP pública fija y sin posibilidad de abrir puertos en el router de forma fiable. Se necesita exponer HOEM a internet con HTTPS para que usuarios reales puedan acceder desde fuera de la red local.

**Decisión**
Cloudflare Tunnel (`cloudflared`) como contenedor Docker adicional en el `docker-compose.yml`. Establece una conexión saliente desde la red local hacia Cloudflare — sin abrir puertos en el router. Cloudflare gestiona el certificado TLS automáticamente y proporciona un dominio estable aunque la IP cambie.

**Alternativas descartadas**
- *VPS (Railway, Render, Fly.io):* coste mensual recurrente. Incompatible con la restricción de presupuesto cero permanente (C-004).
- *DuckDNS + Let's Encrypt + puerto 443 abierto:* requiere abrir puertos en el router, gestionar renovación de certificados manualmente y asumir que la IP dinámica se actualiza a tiempo.
- *ngrok:* URLs aleatorias en el plan free, sin dominio propio, sin SLA. No apto para usuarios reales.
- *Tailscale:* excelente para acceso privado entre dispositivos, pero no sirve para exponer una app pública a usuarios externos.

**Consecuencias positivas**
HTTPS automático y renovación de certificados sin intervención. Dominio estable independiente de la IP. Protección DDoS básica de Cloudflare incluida. Un solo token en `.env` es toda la configuración necesaria.

**Consecuencias negativas**
Cloudflare se convierte en un punto de paso obligatorio — si Cloudflare tiene una incidencia, HOEM no es accesible desde internet aunque el servidor esté funcionando. El timeout de 100s en conexiones inactivas afecta a WebSockets en el futuro (documentado en ADR-007).

---

## ADR-009 — Resend para email transaccional

**Contexto**
HOEM necesita enviar emails en tres situaciones: verificación de cuenta al registrarse, invitaciones a miembros del hogar y alertas de franja barata de energía. Se necesita un servicio que gestione la entrega, reputación del dominio y DKIM/SPF sin configuración de servidor de correo propio.

**Decisión**
Resend como proveedor de email transaccional. Se integra desde `notification-service` mediante su SDK oficial para Java. La API key se inyecta via variable de entorno `RESEND_API_KEY`.

**Alternativas descartadas**
- *Servidor SMTP propio (Postfix):* requiere configurar DKIM, SPF y DMARC manualmente y mantener la reputación del dominio. Trabajo de administración de sistemas innecesario.
- *SendGrid:* plan gratuito limitado a 100 emails/día con restricciones más estrictas. Resend ofrece 3.000 emails/mes con una API más limpia.
- *Mailgun:* plan gratuito discontinuado. Requiere tarjeta de crédito.
- *JavaMailSender con Gmail SMTP:* dependiente de una cuenta Gmail personal, con límites bajos (500/día) y riesgo de bloqueo. No profesional para usuarios reales.

**Consecuencias positivas**
DKIM y SPF gestionados automáticamente — los emails llegan a la bandeja de entrada, no a spam. 3.000 emails/mes suficiente para el MVP con margen amplio. Dashboard con logs de entrega para depurar problemas.

**Consecuencias negativas**
Dependencia de un servicio externo para funcionalidad no crítica. Si Resend tiene una incidencia, las alertas y las invitaciones se retrasan — pero la app sigue funcionando. El límite de 3.000 emails/mes se reevalúa cuando haya usuarios reales consumiéndolo.

---

## ADR-010 — Cache horaria de precios ESIOS

**Contexto**
El módulo de energía necesita mostrar el precio actual del kWh y la previsión de las próximas 24 horas. La API de ESIOS es pública y gratuita pero es un sistema externo fuera del control del proyecto. Los precios PVPC se publican una vez al día para las 24 horas siguientes y cambian hora a hora.

**Decisión**
`energy-service` consulta la API de ESIOS una vez por hora mediante un job programado con `@Scheduled` de Spring. Los precios se almacenan en la tabla `price_cache` del schema `energy`. El frontend siempre lee de la cache local, nunca llama a ESIOS directamente. Si los datos tienen más de 25 horas de antigüedad, se muestra un aviso al usuario pero la app sigue funcionando con los últimos datos disponibles.

**Alternativas descartadas**
- *Consultar ESIOS en tiempo real en cada petición del usuario:* latencia variable, dependencia directa del tiempo de respuesta de ESIOS y riesgo de rate limiting. Un fallo de ESIOS tumbaría el módulo de energía completamente.
- *Cache en memoria sin persistencia:* los precios se pierden al reiniciar el contenedor. Con `restart: unless-stopped` los reinicios son raros, pero un corte de luz vaciaría la cache.
- *Cache con Redis:* añade un contenedor extra innecesario. PostgreSQL con una tabla simple cubre exactamente las mismas necesidades (YAGNI).

**Consecuencias positivas**
El módulo de energía funciona aunque ESIOS esté caído — usa los últimos datos disponibles con aviso transparente al usuario. Latencia de respuesta mínima — siempre se lee de PostgreSQL local. Sin riesgo de rate limiting en la API externa.

**Consecuencias negativas**
Los datos pueden tener hasta 1 hora de desfase respecto a ESIOS — aceptable dado que los precios PVPC cambian exactamente cada hora. Si ESIOS cambia el formato de su API, `energy-service` necesita actualización — mitigado con tests de contrato.

---

## ADR-011 — ArchUnit para forzar reglas de dependencia entre capas

**Contexto**
Cada servicio Spring Boot sigue Hexagonal Architecture con cuatro capas: `controller`, `application`, `domain` e `infrastructure`. La regla fundamental es que `domain` no puede depender de nada externo — ni Spring, ni JPA, ni HTTP. Sin un mecanismo automático que lo fuerce, es muy fácil que bajo presión de tiempo se importe una anotación de Spring en una clase de dominio, rompiendo el aislamiento sin que nadie lo detecte.

**Decisión**
ArchUnit como dependencia de test en todos los servicios. Un test de arquitectura por servicio verifica automáticamente en cada build que las reglas de dependencia se cumplen. Si una clase de `domain` importa algo de Spring o JPA, el test falla y el build se rompe antes de llegar a producción.

**Alternativas descartadas**
- *Revisión manual en code review:* con un solo desarrollador no hay code review real. La disciplina manual falla inevitablemente bajo presión de tiempo.
- *Módulos Maven separados por capa:* más estricto que ArchUnit pero multiplica la complejidad del proyecto — cada servicio pasaría de un módulo a cuatro. Coste de setup desproporcionado.
- *Sin enforcement:* la arquitectura hexagonal sin tests que la protejan se degrada a un monolito mal organizado en pocas semanas.

**Consecuencias positivas**
Las violaciones de arquitectura se detectan en el momento del build, no meses después. El test se escribe una vez por servicio y corre automáticamente con `mvn test`. Documentación ejecutable — el propio test describe qué capas pueden depender de qué.

**Consecuencias negativas**
Añade un test que hay que mantener si la estructura de paquetes cambia. El coste es mínimo — ArchUnit es una dependencia de test sin impacto en el artefacto final. El tiempo de build aumenta en milisegundos.

---

## ADR-012 — Zustand para estado global en React

**Contexto**
El frontend React 19 necesita gestionar estado compartido entre componentes — el usuario autenticado, el hogar activo y los datos de los módulos que se comparten entre pantallas. Se necesita una solución de estado global mantenible por un solo desarrollador sin boilerplate excesivo.

**Decisión**
Zustand como librería de estado global. Un store por dominio funcional (`useAuthStore`, `useHouseholdStore`, `usePantryStore`, etc.). El estado del servidor (datos de la API) se gestiona con TanStack Query — Zustand solo gestiona estado verdaderamente global como el usuario activo y el hogar seleccionado.

**Alternativas descartadas**
- *Redux Toolkit:* más potente y con mejor tooling de debugging, pero requiere definir actions, reducers y slices con considerablemente más boilerplate. Para una SPA de este tamaño el overhead no está justificado.
- *Context API de React:* suficiente para estado simple, pero provoca re-renders innecesarios en árbol completo cuando el estado cambia. Con 6 módulos y polling activo los re-renders serían un problema real de rendimiento.
- *Jotai / Recoil:* modelo atómico interesante pero menos maduro y con comunidad más pequeña que Zustand. Sin ventaja concreta para este caso de uso.
- *Estado local solamente:* viable para componentes simples, pero el usuario autenticado y el hogar activo se necesitan en demasiados puntos del árbol como para pasarlos por props.

**Consecuencias positivas**
Setup en menos de 10 líneas por store. Sin providers que envolver en el árbol de componentes. Compatible nativamente con React 19 y con el patrón de polling condicional del ADR-007. Devtools de Zustand disponibles para debugging.

**Consecuencias negativas**
Dos librerías de estado en paralelo (Zustand + TanStack Query) requieren una convención clara sobre qué va en cada una. Sin esa convención se mezclaría estado de servidor en Zustand y se perderían las ventajas de caché de TanStack Query. Convención documentada en cada store como comentario de cabecera.

---

## ADR-013 — SpringDoc OpenAPI por servicio

**Contexto**
Con 8 microservicios cada uno exponiendo su propia API REST, el frontend y el propio desarrollador necesitan una forma de conocer los contratos de cada endpoint sin mantener documentación manual que se desincronice del código.

**Decisión**
Cada servicio Spring Boot incluye `springdoc-openapi-starter-webmvc-ui` en su `pom.xml` y genera su documentación OpenAPI automáticamente en `/actuator/docs`. Sin repositorio centralizado de contratos. Si un servicio cambia su API, su documentación se actualiza sola en el siguiente build.

**Alternativas descartadas**
- *Contrato centralizado en YAML mantenido a mano:* se desincroniza del código real en cuanto hay presión de tiempo. Con un solo desarrollador el mantenimiento manual es garantía de documentación obsoleta.
- *Sin documentación de API:* inviable cuando el frontend necesita saber exactamente qué devuelve cada endpoint, especialmente en los módulos con polling donde los contratos de respuesta son críticos.

**Consecuencias positivas**
La documentación siempre refleja el estado real del código. Swagger UI disponible en desarrollo para probar endpoints sin Postman. Coste de setup: una dependencia en el `pom.xml` y una anotación en el controller.

**Consecuencias negativas**
Sin contrato centralizado, detectar breaking changes entre servicios requiere revisar cada `/actuator/docs` individualmente. Aceptable en MVP con un solo desarrollador que conoce todos los servicios.

---

## ADR-014 — Docker restart policy `unless-stopped`

**Contexto**
El servidor es un PC doméstico que puede reiniciarse por actualizaciones de Windows, cortes de luz o errores inesperados. Se necesita que HOEM arranque automáticamente sin intervención manual y que los contenedores individuales se recuperen solos si fallan.

**Decisión**
Todos los servicios en `docker-compose.yml` incluyen `restart: unless-stopped`. Docker Desktop gestiona el arranque automático al iniciar Windows sin scripts adicionales. Si un contenedor individual cae por un error de la aplicación, Docker lo reinicia solo.

**Alternativas descartadas**
- *Windows Task Scheduler con script `docker-compose up -d`:* requiere que el usuario haya iniciado sesión en Windows para dispararse, añade un punto de fallo extra y no cubre reinicios de contenedores individuales durante la ejecución normal.
- *Sin política de restart:* cualquier reinicio del PC o crash de un contenedor requeriría intervención manual. Inaceptable para un servidor que debe estar disponible para usuarios reales.

**Consecuencias positivas**
Recuperación automática ante caídas de contenedores individuales sin intervención. Arranque automático del sistema completo tras reinicios de Windows. Sin scripts externos que mantener.

**Consecuencias negativas**
Si un contenedor entra en un bucle de crash (crashloop), Docker lo reiniciará indefinidamente. Mitigado con healthchecks en `docker-compose.yml` que permiten detectar el problema en los logs antes de que afecte a usuarios.

---

## ADR-015 — Logs por servicio con `docker-compose logs`

**Contexto**
Con 8 microservicios corriendo simultáneamente se necesita una forma de leer qué está pasando cuando algo falla. La pregunta es si centralizar todos los logs en una herramienta de observabilidad o consultarlos por servicio de forma individual.

**Decisión**
Durante el MVP los logs se consultan por servicio con `docker-compose logs -f nombre-servicio`. Cada servicio Spring Boot usa el formato de log estructurado por defecto de Spring (timestamp + level + servicio + mensaje). Sin infraestructura de logs centralizada.

**Alternativas descartadas**
- *Grafana Loki desde el inicio:* añade dos contenedores extra (Loki + Grafana), configuración de agentes de log y ~500MB de RAM adicional. El valor real aparece cuando hay múltiples usuarios y se necesita correlacionar errores entre servicios — no en el MVP.
- *ELK Stack (Elasticsearch + Logstash + Kibana):* potente pero consume ~2GB de RAM adicional. Incompatible con la restricción de 13GB de RAM disponibles ya ajustados (C-006).

**Consecuencias positivas**
Sin contenedores adicionales ni RAM extra consumida. Setup inmediato — `docker-compose logs` funciona desde el primer `docker-compose up`. Suficiente para diagnosticar problemas con un solo desarrollador que conoce todos los servicios.

**Consecuencias negativas**
Sin correlación de logs entre servicios — para trazar una petición que pasa por Gateway → auth-service → pantry-service hay que consultar tres logs por separado. Aceptable en MVP.

**Decisión futura documentada**
Migrar a Grafana Loki cuando los logs de un servicio no sean suficientes para diagnosticar un problema — señal de que la correlación entre servicios es necesaria. Loki es gratuito self-hosted y añade solo dos contenedores ligeros.

---

## Historial de cambios

| Versión | Fecha | Cambios |
|---|---|---|
| 1.0 | 2026-05-12 | Versión inicial — 15 ADRs aprobados en revisión pre-MVP |
