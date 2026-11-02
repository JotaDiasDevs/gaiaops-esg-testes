package br.com.fiap.gaiaops.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Pilar E: telemetria dos sensores urbanos e alertas automaticos. */
public class MedicaoSteps {

    private final ContextoDoCenario contexto;

    public MedicaoSteps(ContextoDoCenario contexto) {
        this.contexto = contexto;
    }

    @Quando("o sensor do equipamento {string} envia uma leitura de qualidade do ar com IQAr {int}")
    public void oSensorEnviaLeituraDeQualidadeDoAr(String equipamento, int iqar) {
        enviar(equipamento, "qualidade_ar", Map.of("indice_iqar", iqar), null);
    }

    /** Tabela com cabecalho: bloco (leitura | referencia) | campo | valor. */
    @Quando("o sensor do equipamento {string} envia uma leitura de {string} com:")
    public void oSensorEnviaLeitura(String equipamento, String tipo, List<Map<String, String>> linhas) {
        Map<String, Object> leitura = new LinkedHashMap<>();
        Map<String, Object> referencia = new LinkedHashMap<>();
        for (Map<String, String> linha : linhas) {
            Map<String, Object> bloco = "referencia".equals(linha.get("bloco")) ? referencia : leitura;
            bloco.put(linha.get("campo"), Double.valueOf(linha.get("valor")));
        }
        enviar(equipamento, tipo, leitura, referencia.isEmpty() ? null : referencia);
    }

    @Entao("a leitura gera um alerta de nível {string}")
    public void aLeituraGeraUmAlertaDeNivel(String nivel) {
        contexto.validar()
                .body("alerta.nivel", equalTo(nivel))
                .body("alerta.reconhecido", equalTo(false));
    }

    @Entao("a leitura é registrada sem alerta")
    public void aLeituraERegistradaSemAlerta() {
        contexto.validar().body("alerta", nullValue());
    }

    @Entao("o alerta aparece entre os alertas abertos de nível {string}")
    public void oAlertaApareceEntreOsAlertasAbertos(String nivel) {
        String id = contexto.resposta().path("_id");
        contexto.api()
                .queryParam("nivel", nivel)
                .queryParam("somente_abertos", true)
                .get("/api/medicoes/alertas").then()
                .statusCode(200)
                .body("_id", hasItem(id));
    }

    @Entao("o painel ESG passa a ter {int} alertas abertos")
    public void oPainelPassaATerAlertasAbertos(int quantidade) {
        contexto.api().get("/api/indicadores").then()
                .statusCode(200)
                .body("ambiental.alertas_abertos", equalTo(quantidade));
    }

    private void enviar(String equipamento, String tipo, Map<String, Object> leitura, Map<String, Object> referencia) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("equipamento_id", equipamento);
        corpo.put("tipo", tipo);
        corpo.put("sensor", Map.of("id", "SEN-TESTE-BDD"));
        corpo.put("leitura", leitura);
        if (referencia != null) {
            corpo.put("referencia", referencia);
        }
        contexto.registrar(contexto.api().body(corpo).post("/api/medicoes"));
    }
}
