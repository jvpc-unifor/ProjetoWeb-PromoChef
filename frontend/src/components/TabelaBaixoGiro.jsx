import './TabelaBaixoGiro.css';

// Recebe a lista de produtos (dados) e a função utilitária do pai para alinhar valores R$
export default function TabelaBaixoGiro({ dados, formatarMoeda }) {
    return (
        <div className="table-container">
            <div className="table-header">
                <h3>⚠️ Alerta de Baixo Giro</h3>
                <p>Itens do cardápio que tiveram pouca ou nenhuma saída nos últimos 15 dias.</p>
            </div>
            
            <table className="custom-table">
                <thead>
                    <tr>
                        <th>Cód.</th>
                        <th>Produto</th>
                        <th>Unid. Vendidas</th>
                        <th>Receita Gerada</th>
                        <th>Ação Sugerida</th>
                    </tr>
                </thead>
                <tbody>
                    {/* Lista cada produto dinamicamente em uma linha */}
                    {dados?.map((item) => (
                        <tr key={item.id}>
                            <td>#{item.id}</td>
                            <td>{item.nome}</td>
                            <td>{item.totalVendido} un.</td>
                            <td>{formatarMoeda(item.receitaTotal)}</td>
                            <td>
                                {/* Destaque visual: Badge vermelho se vendeu 0, amarelo se vendeu pouco */}
                                <span className={`status-badge ${item.totalVendido === 0 ? 'danger' : 'warning'}`}>
                                    {item.totalVendido === 0 ? 'Promoção Urgente' : 'Avaliar Campanha'}
                                </span>
                            </td>
                        </tr>
                    ))}
                    
                    {/* Se o banco Ficticio PDV não retornou itens parados, elogiamos a gestão */}
                    {(!dados || dados.length === 0) && (
                        <tr>
                            <td colSpan="5" style={{textAlign: 'center', padding: '2rem', color: '#64748b'}}>
                                Nenhum produto com baixo giro detectado. Bom trabalho! 🎉
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
}
