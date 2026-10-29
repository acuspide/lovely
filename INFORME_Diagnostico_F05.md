# Diagnóstico técnico y refactorización — F-05 Registro de pedidos

> **Estado del documento: INFORME PARCIAL (diagnóstico).**

## 1. Contexto

| Campo | Valor |
|-------|-------|
| Equipo | _[alejandro cubides -yulbryner vanegas yeraldin noguera]_ |
| Repositorio | https://github.com/acuspide/lovely |

**Requisitos relacionados** (tomados de los comentarios del código y del README; el SyRS
completo no está en el repositorio):

| Requisito | Descripción resumida |
|-----------|----------------------|
| RF-05.1 – RF-05.3 | El pedido nace en estado Pendiente, con número visible, precio y nombre congelados y datos de entrega. |
| RF-05.4 / RN01 / RN05 / RN09 | Cada producto debe estar activo, tener stock y alcanzar para la cantidad pedida. |
| RF-05.6 | Correo de confirmación después de guardar (si falla, el pedido sigue registrado). |
| RF-05.7 | Idempotencia: un reintento con la misma clave devuelve el mismo pedido. |
| RF-06.1 / RF-06.2 | Se descuenta el stock en la misma transacción: todo o nada. |
| RF-06.4 | Si no alcanza, se informa cuántas unidades quedan. |
| RF-06.5 | Sin sobreventa con compras simultáneas (bloqueo por producto, en orden de id). |

### 1.1 ¿Por qué se escogió este caso de uso?

El taller pide seleccionar una funcionalidad que involucre **una transacción de negocio** y,
de ser posible, **varias entidades**. Antes de decidir, el equipo revisó las ocho
funcionalidades implementadas (F-01 a F-08) y los casos de uso de la carpeta
`application/usecase/`. La elección de **F-05 Registro de pedidos** (con F-06 incluida) se
explica por las razones siguientes.

**1. Es la transacción de negocio central del sistema.** Lovely Girl existe para vender
productos, y el momento en que una venta queda en firme es cuando la clienta confirma su
pedido. Todo lo demás depende de este caso de uso: la gestión de estados (F-07) trabaja
sobre pedidos ya registrados, la factura (F-08) se emite a partir de ellos y el inventario
(F-06) solo cambia por su causa. Un error en este flujo tiene impacto directo en el negocio:
vender sin stock, cobrar un precio distinto al publicado o registrar dos veces la misma
compra. Por eso es la funcionalidad donde un buen diseño interno resulta más valioso.

**2. Involucra varias entidades y reglas de distintos módulos.** En una sola operación
participan tres entidades (`Pedido`, `Articulo` y `Usuario`), dos value objects
(`DetallePedido` y `DatosEntrega`) y tres repositorios. El caso de uso aplica reglas que
provienen de requisitos de tres funcionalidades distintas: del catálogo (el producto debe
estar activo, RF-02.7), del inventario (el stock debe alcanzar y no quedar negativo, RF-06.4
y RN20) y del propio pedido (precios congelados, idempotencia, RF-05.3 y RF-05.7). Esa
mezcla es justamente el escenario en el que suelen aparecer problemas de responsabilidades:
¿quién debe aplicar cada regla, el caso de uso o la entidad que tiene el dato?

**3. Es una transacción real de "todo o nada" con concurrencia.** RF-06.2 exige que o se
guarden el pedido y todos los descuentos de stock, o no se guarde nada; RF-06.5 exige que dos
clientas que compran la última unidad al mismo tiempo no la vendan dos veces. El caso de uso
resuelve esto con un puerto de transacciones (`GestorTransacciones`), bloqueo de filas
(`obtenerParaActualizar`) en orden de id para evitar bloqueos cruzados, y una clave de
idempotencia. Es el único flujo del proyecto que tiene una prueba de integración de
concurrencia dedicada (`RegistroPedidoConcurrenteTest`), lo que confirma que el propio equipo
lo considera el punto más delicado.

