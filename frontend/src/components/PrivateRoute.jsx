import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function PrivateRoute({ children }) {
    const { user, loading } = useAuth();

    // Mostra quando está carregando
    if (loading) {
        return (
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100vh'
            }}>
                <div>Carregando...</div>
            </div>
        );
    }
    // Se não estiver autenticado, manda fazer login
    if (!user) {
        return <Navigate to="/login" replace />;
    }

    // Autenticado, passa direto
    return children;
}