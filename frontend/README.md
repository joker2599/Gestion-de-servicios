# Frontend — TechService Manager

Vite + React 19 + TypeScript + Tailwind CSS.

## Desarrollo

```powershell
cd D:\techservice-manager\frontend
npm install
npm run dev
```

Abre http://localhost:5173

El proxy de Vite reenvía `/api` → `http://localhost:8081`.  
Opcional: define `VITE_API_URL=http://localhost:8081` en `.env` para llamar al backend sin proxy.

## Rutas

| Ruta | Descripción | Roles |
|---|---|---|
| `/login` | Login JWT | Público |
| `/` | Dashboard | Autenticado |
| `/clientes` | CRUD clientes | ADMIN |
| `/equipos` | Inventario + historial | Autenticado |
| `/ordenes` | Gestión de órdenes | Autenticado (acciones por rol) |
| `/tecnicos` | Técnicos / carga | Autenticado |
| `/reportes` | Filtros de servicios | ADMIN, TECNICO |

NetBeans no es el IDE principal del front; usa VS Code/Cursor + Vite.
