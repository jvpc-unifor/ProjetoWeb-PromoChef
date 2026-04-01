import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar.jsx';
import Navbar from './Navbar.jsx';
import './Layout.css';

export default function Layout({ title = 'Dashboard' }) {
    const [sidebarOpen, setSidebarOpen] = useState(true);

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    return (
        <div className="layout">
            <Sidebar />

            <div className={`main-content ${!sidebarOpen ? 'sidebar-closed' : ''}`}>
                <Navbar title={title} onMenuToggle={toggleSidebar} />

                <main className="content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}