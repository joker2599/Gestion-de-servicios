import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { ClientesPage } from './pages/ClientesPage';
import { DashboardPage } from './pages/DashboardPage';
import { EquiposPage } from './pages/EquiposPage';
import { LoginPage } from './pages/LoginPage';
import { OrdenesPage } from './pages/OrdenesPage';
import { ReportesPage } from './pages/ReportesPage';
import { TecnicosPage } from './pages/TecnicosPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route index element={<DashboardPage />} />
              <Route path="equipos" element={<EquiposPage />} />
              <Route path="ordenes" element={<OrdenesPage />} />
              <Route path="tecnicos" element={<TecnicosPage />} />

              <Route element={<ProtectedRoute roles={['ADMIN']} />}>
                <Route path="clientes" element={<ClientesPage />} />
              </Route>

              <Route element={<ProtectedRoute roles={['ADMIN', 'TECNICO']} />}>
                <Route path="reportes" element={<ReportesPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
