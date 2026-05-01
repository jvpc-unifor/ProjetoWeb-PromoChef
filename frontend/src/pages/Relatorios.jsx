import React, { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import api from '../../services/api';
import styles from './Relatorios.module.css';

export default function Relatorios() {
    const [desperdicioMes, setDesperdicioMes] = useState([]);
    const [historico, setHistorico] = useState([]);
    const [loading, setLoading] = useState(true);

    // Cores para o gráfico de pizza (escala de laranja/vermelho)
    const COLORS = ['#ea580c', '#f97316', '#fb923c', '#fdba74', '#fed7aa', '#ffedd5'];

    // Busca os dados da API ao montar o componente
    useEffect(() => {
        const carregarRelatorios = async () => {
            try {
                const [desperdicioRes, historicoRes] = await Promise.all([
                    api.get('/relatorios/desperdicio'),
                    api.get('/relatorios/historico')
                ]);
                
                setDesperdicioMes(desperdicioRes.data);
                setHistorico(historicoRes.data);
            } catch (error) {
                console.error("Erro ao carregar relatórios:", error);
            } finally {
                setLoading(false);
            }
        };

        carregarRelatorios();
    }, []);

    // Formata valores em Reais (R$)
    const formatarMoeda = (valor) => {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor || 0);
    };

    // Calcula o total financeiro perdido no mês atual
    const totalDesperdicioMes = desperdicioMes.reduce((acc, curr) => acc + curr.valorPerdidoRs, 0);

    if (loading) {
        return <div className={styles.loading}>⏳ Gerando relatórios de desperdício...</div>;
    }

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h1>📉 Relatório de Desperdício</h1>
                <p>Acompanhe as perdas financeiras geradas por lotes vencidos.</p>
            </div>

            {/* Card de Resumo (Mês Atual) */}
            <div className={styles.summaryCard}>
                <div className={styles.cardIcon}>💸</div>
                <div className={styles.cardContent}>
                    <h3>Perda Total (Mês Atual)</h3>
                    <p className={styles.cardValue}>{formatarMoeda(totalDesperdicioMes)}</p>
                </div>
            </div>

            <div className={styles.chartsGrid}>
                {/* Gráfico de Pizza - Ingredientes mais perdidos */}
                <div className={styles.chartContainer}>
                    <h3>Composição do Desperdício (Mês Atual)</h3>
                    <div className={styles.chartWrapper}>
                        {desperdicioMes.length > 0 ? (
                            <ResponsiveContainer width="100%" height={300}>
                                <PieChart>
                                    <Pie
                                        data={desperdicioMes}
                                        dataKey="valorPerdidoRs"
                                        nameKey="ingrediente"
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={100}
                                        label={({ ingrediente }) => ingrediente}
                                    >
                                        {desperdicioMes.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip formatter={(value) => formatarMoeda(value)} />
                                </PieChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className={styles.noData}>Nenhum desperdício registrado neste mês. Ótimo trabalho! 🎉</p>
                        )}
                    </div>
                </div>

                {/* Gráfico de Barras - Histórico de 6 meses */}
                <div className={styles.chartContainer}>
                    <h3>Histórico de Perdas (Últimos 6 Meses)</h3>
                    <div className={styles.chartWrapper}>
                        {historico.length > 0 ? (
                            <ResponsiveContainer width="100%" height={300}>
                                <BarChart data={historico}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                                    <XAxis dataKey="mesAno" tick={{ fill: '#64748b' }} />
                                    <YAxis tick={{ fill: '#64748b' }} tickFormatter={(value) => `R$${value}`} />
                                    <Tooltip 
                                        formatter={(value) => formatarMoeda(value)}
                                        labelFormatter={(label) => `Mês: ${label}`}
                                    />
                                    <Bar dataKey="valorPerdidoRs" fill="#ea580c" radius={[4, 4, 0, 0]} name="Valor Perdido" />
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className={styles.noData}>Sem histórico suficiente para exibir.</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
