# TechService Manager

Sistema web para la **gestión integral de servicios técnicos** y mantenimiento de equipos  
Dominio de referencia: [essentialsplus.cloud](https://essentialsplus.cloud)

Monorepo local: `D:\techservice-manager`

**Guía paso a paso:** [docs/guia-ejecucion-local.md](docs/guia-ejecucion-local.md)

---

## Arquitectura

```
┌─────────────────┐     JWT      ┌──────────────────────┐
│  Frontend Vite  │─────────────▶│  Backend Spring Boot │
│  React + TS     │   /api/v1    │  Security + JPA      │
│  :5173 / :8088  │◀─────────────│  :8081               │
└─────────────────┘              └──────────┬───────────┘
                                            │
                                            ▼
                                   ┌─────────────────┐
                                   │  PostgreSQL 17  │
                                   │  :5432          │
                                   └─────────────────┘
```

| Capa | Tecnología |
|---|---|
| UI | Vite, React 19, TypeScript, Tailwind CSS |
| API | Spring Boot 3.4, Java 21, Maven |
| Seguridad | Spring Security + JWT (BCrypt), roles `ADMIN` / `TECNICO` / `CLIENTE` |
| Persistencia | Spring Data JPA / Hibernate + PostgreSQL 17 |
| Docs API | Springdoc OpenAPI (Swagger UI) |
| Contenedores | Docker Compose (postgres + backend + frontend) |
| CI | GitHub Actions (`.github/workflows/ci.yml`) |

---

## Puertos

| Servicio | Puerto | URL |
|---|---|---|
| Frontend (Vite dev) | 5173 | http://localhost:5173 |
| Frontend (Docker/nginx) | **8088** | http://localhost:8088 |
| Backend API | 8081 | http://localhost:8081 |
| Swagger UI | 8081 | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | 8081 | http://localhost:8081/api-docs |
| Health | 8081 | http://localhost:8081/actuator/health |
| PostgreSQL | 5432 | `localhost:5432` |

> En esta máquina el puerto **8080** lo usa Payara; en Docker Compose el frontend publica **`8088:80`**.

---

## Inicio rápido (local sin Docker)

### 1. PostgreSQL

Servicio Windows `postgresql-x64-17` o:

```powershell
docker compose up -d postgres
```

DB / user / pass: `techservice` / `techservice` / `techservice`

### 2. Backend (Maven o NetBeans)

```powershell
cd D:\techservice-manager\backend
mvn spring-boot:run
```

**NetBeans 22:** File → Open Project → `D:\techservice-manager\backend` → Run (F6)

### 3. Frontend (Vite)

```powershell
cd D:\techservice-manager\frontend
npm install
npm run dev
```

Abre http://localhost:5173 — el proxy reenvía `/api` → `8081`.

---

## Usuarios demo

| Usuario | Password | Rol |
|---|---|---|
| `admin` | `Admin123!` | ADMIN |
| `tecnico` | `Tecnico123!` | TECNICO |
| `cliente` | `Cliente123!` | CLIENTE |

---

## Swagger / OpenAPI

1. Arranca el backend.
2. Abre http://localhost:8081/swagger-ui.html
3. Ejecuta **Auth → POST /api/v1/auth/login** con un usuario demo.
4. Copia el `accessToken` → botón **Authorize** → `Bearer <token>`.
5. Prueba los endpoints de órdenes, equipos, etc.

Especificación: http://localhost:8081/api-docs  
Tags: Auth, Usuarios, Clientes, Tecnicos, Equipos, Ordenes.

---

## Docker Compose (stack completo)

Requiere **Docker Desktop** (o motor Docker + Compose).

```powershell
cd D:\techservice-manager
docker compose up -d --build
```

| Contenedor | URL |
|---|---|
| Frontend (nginx) | http://localhost:8088 |
| Backend | http://localhost:8081 |
| Postgres | localhost:5432 |

Solo base de datos:

```powershell
docker compose up -d postgres
```

**Nota (OTIC-PC7136):** stack validado con Docker Desktop 4.98 / Engine 29.9.2. Antes del compose detén `postgresql-x64-17` si ocupa 5432.

---

## Tests

```powershell
cd D:\techservice-manager\backend
mvn test
```

Informe JaCoCo: `backend/target/site/jacoco/index.html`

Frontend:

```powershell
cd D:\techservice-manager\frontend
npm run build
```

---

## CI (GitHub Actions)

Workflow: `.github/workflows/ci.yml`

- Job **backend**: JDK 21 + `mvn test` + artefacto JaCoCo  
- Job **frontend**: Node 22 + `npm run build`

Se activa en `push`/`pull_request` a `main`/`master` y ramas `cursor/**`.

---

## Estructura del monorepo

```
techservice-manager/
├── backend/                 Spring Boot (Maven / NetBeans)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/techservice/
├── frontend/                Vite + React
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
├── docs/screenshots/        Capturas de referencia
├── .github/workflows/ci.yml
├── docker-compose.yml
└── README.md
```

---

## Capturas

![Login](docs/screenshots/login.png)

![Swagger](docs/screenshots/swagger.png)

---

## Fases del proyecto

| Fase | Estado |
|---|---|
| 1 Backend JPA/REST | Hecha |
| 2 Seguridad JWT | Hecha |
| 3 Frontend React | Hecha |
| 4 Calidad (tests) | Hecha |
| 5 Publicación (Docker/CI/README) | Hecha |
