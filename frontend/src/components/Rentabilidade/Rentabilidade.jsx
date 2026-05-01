import { useEffect, useState } from 'react';
import api from '../../services/api';
import styles from './Rentabilidade.module.css';

export default function Rentabilidade() {
    const [produtos, setProdutos] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const carregarRentabilidade = async () => {
            try {
                const response = await api.get('/produtos/rentabilidade');
                setProdutos(response.data);
            } catch (error) {
                console.error("Erro ao carregar rentabilidade dos produtos:", error);
            } finally {
                setLoading(false);
            }
        };

        carregarRentabilidade();
    }, []);

    const formatarMoeda = (valor) => {
        return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor || 0);
    };

    const formatarPorcentagem = (valor) => {
        return `${(valor || 0).toFixed(2)}%`;
    };

    // Função para determinar a cor do semáforo com base na margem de lucro
    const getSemaforoClass = (margem) => {
        if (margem >= 50) return styles.semaforoVerde;
        if (margem >= 20) return styles.semaforoAmarelo;
        return styles.semaforoVermelho;
    };

    if (loading) {
        return <div className={styles.loading}>⏳ Analisando margens de lucro...</div>;
    }

    // Calcula os destaques
    const maisLucrativo = produtos.length > 0 ? produtos[0] : null; // Como a API já ordena DESC, o [0] é o maior
    const menosLucrativo = produtos.length > 0 ? produtos[produtos.length - 1] : null;

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2>📊 Margem de Lucro por Produto</h2>
                <p>Análise de rentabilidade cruzando o preço de venda com o custo real de produção (ingredientes).</p>
            </div>

            {/* Cards de Destaque */}
            <div className={styles.cardsGrid}>
                {maisLucrativo && (
                    <div className={`${styles.highlightCard} ${styles.cardGreen}`}>
                        <div className={styles.cardTitle}>Produto Mais Lucrativo</div>
                        <div className={styles.cardProductName}>{maisLucrativo.produtoNome}</div>
                        <div className={styles.cardValue}>{formatarPorcentagem(maisLucrativo.margemLucroPct)}</div>
                        <div className={styles.cardSubtitle}>Lucro Bruto: {formatarMoeda(maisLucrativo.lucroBruto)}</div>
                    </div>
                )}
                
                {menosLucrativo && (
                    <div className={`${styles.highlightCard} ${styles.cardRed}`}>
                        <div className={styles.cardTitle}>Atenção: Menor Rentabilidade</div>
                        <div className={styles.cardProductName}>{menosLucrativo.produtoNome}</div>
                        <div className={styles.cardValue}>{formatarPorcentagem(menosLucrativo.margemLucroPct)}</div>
                        <div className={styles.cardSubtitle}>Lucro Bruto: {formatarMoeda(menosLucrativo.lucroBruto)}</div>
                    </div>
                )}
            </div>

            {/* Tabela de Rentabilidade */}
            <div className={styles.tableContainer}>
                <table className={styles.table}>
                    <thead>
                        <tr>
                            <th>Status</th>
                            <th>Produto</th>
                            <th>Custo de Produção</th>
                            <th>Preço de Venda</th>
                            <th>Lucro Bruto</th>
                            <th>Margem de Lucro</th>
                        </tr>
                    </thead>
                    <tbody>
                        {produtos.map((produto) => (
                            <tr key={produto.produtoId}>
                                <td className={styles.statusCell}>
                                    <div className={`${styles.semaforo} ${getSemaforoClass(produto.margemLucroPct)}`}></div>
                                </td>
                                <td className={styles.nomeCell}>{produto.produtoNome}</td>
                                <td>{formatarMoeda(produto.custoProducao)}</td>
                                <td>{formatarMoeda(produto.precoVenda)}</td>
                                <td className={produto.lucroBruto < 0 ? styles.textRed : styles.textGreen}>
                                    {formatarMoeda(produto.lucroBruto)}
                                </td>
                                <td>
                                    <span className={`${styles.badge} ${getSemaforoClass(produto.margemLucroPct)}Badge`}>
                                        {formatarPorcentagem(produto.margemLucroPct)}
                                    </span>
                                </td>
                            </tr>
                        ))}
                        {produtos.length === 0 && (
                            <tr>
                                <td colSpan="6" className={styles.emptyMessage}>
                                    Nenhum dado de rentabilidade encontrado.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
            
            <div className={styles.legend}>
                <span className={styles.legendItem}><div className={`${styles.semaforo} ${styles.semaforoVerde}`}></div> Excelente (≥ 50%)</span>
                <span className={styles.legendItem}><div className={`${styles.semaforo} ${styles.semaforoAmarelo}`}></div> Atenção (20% a 49%)</span>
                <span className={styles.legendItem}><div className={`${styles.semaforo} ${styles.semaforoVermelho}`}></div> Crítico (&lt; 20%)</span>
            </div>
        </div>
    );
}