**4. Es el caso de uso con más colaboradores dentro de la lógica de negocio.**
`RegistrarPedidoUseCase` recibe seis dependencias (tres repositorios, el notificador de
correo, el gestor de transacciones y el reloj). Solo `RestablecerContrasenaUseCase` tiene
más (siete), pero ese caso de uso pertenece a la seguridad de cuentas y trabaja sobre una
sola entidad de negocio (`Usuario`) con sus tokens y sesiones. Un número alto de
colaboradores no es un defecto por sí mismo, pero sí indica dónde conviene revisar el
reparto de responsabilidades.

**5. Está bien protegido por pruebas, lo que permite refactorizar con seguridad.** La
segunda sesión del taller exige cambiar el código **sin alterar el comportamiento
observable**. `RegistrarPedidoUseCaseTest` tiene 13 pruebas unitarias que cubren el camino
feliz, el stock insuficiente, los productos agotados o desactivados, el "todo o nada", la
idempotencia y la agrupación de productos repetidos. Además, `ArticuloTest`,
`CancelarPedidoUseCaseTest` y `RevisarCarritoUseCaseTest` cubren las piezas vecinas. Sin esta
red de seguridad, cualquier refactorización sería una apuesta; con ella, se puede demostrar
que el comportamiento se conserva.

**6. Permite estudiar la arquitectura hexagonal de punta a punta.** El flujo atraviesa todas
las capas declaradas en el README: un adaptador de entrada (`PedidoRestController`), el caso
de uso de aplicación, las entidades y puertos del dominio, y tres adaptadores de salida
distintos (JPA/Oracle, transacción de Spring y correo SMTP). Esto hace posible cumplir el
requisito de documentar al menos un hallazgo sobre arquitectura, capas o dependencias con
evidencia concreta, y no solo sobre estilo de código.

#### Alternativas consideradas y descartadas

| Funcionalidad candidata | Por qué no se eligió |
|-------------------------|----------------------|
| F-01 Autenticación y roles (p. ej. `RestablecerContrasenaUseCase`) | Es transaccional y compleja, pero su complejidad es sobre todo de seguridad (tokens, hashes, sesiones) y gira en torno a una sola entidad de negocio. Además, refactorizar código de seguridad tiene un riesgo alto frente al beneficio académico. |
| F-02 Gestión del catálogo (`CrearArticuloUseCase`) | Es un CRUD de una sola entidad con una dependencia (`ArticuloRepository`); no hay transacción de negocio entre entidades. |
| F-04 Carrito de compras (`RevisarCarritoUseCase`) | Es de solo lectura: verifica el inventario pero no guarda nada, así que no hay transacción. |
| F-07 Estados del pedido (`CancelarPedidoUseCase`, `AvanzarEstadoPedidoUseCase`) | Son transaccionales, pero dependen de que exista un pedido registrado: son consecuencia de F-05. Analizar primero F-05 permite entender después estos flujos (y de hecho la refactorización H-02 también los mejora). |
| F-08 Factura en PDF (`DescargarFacturaUseCase`) | Gran parte de su complejidad está en la generación del PDF (un adaptador de infraestructura), no en reglas de negocio entre entidades. |

En resumen, F-05 reúne a la vez lo que pide el taller (transacción de negocio con varias
entidades), lo que más le importa al negocio (que la venta quede registrada correctamente) y
lo que hace viable la segunda sesión (pruebas suficientes para refactorizar sin romper nada).

---

## 2. Arquitectura real de la funcionalidad

### 2.1 Mapa de componentes

