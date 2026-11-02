package br.com.fiap.gaiaops.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.LinkedHashMap;
import java.util.Map;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Pilar G: cadastro, integridade e desativacao dos equipamentos publicos. */
public class EquipamentoSteps {

    private final ContextoDoCenario contexto;

    public EquipamentoSteps(ContextoDoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("que a base ESG do município está carregada com {int} equipamentos")
    public void queABaseEstaCarregadaCom(int quantidade) {
        contexto.api().get("/api/equipamentos").then().statusCode(200).body("$", hasSize(quantidade));
    }

    @Dado("que o equipamento {string} foi desativado")
    public void queOEquipamentoFoiDesativado(String codigo) {
        contexto.api().delete("/api/equipamentos/{codigo}", codigo).then().statusCode(204);
    }

    /** Tabela de duas colunas (campo | valor); numeros viram numeros no JSON. */
    @Quando("o gestor cadastra o equipamento:")
    public void oGestorCadastraOEquipamento(Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        campos.forEach((campo, valor) -> corpo.put(campo, valor.matches("-?\\d+") ? Integer.valueOf(valor) : valor));
        contexto.registrar(contexto.api().body(corpo).post("/api/equipamentos"));
    }

    @Quando("o gestor cadastra o equipamento com código {string}, nome {string} e tipo {string}")
    public void oGestorCadastraOEquipamentoCom(String codigo, String nome, String tipo) {
        Map<String, Object> corpo = Map.of("codigo", codigo, "nome", nome, "tipo", tipo, "distrito", "Centro");
        contexto.registrar(contexto.api().body(corpo).post("/api/equipamentos"));
    }

    @Quando("o gestor desativa o equipamento {string}")
    public void oGestorDesativaOEquipamento(String codigo) {
        contexto.registrar(contexto.api().delete("/api/equipamentos/{codigo}", codigo));
    }

    @Entao("o equipamento {string} pode ser consultado e está ativo")
    public void oEquipamentoPodeSerConsultadoEEstaAtivo(String codigo) {
        contexto.api().get("/api/equipamentos/{codigo}", codigo).then()
                .statusCode(200)
                .body("codigo", equalTo(codigo))
                .body("ativo", equalTo(true));
    }

    @Entao("o equipamento {string} continua registrado, porém inativo")
    public void oEquipamentoContinuaRegistradoPoremInativo(String codigo) {
        contexto.api().get("/api/equipamentos/{codigo}", codigo).then()
                .statusCode(200)
                .body("ativo", equalTo(false))
                .body("desativado_em", notNullValue());
    }

    @Entao("o painel ESG conta {int} equipamentos ativos de {int}")
    public void oPainelContaEquipamentosAtivos(int ativos, int total) {
        contexto.api().get("/api/indicadores").then()
                .statusCode(200)
                .body("governanca.equipamentos_ativos", equalTo(ativos))
                .body("governanca.equipamentos_total", equalTo(total));
    }
}
