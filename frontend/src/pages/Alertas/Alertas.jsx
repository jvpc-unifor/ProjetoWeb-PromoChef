import { useEffect, useState } from 'react';
import api from '../../services/api';
import './Alertas.css';

export default function Alertas() {
    const [alertas, setAlertas] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        carregarAlertas();
    }, []);

    const carregarAlertas = async () => {
        try {
            const response = await api.get('/alertas/vencimento');
            setAlertas(response.data);
        } catch (error) {
            console.error('Erro ao buscar alertas:', error);
        } finally {
            setLoading(false);
        }
    };

    const marcarVisualizado = async (id) => {
        try {
            await api.patch(`/alertas/${id}/visualizar`);
            // Remove o alerta da lista atual após visualizar
            setAlertas(alertas.filter(a => a.id !== id));
        } catch (error) {
            console.error('Erro ao visualizar alerta:', error);
        }
    };

    if (loading) {
        return <div className="loading-state">⏳ Carregando alertas...</div>;
    }

    return (
        <div className="alertas-page">
            <div className="alertas-header">
                <h1>⚠️ Central de Alertas</h1>
                <p>Gerencie os lotes próximos ao vencimento e evite desperdícios.</p>
            </div>

            {alertas.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-icon">✅</div>
                    <h3>Tudo tranquilo por aqui!</h3>
                    <p>Nenhum lote próximo do vencimento encontrado.</p>
                </div>
            ) : (
                <div className="alertas-grid">
                    {alertas.map(alerta => {
                        // Calcula dias restantes se a mensagem não tiver claro, mas a mensagem já vem formatada
                        return (
                            <div key={alerta.id} className="alerta-card">
                                <div className="alerta-icon">⚠️</div>
                                <div className="alerta-content">
                                    <h3>Atenção Necessária</h3>
                                    <p>{alerta.mensagem}</p>
                                    <span className="alerta-date">Gerado em: {new Date(alerta.dataAlerta).toLocaleDateString('pt-BR')}</span>
                                </div>
                                <div className="alerta-actions">
                                    <button 
                                        className="btn-visualizar"
                                        onClick={() => marcarVisualizado(alerta.id)}
                                    >
                                        Marcar como Visto
                                    </button>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