| Capa | Componente | Archivo | Responsabilidad en F-05 |
|------|-----------|---------|-------------------------|
| Infraestructura (adaptador de entrada) | `PedidoRestController.registrar` | `infrastructure/web/rest/PedidoRestController.java` (L84–98) | Recibe `POST /api/pedidos`, toma la clienta de la sesión, arma la `Solicitud`, responde 201/200. |
| Infraestructura (DTO) | `RegistrarPedidoRequest` | `infrastructure/web/rest/dto/RegistrarPedidoRequest.java` | Validación de forma con Bean Validation (líneas no vacías, máx. 50, cantidad 1–999). |
| Aplicación (caso de uso) | `RegistrarPedidoUseCase` | `application/usecase/RegistrarPedidoUseCase.java` | Orquesta idempotencia, transacción, bloqueo y verificación, descuento, creación del pedido y notificación. |
| Aplicación (puertos) | `GestorTransacciones`, `NotificadorPedidos` | `application/` | Abstraen la transacción y el correo. |
| Dominio (entidades) | `Pedido.registrar`, `Articulo.verificarDisponibleParaVenta`, `Articulo.descontarStock` | `domain/entity/` | Invariantes del pedido y del inventario. |
| Dominio (puertos) | `PedidoRepository`, `ArticuloRepository`, `UsuarioRepository` | `domain/repository/` | Contratos de persistencia. |
| Infraestructura (adaptadores de salida) | `PedidoRepositoryJpaAdapter`, `ArticuloRepositoryJpaAdapter`, `SpringGestorTransacciones`, `NotificadorPedidosSmtp` | `infrastructure/persistence/`, `infrastructure/correo/` | JPA/Oracle, transacción Spring, SMTP. |
| Configuración | `PedidosConfig` | `infrastructure/config/PedidosConfig.java` | Único lugar donde se conectan puertos con adaptadores. |
| Pruebas | `RegistrarPedidoUseCaseTest` (13 pruebas), `ArticuloTest`, `PedidoTest`, `RegistroPedidoConcurrenteTest` (requiere Oracle) | `src/test/java/...` | Protegen el comportamiento observable. |

### 2.2 Dependencias verificadas

Se verificó con `grep` que **ningún archivo de `domain/` ni de `application/` importa
`org.springframework`, `jakarta` ni `infrastructure`**. La regla de dependencia de la
arquitectura hexagonal se respeta en esta funcionalidad: los adaptadores dependen de los
puertos y no al revés.

### 2.3 Flujo de `POST /api/pedidos`

```
PedidoRestController.registrar
  └─ RegistrarPedidoUseCase.ejecutar
       ├─ usuarioRepository.obtenerPorId                 (clienta, para el correo)
       ├─ pedidoRepository.buscarPorClaveIdempotencia    (RF-05.7: ¿reintento?)
       ├─ transacciones.enTransaccion(registrarEnTransaccion)
       │    ├─ cantidadesPorArticulo                      (suma productos repetidos)
       │    ├─ por cada id en orden: bloquearYVerificar   (SELECT ... FOR UPDATE + reglas de venta)
       │    ├─ articulo.descontarStock + guardar          (RF-06.1)
       │    ├─ new DetallePedido(...)                     (congela nombre y precio)
       │    └─ Pedido.registrar + pedidoRepository.guardar
       ├─ catch PedidoDuplicadoException → devuelve el pedido existente
       └─ notificador.enviarConfirmacion                  (fuera de la transacción)
```

### 2.4 Línea base de pruebas (antes de cualquier cambio)

`./gradlew test` sobre `main` (`ffd8f0a`): **280 pruebas, 278 pasan, 2 fallan**.
Las 2 que fallan (`EcommerceApplicationTests.contextLoads` y
`RegistroPedidoConcurrenteTest`) son `@SpringBootTest` y necesitan Oracle levantado
(`java.net.ConnectException`); el README ya lo advierte. No son fallas del código.
Resultados XML guardados en `docs/refactorizacion/evidencias/antes/`.

---

## 3. Hallazgos

### Resumen

| ID | Ubicación | Categoría | Criterio afectado | Prioridad |
|----|-----------|-----------|-------------------|-----------|
| H-01 | `RegistrarPedidoUseCase.bloquearYVerificar` (L126–134) vs `Articulo.descontarStock` (L145–156) | Riesgo arquitectónico / deuda técnica | Encapsulamiento del dominio, Experto en información (GRASP), DRY | **Seleccionado** |
| H-02 | `RegistrarPedidoUseCase.cantidadesPorArticulo` (L137–143), `RevisarCarritoUseCase.ejecutar` (L41–44), `CancelarPedidoUseCase.devolverStock` (L82–85) | Deuda técnica (duplicación) | DRY, mantenibilidad | **Seleccionado** (complemento) |
| H-03 | `RegistrarPedidoRequest` (L24, L32) vs `RevisarCarritoUseCase` (L28, L45–47) | Riesgo arquitectónico | Separación de capas: regla de negocio en el adaptador web | Pendiente |
| H-04 | `PedidoRepositoryJpaAdapter.guardar` (L72–80) | Riesgo / oportunidad de mejora | Robustez, acoplamiento al texto de error del driver | Pendiente |

