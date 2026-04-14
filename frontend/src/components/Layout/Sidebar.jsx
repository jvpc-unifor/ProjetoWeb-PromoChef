import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Sidebar.css';

export default function Sidebar() {
    const { user, signOut } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        signOut();
        navigate('/login');
    };

    const menuComum = [
        { path: '/dashboard', label: 'Dashboard', icon: '📊' },
        { path: '/importacao', label: 'Importação', icon: '📥' },
        { path: '/alertas', label: 'Alertas', icon: '🔔', badge: 3 },
    ];

    const menuAdmin = [
        { path: '/usuarios', label: 'Usuários', icon: '👥' },
        { path: '/configuracoes', label: 'Configurações', icon: '⚙️' },
    ];

    const menuGerente = [
        { path: '/promocoes', label: 'Promoções', icon: '🏷️', badge: 2 },
        { path: '/relatorios', label: 'Relatórios', icon: '📈' },
    ];

    const menuPerfil = user?.tipo === 'ADMIN' ? menuAdmin : menuGerente;
    const todosMenus = [...menuComum, ...menuPerfil];

    return (
        <aside className="sidebar">
            <div className="sidebar-header">
                <h2>🍔 PromoChef</h2>
                <p className="sidebar-subtitle">Sistema Inteligente</p>
            </div>

            <nav className="sidebar-nav">
                <ul className="menu-list">
                    {todosMenus.map((item) => (
                        <li key={item.path}>
                            <NavLink
                                to={item.path}
                                className={({ isActive }) =>
                                    `menu-item ${isActive ? 'active' : ''}`
                                }
                            >
                                <span className="menu-icon">{item.icon}</span>
                                <span className="menu-label">{item.label}</span>
                                {item.badge && (
                                    <span className="menu-badge">{item.badge}</span>
                                )}
                            </NavLink>
                        </li>
                    ))}
                </ul>
            </nav>

            <div className="sidebar-footer">
                <div className="user-info">
                    <div className="user-avatar">
                        {user?.nome?.charAt(0).toUpperCase()}
                    </div>
                    <div className="user-details">
                        <p className="user-name">{user?.nome}</p>
                        <p className="user-role">{user?.tipo}</p>
                    </div>
                </div>
                <button onClick={handleLogout} className="btn-logout">
                    🚪 Sair
                </button>
            </div>
        </aside>
    );
}