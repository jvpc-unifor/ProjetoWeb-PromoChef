import { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import './Navbar.css';

export default function Navbar({ title, onMenuToggle }) {
    const { user } = useAuth();
    const [showNotifications, setShowNotifications] = useState(false);

    // Simulação das notificações (Fazer de verdade só na Sprint 04)
    const notificacoes = [
        { id: 1, tipo: 'alerta', mensagem: 'Mozzarella vence em 2 dias', lido: false },
        { id: 2, tipo: 'promocao', mensagem: 'Nova promoção sugerida: Pizza Margherita', lido: false },
        { id: 3, tipo: 'alerta', mensagem: 'Tomate lote #A123 vence hoje', lido: false },
    ];

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