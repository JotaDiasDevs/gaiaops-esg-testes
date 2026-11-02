package br.com.fiap.gaiaops.suporte;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import io.restassured.module.jsv.JsonSchemaValidator;

/**
 * Acesso aos contratos JSON Schema em src/test/resources/schemas.
 *
 * Cada contrato descreve a forma de uma resposta da API: campos obrigatorios,
 * tipos, formatos e valores permitidos. Se a API mudar a resposta de um jeito que
 * quebre um consumidor (remover um campo, trocar um tipo), o teste falha.
 */
public final class Contratos {

    private Contratos() {
    }

    /** Ex.: contrato("equipamento") valida contra schemas/equipamento.json. */
    public static JsonSchemaValidator contrato(String nome) {
        return matchesJsonSchemaInClasspath("schemas/" + nome + ".json");
    }
}
