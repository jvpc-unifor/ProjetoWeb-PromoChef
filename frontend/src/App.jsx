import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Login from './pages/Login';

// só pra teste
function Dashboard() {
  return (
      <div style={{ padding: '20px' }}>
        <h1>🎉 Dashboard PromoChef</h1>
        <p>Bem-vindo ao sistema! Autenticação funcionou!</p>
      </div>
  );
}

function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/*públicas */}
            <Route path="/login" element={<Login />} />

            {/* protegidas */}
            <Route
                path="/dashboard"
                element={
                  <PrivateRoute>
                    <Dashboard />
                  </PrivateRoute>
                }
            />

            {/* Redirecionar raiz para dashboard */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            {/* 404 */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
  );
}

export default App;