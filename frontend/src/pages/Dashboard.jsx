import { useAuth } from '../contexts/AuthContext';

export default function Dashboard() {
    const { user } = useAuth();

    return (
        <div className="dashboard">
            <div className="welcome-card">
                <h1>👋 Bem-vindo, {user?.nome}!</h1>
                <p>Perfil: <strong>{user?.tipo}</strong></p>
                <p className="welcome-subtitle">
                    {user?.tipo === 'ADMIN'
                        ? 'Você tem acesso completo ao sistema.'
                        : 'Gerencie promoções, alertas e relatórios.'}
                </p>
            </div>

            <div className="kpi-grid">
                <div className="kpi-card">
                    <div className="kpi-icon">📊</div>
                    <div className="kpi-content">
                        <h3>Vendas Hoje</h3>
                        <p className="kpi-value">R$ 1.234,00</p>
                    </div>
                </div>

                <div className="kpi-card">
                    <div className="kpi-icon">🔔</div>
                    <div className="kpi-content">
                        <h3>Alertas</h3>
                        <p className="kpi-value">3</p>
                    </div>
                </div>

                <div className="kpi-card">
                    <div className="kpi-icon">🏷️</div>
                    <div className="kpi-content">
                        <h3>Promoções</h3>
                        <p className="kpi-value">2</p>
                    </div>
                </div>

                <div className="kpi-card">
                    <div className="kpi-icon">📦</div>
                    <div className="kpi-content">
                        <h3>Produtos</h3>
                        <p className="kpi-value">45</p>
                    </div>
                </div>
            </div>
        </div>
    );
}