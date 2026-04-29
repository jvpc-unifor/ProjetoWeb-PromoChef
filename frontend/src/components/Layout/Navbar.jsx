import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import api from '../../services/api';
import './Navbar.css';

export default function Navbar({ title, onMenuToggle }) {
    const { user } = useAuth();
    const [showNotifications, setShowNotifications] = useState(false);
    const [notificacoes, setNotificacoes] = useState([]);

    useEffect(() => {
        carregarAlertas();
    }, []);

    const carregarAlertas = async () => {
        try {
            const [alertasRes, promocoesRes] = await Promise.all([
                api.get('/alertas/vencimento'),
                api.get('/promocoes/sugestoes')
            ]);
            
            // Mapeia para o formato esperado pelo menu
            const alertas = alertasRes.data.map(alerta => ({
                id: `alerta-${alerta.id}`,
                tipo: 'alerta',
                mensagem: alerta.mensagem,
                lido: alerta.visualizado
            }));

            const promocoes = promocoesRes.data.map(promo => ({
                id: `promo-${promo.id}`,
                tipo: 'promocao',
                mensagem: `Sugestão de ${promo.descontoPct}% OFF para ${promo.produto.nome}`,
                lido: false // promoções sugeridas sempre são tratadas como novas no dropdown até serem aceitas/recusadas
            }));

            setNotificacoes([...alertas, ...promocoes]);
        } catch (error) {
            console.error('Erro ao carregar notificações', error);
        }
    };

    const naoLidas = notificacoes.filter(n => !n.lido).length;

    return (
        <header className="navbar">
            <div className="navbar-left">
                <button className="btn-menu-toggle" onClick={onMenuToggle}>
                    ☰
                </button>
                <h1 className="navbar-title">{title}</h1>
            </div>

            <div className="navbar-right">
                <div className="notification-container">
                    <button
                        className="btn-notification"
                        onClick={() => setShowNotifications(!showNotifications)}
                    >
                        🔔
                        {naoLidas > 0 && (
                            <span className="notification-badge">{naoLidas}</span>
                        )}
                    </button>

                    {showNotifications && (
                        <div className="notification-dropdown">
                            <div className="notification-header">
                                <h4>Notificações</h4>
                                <span className="notification-count">{naoLidas} novas</span>
                            </div>
                            <ul className="notification-list">
                                {notificacoes.map((notificacao) => (
                                    <li
                                        key={notificacao.id}
                                        className={`notification-item ${!notificacao.lido ? 'unread' : ''}`}
                                    >
                                        <span className={`notification-icon ${notificacao.tipo}`}>
                                            {notificacao.tipo === 'alerta' ? '⚠️' : '🏷️'}
                                        </span>
                                        <span className="notification-message">{notificacao.mensagem}</span>
                                    </li>
                                ))}
                            </ul>
                            <div className="notification-footer">
                                <button>Ver todas</button>
                            </div>
                        </div>
                    )}
                </div>

                <div className="user-profile">
                    <div className="user-avatar-small">
                        {user?.nome?.charAt(0).toUpperCase()}
                    </div>
                    <span className="user-name-small">{user?.nome}</span>
                </div>
            </div>
        </header>
    );
}