---

### Ficha H-01 — Regla "¿alcanza el stock?" duplicada entre caso de uso y entidad

- **ID:** H-01 | **Tipo:** riesgo arquitectónico / deuda técnica
- **Ubicación:** `backend/ecommerce/src/main/java/com/example/ecommerce/application/usecase/RegistrarPedidoUseCase.java`, método `bloquearYVerificar`, L126–134 (la comparación está en L130–132).
  Comparado con `domain/entity/Articulo.java`, método `descontarStock`, L145–156 (comparación en L152–154).
- **Evidencia:**

  ```java
  // RegistrarPedidoUseCase.java L129-132
  articulo.verificarDisponibleParaVenta();
  if (cantidad > articulo.getStock()) {
      throw new StockInsuficienteException(articulo.getNombre().getValor(), articulo.getStock());
  }
  ```
  ```java
  // Articulo.java L152-154
  if (cantidad > stock) {
      throw new StockInsuficienteException(nombre.getValor(), stock);
  }
  ```
- **Observación:** la regla de negocio "la cantidad pedida no puede superar el stock" (RN09 / RF-06.4)
  está escrita dos veces: una en el dominio (`Articulo`) y otra en la capa de aplicación, que
  lee el estado interno de la entidad con `getStock()`/`getNombre()` y construye ella misma la
  excepción de dominio. El caso de uso necesita verificar **todos** los productos antes de
  descontar ninguno (dos fases), y por eso no puede usar `descontarStock` directamente; pero
  la entidad no ofrece una operación de solo verificación con cantidad, así que la regla se
  copió fuera del dominio.
- **Criterio:** Experto en información (GRASP) y encapsulamiento: quien tiene el dato (`stock`)
  debe aplicar la regla. DRY. **No** se afirma violación de SRP: el caso de uso sigue teniendo
  una sola razón de cambio (orquestar el registro); el problema es la ubicación de la regla.
- **Impacto y riesgo:** si cambia la regla (p. ej. stock reservado, stock mínimo de exhibición,
  o un mensaje distinto), hay que cambiar dos lugares; si solo se cambia uno, la verificación
  previa y el descuento pueden discrepar. Además el orden de validaciones ya difiere entre los
  dos lugares (la verificación del caso de uso lanza `ArticuloAgotadoException` con stock 0;
  `descontarStock` lanzaría `StockInsuficienteException`), lo que muestra que son dos
  implementaciones independientes de la misma regla.
- **Acción propuesta:** agregar en `Articulo` un método de consulta-verificación
  `verificarPuedeVender(int cantidad)` que reúna `verificarDisponibleParaVenta()` y la
  comprobación de cantidad, y que `descontarStock` y `verificarPuedeVender` compartan un único
  método privado para la comparación. El caso de uso pasa a llamar
  `articulo.verificarPuedeVender(cantidad)`.
- **Verificación:** `RegistrarPedidoUseCaseTest` (13 pruebas: stock insuficiente, agotado,
  inactivo, todo o nada) y `ArticuloTest` deben seguir en verde sin modificarse; se agregan
  pruebas unitarias del nuevo método que fijan el tipo de excepción y el mensaje.
- **Estado:** seleccionado.

---

### Ficha H-02 — Agrupación de cantidades por artículo repetida en tres casos de uso

- **ID:** H-02 | **Tipo:** deuda técnica (duplicación)
- **Ubicación:**
  - `application/usecase/RegistrarPedidoUseCase.java`, `cantidadesPorArticulo`, L137–143
  - `application/usecase/RevisarCarritoUseCase.java`, `ejecutar`, L41–44
  - `application/usecase/CancelarPedidoUseCase.java`, `devolverStock`, L82–85
