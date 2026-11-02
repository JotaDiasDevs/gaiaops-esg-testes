package br.com.fiap.gaiaops.suporte;

import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.ConnectionString;

/**
 * Decide qual MongoDB os testes de integracao (BDD e API) usam.
 *
 * - Padrao (GitHub Actions e qualquer maquina com Docker): um container mongo:8.0
 *   descartavel, iniciado UMA vez pelo Testcontainers e compartilhado por todos os
 *   cenarios e testes. Some ao fim da execucao.
 * - Variavel TEST_MONGODB_URI definida: usa esse MongoDB (ex.: o instalado na
 *   maquina, quando nao ha Docker). Por seguranca o nome do banco precisa conter
 *   "teste", porque os testes apagam e recarregam as collections a cada cenario.
 */
public final class MongoDeTeste {

    static final String VARIAVEL = "TEST_MONGODB_URI";

    private static MongoDBContainer container;

    private MongoDeTeste() {
    }

    public static synchronized String uri() {
        String externa = System.getenv(VARIAVEL);
        if (externa != null && !externa.isBlank()) {
            exigirBancoDescartavel(externa);
            return externa;
        }
        if (container == null) {
            container = new MongoDBContainer("mongo:8.0");
            container.start();
        }
        return container.getReplicaSetUrl("gaiaops_testes");
    }

    /** Impede que um descuido aponte os testes para uma base com dados reais. */
    static void exigirBancoDescartavel(String uri) {
        String banco = new ConnectionString(uri).getDatabase();
        if (banco == null || !banco.toLowerCase().contains("teste")) {
            throw new IllegalStateException(VARIAVEL + " deve apontar para um banco descartavel cujo nome"
                    + " contenha 'teste' (ex.: mongodb://localhost:27017/gaiaops_testes). Informado: " + banco);
        }
    }
}
