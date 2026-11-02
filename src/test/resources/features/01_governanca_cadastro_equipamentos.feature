# language: pt
@governanca @equipamentos
Funcionalidade: Cadastro íntegro dos equipamentos públicos
  Como gestor de governança da prefeitura de Vila Nova do Horizonte
  Quero que o cadastro de equipamentos públicos só aceite dados válidos
  Para que todos os indicadores ESG da cidade sejam calculados sobre dados confiáveis

  Contexto:
    Dado que a base ESG do município está carregada com 12 equipamentos

  @positivo
  Cenário: Cadastrar uma nova escola municipal
    Quando o gestor cadastra o equipamento:
      | codigo             | EQP-ESC-002            |
      | nome               | EMEF Jardim das Flores |
      | tipo               | escola                 |
      | distrito           | Morro Alto             |
      | populacao_atendida | 640                    |
    Então a API responde com o status 201
    E a resposta segue o contrato "equipamento"
    E o equipamento "EQP-ESC-002" pode ser consultado e está ativo
    E o painel ESG conta 13 equipamentos ativos de 13

  @negativo
  Esquema do Cenário: Recusar cadastro que fere as regras de integridade (<motivo>)
    Quando o gestor cadastra o equipamento com código "<codigo>", nome "<nome>" e tipo "<tipo>"
    Então a API responde com o status 400
    E a resposta segue o contrato "problema-validacao"
    E a resposta aponta erro no campo "<campo>"
    E o painel ESG conta 12 equipamentos ativos de 12

    Exemplos:
      | motivo                 | codigo      | nome      | tipo     | campo  |
      | código fora do padrão  | escola-2    | EMEF Nova | escola   | codigo |
      | tipo inexistente       | EQP-SHP-001 | Shopping  | shopping | tipo   |
      | nome curto demais      | EQP-ESC-003 | AB        | escola   | nome   |

  @negativo
  Cenário: Impedir dois equipamentos com o mesmo código
    Quando o gestor cadastra o equipamento com código "EQP-ESC-001", nome "Outra escola" e tipo "escola"
    Então a API responde com o status 409
    E a resposta segue o contrato "problema"
    E a mensagem de erro contém "EQP-ESC-001"

  @positivo @rastreabilidade
  Cenário: Desativar um equipamento preserva o histórico
    Quando o gestor desativa o equipamento "EQP-ILU-001"
    Então a API responde com o status 204
    E o equipamento "EQP-ILU-001" continua registrado, porém inativo
    E o painel ESG conta 11 equipamentos ativos de 12
