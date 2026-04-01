import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

// Serão feitas nas próximas Sprints é só para estruturar tudo.
function Importacao() { return <div><h2>📥 Importação</h2></div>; }
function Alertas() { return <div><h2>🔔 Alertas</h2></div>; }
function Promocoes() { return <div><h2>🏷️ Promoções</h2></div>; }
function Relatorios() { return <div><h2>📈 Relatórios</h2></div>; }
function Usuarios() { return <div><h2>👥 Usuários (ADMIN)</h2></div>; }
function Configuracoes() { return <div><h2>⚙️ Configurações (ADMIN)</h2></div>; }

function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route
                path="/"
                element={
                  <PrivateRoute>
                    <Layout title="Dashboard" />
                  </PrivateRoute>
                }
            >
              <Route index element={<Dashboard />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="importacao" element={<Importacao />} />
              <Route path="alertas" element={<Alertas />} />
              <Route path="promocoes" element={<Promocoes />} />
              <Route path="relatorios" element={<Relatorios />} />
              <Route path="usuarios" element={<Usuarios />} />
              <Route path="configuracoes" element={<Configuracoes />} />
            </Route>

            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
  );
}

export default App;