import { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import api from '../services/api';
import './Dashboard.css';
import { 
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip as RechartsTooltip, ResponsiveContainer, 
    BarChart, Bar
} from 'recharts';
import TabelaBaixoGiro from '../components/TabelaBaixoGiro';

export default function Dashboard() {
    const { user } = useAuth();
    
    // Estados para armazenar os retornos da API separadamente
    const [kpis, setKpis] = useState(null);
    const [graficos, setGraficos] = useState(null);
    const [loading, setLoading] = useState(true);

    // useEffect roda uma vez quando o componente é montado na tela
    useEffect(() => {
        const carregarDashboard = async () => {
            try {
                // Promise.all executa as chamadas em paralelo (mais rápido)
                const [kpisRes, vendasRes] = await Promise.all([
                    api.get('/dashboard/kpis'),
                    api.get('/dashboard/vendas')
                ]);
                
                // Salva os resultados no estado para renderizar a tela
                setKpis(kpisRes.data);
                setGraficos(vendasRes.data);
            } catch (error) {
                console.error("Erro ao carregar dashboard:", error);
            } finally {
                // Independentemente de erro ou sucesso, libera a tela de loading
                setLoading(false);
            }
        };

        carregarDashboard();
    }, []);

    // Função utilitária para formatar valores no padrão monetário do Brasil
    const formatarMoeda = (valor) => {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor || 0);
    };

    // Exibe tela de carregamento enquanto o Axios pede os dados pro Spring Boot
    if (loading) {
        return <div className="loading-state">⏳ Carregando sua inteligência de dados...</div>;
    }

    return (
        <div className="dashboard">
            <div className="welcome-card">
                <h1>👋 Bem-vindo, {user?.nome}!</h1>
                <p>Perfil: <strong>{user?.tipo}</strong></p>
                <p className="welcome-subtitle">
                    {user?.tipo === 'ADMIN'
                        ? 'Você tem acesso completo ao sistema.'
                        : 'Analise suas vendas, alertas e relatórios diários.'}
                </p>
            </div>

            {/* Cartões Macro - KPIs da API */}
            <div className="kpi-grid">
                <div className="kpi-card">
                    <div className="kpi-icon money">💰</div>
                    <div className="kpi-content">
                        <h3>Faturamento Mensal</h3>
                        <p className="kpi-value">{formatarMoeda(kpis?.faturamentoTotal)}</p>
                    </div>
                </div>

                <div className="kpi-card">
                    <div className="kpi-icon box">📦</div>
                    <div className="kpi-content">
                        <h3>Produtos Vendidos</h3>
                        <p className="kpi-value">{kpis?.totalProdutosVendidos}</p>
                    </div>
                </div>

                <div className="kpi-card">
                    <div className="kpi-icon ticket">🧾</div>
                    <div className="kpi-content">
                        <h3>Pedidos Realizados</h3>
                        <p className="kpi-value">{kpis?.totalPedidos}</p>
                    </div>
                </div>

                <div className="kpi-card">
                    <div className="kpi-icon chart">📈</div>
                    <div className="kpi-content">
                        <h3>Ticket Médio</h3>
                        <p className="kpi-value">{formatarMoeda(kpis?.ticketMedio)}</p>
                    </div>
                </div>
            </div>

            {/* Seção Principal de Gráficos */}
            <div className="charts-grid">
                
                {/* 1. Gráfico de Linhas - Evolução do Faturamento Diário */}
                <div className="chart-container">
                    <h3>Evolução do Faturamento (Últimos 7 dias)</h3>
                    <div className="chart-wrapper">
                        {/* ResponsiveContainer garante que o gráfico se ajuste a telas menores */}
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={graficos?.faturamentoDiario}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0"/>
                                <XAxis 
                                    dataKey="dataVenda" 
                                    tick={{fill: '#64748b'}} 
                                    tickFormatter={(tick) => new Date(tick).toLocaleDateString('pt-BR', {day: '2-digit', month: '2-digit'})}
                                />
                                <YAxis tick={{fill: '#64748b'}} tickFormatter={(value) => `R$${value}`}/>
                                <RechartsTooltip 
                                    formatter={(value) => formatarMoeda(value)} 
                                    labelFormatter={(label) => new Date(label).toLocaleDateString('pt-BR')} 
                                />
                                <Line type="monotone" dataKey="faturamento" stroke="#f97316" strokeWidth={3} activeDot={{ r: 8 }} name="Receita" />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* 2. Gráfico de Barras - Os 5 mais vendidos */}
                <div className="chart-container">
                    <h3>Top 5 - Produtos Mais Vendidos</h3>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={graficos?.top5Produtos} layout="vertical" margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#e2e8f0"/>
                                <XAxis type="number" tick={{fill: '#64748b'}}/>
                                <YAxis dataKey="nome" type="category" tick={{fill: '#64748b'}} width={120} />
                                <RechartsTooltip cursor={{fill: '#f1f5f9'}} />
                                <Bar dataKey="totalVendido" fill="#f97316" radius={[0, 4, 4, 0]} name="Unidades Vendidas" />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* 3. Tabela de Baixo Giro renderizada por meio do novo componente encapsulado */}
            <TabelaBaixoGiro dados={graficos?.produtosBaixoGiro} formatarMoeda={formatarMoeda} />
            
        </div>
    );
}