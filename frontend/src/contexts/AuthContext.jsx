import { createContext, useState, useContext, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext({});

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Isso aqui vai verificar se já existe usuário logado ao carregar
    useEffect(() => {
        const storedUser = localStorage.getItem('@PromoChef:user');
        const storedToken = localStorage.getItem('@PromoChef:token');

        if (storedUser && storedToken) {
            setUser(JSON.parse(storedUser));
            api.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
        }
        setLoading(false);
    }, []);

    async function signIn({ email, senha }) {
        try {
            const response = await api.post('/auth/login', { email, senha });

            const { token, nome, email: userEmail, tipo } = response.data;

            // Salva no navegador
            localStorage.setItem('@PromoChef:token', token);
            localStorage.setItem('@PromoChef:user', JSON.stringify({
                nome,
                email: userEmail,
                tipo
            }));

            // Configura token no Axios
            api.defaults.headers.common['Authorization'] = `Bearer ${token}`;

            setUser({ nome, email: userEmail, tipo });

            return { success: true };
        } catch (error) {
            console.error('Erro no login:', error);
            return {
                success: false,
                message: error.response?.data || 'Email ou senha inválidos'
            };
        }
    }

    function signOut() {
        localStorage.removeItem('@PromoChef:token');
        localStorage.removeItem('@PromoChef:user');
        setUser(null);
    }

    return (
        <AuthContext.Provider value={{
            user,
            signIn,
            signOut,
            loading,
            isAuthenticated: !!user
        }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth deve ser usado dentro de um AuthProvider');
    }
    return context;
}