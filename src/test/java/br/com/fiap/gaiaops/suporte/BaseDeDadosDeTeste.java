package br.com.fiap.gaiaops.suporte;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import br.com.fiap.gaiaops.carga.CargaInicial;
import br.com.fiap.gaiaops.comum.Colecoes;

/**
 * Devolve a base ao estado da carga inicial (os 100 documentos da Fase 5).
 *
 * Chamado antes de CADA cenario BDD e de CADA teste de API: assim nenhum teste
 * depende da ordem de execucao nem dos dados que outro teste criou ou alterou.
 */
public class BaseDeDadosDeTeste {

    private final MongoTemplate mongo;
    private final CargaInicial cargaInicial;

    public BaseDeDadosDeTeste(MongoTemplate mongo, CargaInicial cargaInicial) {
        this.mongo = mongo;
        this.cargaInicial = cargaInicial;
    }

    public void restaurar() {
        Colecoes.TODAS.forEach(colecao -> mongo.getCollection(colecao).drop());
        try {
            cargaInicial.run(null);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao recarregar a base de teste", e);
        }
        // Mesmo indice unico do ambiente real (mongo/init/01-setup.js): o codigo e a chave do equipamento.
        mongo.getCollection(Colecoes.EQUIPAMENTOS)
                .createIndex(Indexes.ascending("codigo"), new IndexOptions().unique(true));
    }
}
