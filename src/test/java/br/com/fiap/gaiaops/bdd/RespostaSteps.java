package br.com.fiap.gaiaops.bdd;

import static br.com.fiap.gaiaops.suporte.Contratos.contrato;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

import io.cucumber.java.pt.Entao;

/** Verificacoes comuns a qualquer resposta: status HTTP, contrato e formato de erro. */
public class RespostaSteps {

    private final ContextoDoCenario contexto;

    public RespostaSteps(ContextoDoCenario contexto) {
        this.contexto = contexto;
    }

    @Entao("a API responde com o status {int}")
    public void aApiRespondeComOStatus(int status) {
        contexto.validar().statusCode(status);
    }

    @Entao("a resposta segue o contrato {string}")
    public void aRespostaSegueOContrato(String nomeDoContrato) {
        contexto.validar().body(contrato(nomeDoContrato));
    }

    @Entao("a resposta aponta erro no campo {string}")
    public void aRespostaApontaErroNoCampo(String campo) {
        contexto.validar().body("erros." + campo, not(emptyOrNullString()));
    }

    @Entao("a mensagem de erro contém {string}")
    public void aMensagemDeErroContem(String trecho) {
        contexto.validar().body("detail", containsString(trecho));
    }
}
