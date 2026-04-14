import React, { useState, useRef } from 'react';
import api from '../../services/api';
import styles from './Importacao.module.css';

const TABS = [
    { id: 'produtos', title: 'Produtos', endpoint: '/importacao/produtos' },
    { id: 'ingredientes', title: 'Ingredientes', endpoint: '/importacao/ingredientes' },
    { id: 'ficha-tecnica', title: 'Ficha Técnica', endpoint: '/importacao/ficha-tecnica' },
    { id: 'vendas', title: 'Vendas', endpoint: '/importacao/vendas' }
];

function Importacao() {
    const [activeTab, setActiveTab] = useState(TABS[0]);
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const fileInputRef = useRef(null);

    const handleTabChange = (tab) => {
        setActiveTab(tab);
        setFile(null);
        setResult(null);
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        e.stopPropagation();
    };

    const handleDrop = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            validateAndSetFile(e.dataTransfer.files[0]);
        }
    };

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            validateAndSetFile(e.target.files[0]);
        }
    };

    const validateAndSetFile = (selectedFile) => {
        if (selectedFile.name.endsWith('.csv')) {
            setFile(selectedFile);
            setResult(null); // limpa resultados anteriores
        } else {
            alert('Por favor, selecione um arquivo válido (.csv)');
        }
    };

    const handleUpload = async () => {
        if (!file) return;
        setLoading(true);
        setResult(null);

        const formData = new FormData();
        formData.append('arquivo', file);

        try {
            const response = await api.post(activeTab.endpoint, formData);
            setResult(response.data);
        } catch (error) {
            if (error.response?.data) {
                setResult(error.response.data);
            } else {
                setResult({ sucesso: false, mensagem: 'Erro de comunicação com o servidor', erros: [] });
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2>📥 Importação de Dados CSV</h2>
                <p>Arraste seu arquivo e faça upload para abastecer o sistema.</p>
            </div>

            <div className={styles.tabs}>
                {TABS.map((tab) => (
                    <button
                        key={tab.id}
                        className={`${styles.tabBtn} ${activeTab.id === tab.id ? styles.active : ''}`}
                        onClick={() => handleTabChange(tab)}
                    >
                        {tab.title}
                    </button>
                ))}
            </div>

            <div className={styles.content}>
                <div
                    className={`${styles.dropzone} ${file ? styles.hasFile : ''}`}
                    onDragOver={handleDragOver}
                    onDrop={handleDrop}
                >
                    {!file ? (
                        <>
                            <p>Arraste o arquivo CSV de <b>{activeTab.title}</b> para cá</p>
                            <span>ou</span>
                            <button className={styles.browseBtn} onClick={() => fileInputRef.current.click()}>
                                Procurar Arquivo
                            </button>
                            <input
                                type="file"
                                accept=".csv"
                                ref={fileInputRef}
                                style={{ display: 'none' }}
                                onChange={handleFileChange}
                            />
                        </>
                    ) : (
                        <div className={styles.fileSelectedInfo}>
                            <p className={styles.fileName}>📄 {file.name}</p>
                            <span className={styles.fileSize}>{(file.size / 1024).toFixed(1)} KB</span>
                            <div className={styles.actions}>
                                <button className={styles.uploadBtn} onClick={handleUpload} disabled={loading}>
                                    {loading ? 'Enviando...' : 'Fazer Upload'}
                                </button>
                                <button className={styles.clearBtn} onClick={() => setFile(null)} disabled={loading}>
                                    Trocar
                                </button>
                            </div>
                        </div>
                    )}
                </div>

                {result && (
                    <div className={`${styles.resultCard} ${result.sucesso ? styles.success : styles.error}`}>
                        <h3>{result.mensagem}</h3>
                        <div className={styles.stats}>
                            <div className={styles.statBox}>
                                <span>Total Lido</span>
                                <strong>{result.totalLinhas}</strong>
                            </div>
                            <div className={styles.statBox}>
                                <span>Sucesso</span>
                                <strong className={styles.txtSuccess}>{result.linhasSucesso}</strong>
                            </div>
                            <div className={styles.statBox}>
                                <span>Erros</span>
                                <strong className={styles.txtError}>{result.linhasErro}</strong>
                            </div>
                        </div>

                        {result.erros && result.erros.length > 0 && (
                            <div className={styles.errorList}>
                                <h4>Resumo dos Erros:</h4>
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Linha</th>
                                            <th>Campo</th>
                                            <th>Valor Lido</th>
                                            <th>Justificativa</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {result.erros.map((erro, i) => (
                                            <tr key={i}>
                                                <td>#{erro.numeroLinha}</td>
                                                <td>{erro.campo}</td>
                                                <td><span className={styles.badValue}>{erro.valor || '(Vazio)'}</span></td>
                                                <td>{erro.motivo}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Importacao;
