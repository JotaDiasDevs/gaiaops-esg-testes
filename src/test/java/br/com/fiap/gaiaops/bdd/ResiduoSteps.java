package br.com.fiap.gaiaops.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Map;

import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

/** Pilares E e S: coleta seletiva, destinacao dos residuos e painel ESG. */
public class ResiduoSteps {

    private final ContextoDoCenario contexto;

    public ResiduoSteps(ContextoDoCenario contexto) {
        this.contexto = contexto;
    }

    @Quando("consulto o painel ESG do município")
    public void consultoOPainelEsg() {
        contexto.registrar(contexto.api().get("/api/indicadores"));
    }

    @Quando("consulto o resumo de resíduos por categoria")
    public void consultoOResumoDeResiduos() {
        contexto.registrar(contexto.api().get("/api/residuos/resumo"));
    }

    @Quando("consulto as coletas da categoria {string}")
    public void consultoAsColetasDaCategoria(String categoria) {
        contexto.registrar(contexto.api().queryParam("categoria", categoria).get("/api/residuos"));
    }

    @Entao("o total de resíduos coletados é de {int} kg")
    public void oTotalDeResiduosColetados(int quilos) {
        contexto.validar().body("ambiental.residuos_coletados_kg", closeTo(quilos, 0.01));
    }

    @Entao("a taxa de desvio de aterro é de {double}%")
    public void aTaxaDeDesvioDeAterro(double percentual) {
        contexto.validar().body("ambiental.taxa_desvio_de_aterro_pct", closeTo(percentual, 0.01));
    }

    @Entao("a categoria {string} com destinação {string} soma {int} kg")
    public void aCategoriaComDestinacaoSoma(String categoria, String destinacao, int quilos) {
        List<Map<String, Object>> linhas = contexto.resposta().jsonPath().getList(
                "findAll { it.categoria == '%s' && it.destinacao == '%s' }".formatted(categoria, destinacao));
        assertThat(linhas, hasSize(1));
        assertThat(((Number) linhas.get(0).get("peso_kg")).intValue(), equalTo(quilos));
    }

    @Entao("nenhuma coleta é retornada")
    public void nenhumaColetaERetornada() {
        contexto.validar().body("$", empty());
    }
}