- **Evidencia:**

  ```java
  // RegistrarPedidoUseCase L138-141
  Map<Long, Integer> cantidades = new LinkedHashMap<>();
  for (Linea linea : lineas) {
      cantidades.merge(linea.articuloId(), linea.cantidad(), Integer::sum);
  }
  // RevisarCarritoUseCase L41-44
  Map<Long, Integer> cantidadPorArticulo = new LinkedHashMap<>();
  for (LineaSolicitada linea : lineas) {
      cantidadPorArticulo.merge(linea.articuloId(), linea.cantidad(), Integer::sum);
  }
  // CancelarPedidoUseCase L82-85
  Map<Long, Integer> unidadesPorArticulo = new TreeMap<>();
  for (DetallePedido detalle : pedido.getDetalles()) {
      unidadesPorArticulo.merge(detalle.getArticuloId(), detalle.getCantidad(), Integer::sum);
  }
  ```
- **Observación:** la regla "si un producto viene repetido se suman sus cantidades" está
  implementada tres veces con el mismo algoritmo, sobre tres tipos de línea distintos. En dos
  casos se conserva el orden de llegada (`LinkedHashMap`) y en el otro se ordena por id
  (`TreeMap`), lo cual es intencional (orden de bloqueo).
- **Criterio:** DRY / mantenibilidad.
- **Impacto y riesgo:** bajo. Es código corto y correcto; el riesgo es que una futura regla
  (p. ej. rechazar cantidades que sumadas excedan 999) se aplique solo en uno de los tres.
- **Acción propuesta:** extraer una utilidad de paquete `CantidadesPorArticulo.sumar(...)`
  genérica (recibe la lista y dos funciones para obtener id y cantidad) que devuelva un mapa en
  orden de llegada; `CancelarPedidoUseCase` lo envuelve en `TreeMap` para conservar su orden.
- **Verificación:** `RegistrarPedidoUseCaseTest.unProductoRepetidoQuedaEnUnSoloDetalle`,
  `RevisarCarritoUseCaseTest` y `CancelarPedidoUseCaseTest` sin cambios y en verde; prueba
  unitaria nueva para la utilidad (suma y orden).
- **Estado:** seleccionado (complemento de H-01, mismo commit de refactorización).

---

### Ficha H-03 — Límites del pedido solo en el DTO web (regla de negocio en el adaptador)

- **ID:** H-03 | **Tipo:** riesgo arquitectónico
- **Ubicación:** `infrastructure/web/rest/dto/RegistrarPedidoRequest.java` L24 (`@Size(max = 50)`)
  y L32 (`@Max(value = 999)`); comparado con `application/usecase/RevisarCarritoUseCase.java`
  L28 (`MAXIMO_PRODUCTOS_DISTINTOS = 50`) y L45–47.
- **Evidencia:**

  ```java
  // RegistrarPedidoRequest.java L23-25
  @NotEmpty(message = "Tu carrito está vacío.")
  @Size(max = 50, message = "El pedido puede tener como máximo 50 productos distintos.")
  List<@Valid @NotNull Linea> lineas,
  ```
  ```java
  // RevisarCarritoUseCase.java L45-47
  if (cantidadPorArticulo.size() > MAXIMO_PRODUCTOS_DISTINTOS) {
      throw new CarritoDemasiadoGrandeException(MAXIMO_PRODUCTOS_DISTINTOS);
  }
  ```
- **Observación:** el tope de 50 productos distintos y de 999 unidades por producto se aplica
  en el caso de uso del carrito, pero en el registro de pedidos solo existe como anotación del
  DTO de la capa web. `RegistrarPedidoUseCase` acepta cualquier tamaño si se invoca desde otro
  adaptador (otra API, un proceso batch, una prueba). Además, `@Size` cuenta **líneas**, no
  productos **distintos** como dice el mensaje (dos líneas del mismo producto cuentan dos). El
  valor 50 está repetido como literal en 3 sitios y 999 en 2.
- **Criterio:** separación de capas (la regla de negocio debe vivir en el dominio o la
  aplicación; el adaptador solo valida forma), consistencia entre casos de uso.
- **Impacto y riesgo:** medio en arquitectura, bajo hoy en funcionamiento: el único adaptador
  existente es el REST y el frontend (`CarritoService`) envía una línea por producto. **No se
  verificó** si el SyRS define estos topes como regla de negocio (no está en el repositorio).
- **Acción propuesta:** mover los topes a constantes del dominio y validarlos en
  `RegistrarPedidoUseCase` igual que en el carrito; los DTO referencian la constante.
  Cambiaría el código HTTP/mensaje en algunos casos límite, por eso se deja como pendiente y
  debe declararse como corrección si se hace.
