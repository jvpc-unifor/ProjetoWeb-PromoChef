import { useEffect, useState } from 'react';
import api from '../../services/api';
import './Promocoes.css';

export default function Promocoes() {
    const [promocoes, setPromocoes] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        carregarPromocoes();
    }, []);

    const carregarPromocoes = async () => {
        try {
            const response = await api.get('/promocoes/sugestoes');
            setPromocoes(response.data);
        } catch (error) {
            console.error('Erro ao buscar promoções:', error);
        } finally {
            setLoading(false);
        }
    };

    const ativarPromocao = async (id) => {
        try {
            await api.patch(`/promocoes/${id}/ativar`);
            setPromocoes(promocoes.filter(p => p.id !== id));
        } catch (error) {
            console.error('Erro ao ativar promoção:', error);
        }
    };

    const recusarPromocao = async (id) => {
        try {
            await api.patch(`/promocoes/${id}/recusar`);
            setPromocoes(promocoes.filter(p => p.id !== id));
        } catch (error) {
            console.error('Erro ao recusar promoção:', error);
        }
    };

    if (loading) {
        return <div className="loading-state">⏳ Processando sugestões...</div>;
    }

    return (
        <div className="promocoes-page">
            <div className="promocoes-header">
                <h1>🏷️ Motor de Promoções</h1>
                <p>Sugestões inteligentes baseadas no vencimento de ingredientes para reduzir o desperdício.</p>
            </div>

            {promocoes.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">✨</div>
                    <h3>Sem sugestões no momento!</h3>
                    <p>O Motor PromoChef não encontrou novas oportunidades de promoção.</p>
                </div>
            ) : (
                <div className="promocoes-grid">
                    {promocoes.map(promo => (
                        <div key={promo.id} className="promocao-card">
                            <div className="promocao-badge">
                                {promo.descontoPct}% OFF
                            </div>
                            <div className="promocao-content">
                                <h3>{promo.produto.nome}</h3>
                                <p className="promocao-motivo"><strong>Motivo:</strong> {promo.motivo}</p>
                                <p className="promocao-preco">
                                    De: <span className="preco-antigo">R$ {promo.produto.preco.toFixed(2)}</span><br/>
                                    Por: <span className="preco-novo">R$ {(promo.produto.preco * (1 - promo.descontoPct / 100)).toFixed(2)}</span>
                                </p>
                            </div>
                            <div className="promocao-actions">
                                <button 
                                    className="btn-ativar"
                                    onClick={() => ativarPromocao(promo.id)}
                                >
                                    ✅ Ativar
                                </button>
                                <button 
                                    className="btn-recusar"
                                    onClick={() => recusarPromocao(promo.id)}
                                >
                                    ❌ Recusar
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
