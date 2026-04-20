# Módulo de Productos — Arquitectura Hexagonal (Ports & Adapters)
## Unidad 7 Post-Contenido 2

API REST que implementa arquitectura hexagonal estricta: el dominio es un POJO Java puro sin anotaciones de Spring ni JPA.

---

## Arquitectura Hexagonal

```
                    ┌─────────────────────────────────────┐
                    │           DOMINIO PURO               │
                    │   (sin Spring, sin JPA, solo java.*) │
                    │                                      │
  HTTP Request      │  ┌─────────────────────────────┐    │
      │             │  │   ProductoDomainService      │    │
      ▼             │  │  (CrearProductoUseCase)      │    │
┌──────────────┐    │  │  (ListarProductosUseCase)    │    │    ┌─────────────────────┐
│  ADAPTADOR   │    │  │  (ActualizarStockUseCase)    │    │    │     ADAPTADOR       │
│  PRIMARIO    │───►│  └──────────┬──────────────────┘    │───►│     SECUNDARIO      │
│  (Driving)   │    │             │                        │    │     (Driven)        │
│              │    │  Puerto IN  │  Puerto OUT             │    │                     │
│  ProductoC-  │    │  (interface)│  ProductoRepository-   │    │  ProductoRepository │
│  ontroller   │    │             │  Port (interface)       │    │  Adapter            │
│  @RestCtrl   │    └─────────────┼────────────────────────┘    │  @Component         │
└──────────────┘                  │                              │  + JpaRepository    │
                                  └─────────────────────────────►│  + H2 Database      │
                                                                  └─────────────────────┘
```

### Flujo de dependencias
```
HTTP → ProductoController → [Puerto IN] → ProductoDomainService
                                              │
                                         [Puerto OUT]
                                              │
                                    ProductoRepositoryAdapter → JPA → H2
```

---

## Estructura de Paquetes

```
src/main/java/com/example/hexagonal/
├── HexagonalApplication.java
├── domain/                          ← SIN imports de Spring ni JPA
│   ├── model/
│   │   ├── Producto.java            ← POJO puro
│   │   ├── StockInsuficienteException.java
│   │   ├── PrecioInvalidoException.java
│   │   └── ProductoNotFoundException.java
│   ├── port/
│   │   ├── in/
│   │   │   ├── CrearProductoUseCase.java
│   │   │   ├── ListarProductosUseCase.java
│   │   │   └── ActualizarStockUseCase.java
│   │   └── out/
│   │       └── ProductoRepositoryPort.java
│   └── service/
│       └── ProductoDomainService.java  ← Sin @Service
├── adapter/
│   ├── in/web/
│   │   ├── ProductoController.java     ← @RestController
│   │   └── GlobalExceptionHandler.java
│   └── out/persistence/
│       ├── ProductoJpaEntity.java      ← @Entity solo aquí
│       ├── ProductoJpaRepository.java
│       └── ProductoRepositoryAdapter.java ← @Component
└── config/
    └── BeanConfiguration.java          ← Wiring Spring↔dominio
```

---

## Principio clave

`ProductoDomainService` **no tiene `@Service`** — es una clase Java pura instanciada manualmente en `BeanConfiguration`. Esto garantiza que el dominio no dependa de Spring y pueda testearse con JUnit puro sin contexto.

---

## Requisitos

- Java 17+
- Maven 3.8+

---

## Ejecución

```bash
mvn clean package
mvn spring-boot:run
```

App en `http://localhost:8080` | Consola H2: `http://localhost:8080/h2-console`

---

## Pruebas con curl

### GET /api/productos — Lista vacía (200 OK)
```bash
curl -X GET http://localhost:8080/api/productos
```

### POST /api/productos — Crear producto (201 Created)
```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Laptop","descripcion":"Laptop gaming","precio":1500.00,"stock":10}'
```

### GET /api/productos/{id} — Buscar por ID
```bash
curl -X GET http://localhost:8080/api/productos/1
```

### PATCH /api/productos/{id}/stock — Reducir stock válido (200 OK)
```bash
curl -X PATCH "http://localhost:8080/api/productos/1/stock?cantidad=3"
```

### PATCH /api/productos/{id}/stock — Stock insuficiente (400 Bad Request)
```bash
curl -X PATCH "http://localhost:8080/api/productos/1/stock?cantidad=999"
```

---

## Checkpoints verificados

| Checkpoint | Estado |
|---|---|
| El proyecto compila con `mvn clean package` sin errores | ✅ |
| GET /api/productos retorna lista vacía (200 OK) | ✅ |
| POST /api/productos crea un producto y retorna 201 Created | ✅ |
| PATCH con cantidad > stock retorna 400 con mensaje de error | ✅ |
| Las clases en domain/ no tienen imports de org.springframework ni javax.persistence | ✅ |
| ProductoDomainService se puede instanciar sin @SpringBootTest | ✅ |
| ProductoRepositoryAdapter traduce correctamente Producto ↔ ProductoJpaEntity | ✅ |