- **Verificación:** prueba unitaria del caso de uso con 51 productos distintos; prueba
  `@WebMvcTest` del controlador para el mensaje.
- **Estado:** pendiente.

---

### Ficha H-04 — Detección de pedido duplicado por texto del mensaje del driver

- **ID:** H-04 | **Tipo:** riesgo técnico / oportunidad de mejora
- **Ubicación:** `infrastructure/persistence/PedidoRepositoryJpaAdapter.java`, `guardar`, L72–80
  (constante `RESTRICCION_IDEMPOTENCIA`, L24). Restricción definida en
  `db/migration/V7__f05_pedidos.sql` L26.
- **Evidencia:**

  ```java
  } catch (DataIntegrityViolationException ex) {
      if (String.valueOf(ex.getMostSpecificCause().getMessage()).toUpperCase(Locale.ROOT)
              .contains(RESTRICCION_IDEMPOTENCIA)) {
          throw new PedidoDuplicadoException(ex);
      }
      throw ex;
  }
  ```
- **Observación:** RF-05.7 (dos envíos simultáneos del mismo intento) depende de que el
  mensaje de texto de la excepción de Oracle contenga el nombre de la restricción. Si se
  renombra la restricción en una migración, cambia el driver o el formato del mensaje, la
  excepción deja de traducirse y la clienta recibe un error 500 en lugar del pedido ya
  registrado. El adaptador está en la capa correcta (traducir errores técnicos es su
  responsabilidad), así que no es un problema de capas, sino de robustez.
- **Criterio:** robustez / mantenibilidad (acoplamiento a un detalle textual).
- **Impacto y riesgo:** medio si ocurre, probabilidad baja. **No hay prueba que lo cubra**:
  `RegistroPedidoConcurrenteTest` prueba la última unidad, no la clave duplicada. No se pudo
  verificar en este entorno porque requiere Oracle.
- **Acción propuesta:** obtener el nombre de la restricción de forma estructurada
  (`org.hibernate.exception.ConstraintViolationException#getConstraintName()`) y agregar una
  prueba de integración con dos inserciones de la misma clave.
- **Verificación:** prueba de integración contra Oracle (Docker).
- **Estado:** pendiente.

---

### Observaciones que **no** se registran como hallazgo

- `RegistrarPedidoUseCase.ejecutar` consulta la clienta antes de la idempotencia solo para el
  correo; es una consulta extra, pero no un problema de diseño demostrable.
- `PedidoRestController.registrar` construye `DatosEntrega` (value object de dominio): en
  hexagonal el adaptador de entrada puede depender del dominio; no se considera violación.
- El método `registrarEnTransaccion` es largo (≈30 líneas) pero sus pasos están numerados y
  son cohesivos; un método largo no implica violar SRP.

---

## 4. Priorización

Escala 1 (bajo) – 3 (alto). Para esfuerzo, 1 = poco esfuerzo.

| ID | Impacto | Riesgo | Esfuerzo | Decisión y razón |
|----|:------:|:------:|:-------:|------------------|
| H-01 | 2 | 2 | 1 | **Se refactoriza.** Regla central de F-05/F-06; cambio pequeño y totalmente cubierto por 13 pruebas del caso de uso + pruebas de la entidad, sin alterar el comportamiento observable. Cumple el hallazgo obligatorio de arquitectura/responsabilidades. |
| H-02 | 1 | 1 | 1 | **Se refactoriza** junto con H-01: misma zona del código, riesgo mínimo y cubierto por pruebas de tres casos de uso. |
| H-03 | 2 | 2 | 2 | Pendiente: corregirlo cambia respuestas en casos límite (sería corrección, no refactorización) y falta confirmar la regla en el SyRS. |
| H-04 | 2 | 2 | 2 | Pendiente: requiere Oracle para verificar; queda como deuda técnica documentada. |

### Información faltante

- El SyRS y el documento de arquitectura no están en el repositorio; la coherencia
  arquitectura–código se evaluó contra el README y los comentarios del código.
- No se pudo levantar Oracle en el entorno de análisis: las 2 pruebas `@SpringBootTest` no se
  ejecutaron.
