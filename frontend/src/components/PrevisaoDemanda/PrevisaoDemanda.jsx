import { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import api from '../../services/api';
import styles from './PrevisaoDemanda.module.css';

export default function PrevisaoDemanda() {
    const [dadosPrevisao, setDadosPrevisao] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const carregarPrevisao = async () => {
            try {
                const response = await api.get('/dashboard/previsao');
                setDadosPrevisao(response.data);
            } catch (error) {
                console.error("Erro ao carregar previsão de demanda:", error);
            } finally {
                setLoading(false);
            }
        };

        carregarPrevisao();
    }, []);

    // Função utilitária para mapear MySQL DAYOFWEEK (1=Domingo) para nome do dia
    const getNomeDia = (diaSemana) => {
        const dias = {
            1: 'Domingo', 2: 'Segunda', 3: 'Terça', 
            4: 'Quarta', 5: 'Quinta', 6: 'Sexta', 7: 'Sábado'
        };
        return dias[diaSemana] || '';
    };

    if (loading) {
        return <div className={styles.loading}>⏳ Calculando previsões de demanda...</div>;
    }

    // Calcular previsão para "Amanhã"
    const hojeJs = new Date().getDay(); // 0=Dom, 1=Seg...
    const amanhaMysql = ((hojeJs + 1) % 7) + 1; // Mapeia para MySQL (1 a 7)
    
    // Filtrar dados específicos de amanhã
    const previsaoAmanha = dadosPrevisao
        .filter(item => item.diaSemana === amanhaMysql)
        .sort((a, b) => b.mediaVendasEsperada - a.mediaVendasEsperada); // Ordenar por mais vendidos

    // Preparar dados para o gráfico de projeção de 7 dias (Total agregado de todos os produtos por dia)
    const diasProjecao = [];
    for (let i = 1; i <= 7; i++) {
        // Começar de hoje
        const diaMysql = ((hojeJs + i - 1) % 7) + 1;
        
        // Somar a média esperada de todos os produtos para este dia
        const totalEsperado = dadosPrevisao
            .filter(item => item.diaSemana === diaMysql)
            .reduce((sum, curr) => sum + curr.mediaVendasEsperada, 0);

        diasProjecao.push({
            nomeDia: getNomeDia(diaMysql),
            mediaVendas: Math.round(totalEsperado) // Arredondar para gráfico ficar mais limpo
        });
    }

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2>🔮 Previsão de Demanda</h2>
                <p>Projeção baseada no histórico de vendas das últimas 4 semanas.</p>
            </div>

            <div className={styles.contentGrid}>
                {/* Cards de Previsão por Produto (Foco em Amanhã) */}
                <div className={styles.cardsSection}>
                    <h3>Projeção de Vendas para {getNomeDia(amanhaMysql)} (Amanhã)</h3>
                    <div className={styles.cardsGrid}>
                        {previsaoAmanha.length > 0 ? (
                            previsaoAmanha.map(item => (
                                <div key={item.produtoId} className={styles.productCard}>
                                    <div className={styles.cardHeader}>
                                        <span className={styles.productName}>{item.produtoNome}</span>
                                    </div>
                                    <div className={styles.cardBody}>
                                        <div className={styles.expectedValue}>
                                            {Math.round(item.mediaVendasEsperada)}
                                            <span className={styles.unit}>unidades</span>
                                        </div>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className={styles.noData}>Sem dados históricos para amanhã.</p>
                        )}
                    </div>
                </div>

                {/* Gráfico de Linhas - Projeção 7 dias (Volume Total) */}
                <div className={styles.chartSection}>
                    <h3>Projeção de Volume Total (Próximos 7 Dias)</h3>
                    <div className={styles.chartWrapper}>
                        {diasProjecao.length > 0 ? (
                            <ResponsiveContainer width="100%" height={300}>
                                <LineChart data={diasProjecao}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0"/>
                                    <XAxis dataKey="nomeDia" tick={{fill: '#64748b'}} />
                                    <YAxis tick={{fill: '#64748b'}} />
                                    <Tooltip 
                                        formatter={(value) => [`${value} unid.`, 'Volume Esperado']}
                                    />
                                    <Line 
                                        type="monotone" 
                                        dataKey="mediaVendas" 
                                        stroke="#f97316" 
                                        strokeWidth={4} 
                                        activeDot={{ r: 8 }} 
                                        name="Volume Esperado" 
                                    />
                                </LineChart>
                            </ResponsiveContainer>
                        ) : (
                            <p className={styles.noData}>Sem dados para gerar gráfico.</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
