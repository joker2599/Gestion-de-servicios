# TechService Manager

Sistema web para la **gestión integral de servicios técnicos** y mantenimiento de equipos  
Dominio de referencia: [essentialsplus.cloud](https://essentialsplus.cloud)

Monorepo local: `D:\techservice-manager`



Arquitectura


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


| Contenedor | URL |
|---|---|
| Frontend (nginx) | http://localhost:8088 |
| Backend | http://localhost:8081 |
| Postgres | localhost:5432 |




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
├──
├── .github/workflows/ci.yml
├── docker-compose.yml
└── README.md
```



Fases del proyecto



| 1 Backend JPA/REST | Hecha |
| 2 Seguridad JWT | Hecha |
| 3 Frontend React | Hecha |
| 4 Calidad (tests) | Hecha |
| 5 Publicación (Docker/CI/README) | Hecha |
