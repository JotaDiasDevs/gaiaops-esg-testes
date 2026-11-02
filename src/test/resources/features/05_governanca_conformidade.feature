# language: pt
@governanca @conformidade
Funcionalidade: Conformidade regulatória e rastreabilidade
  Como auditor de compliance ambiental
  Quero que licenças vencidas sejam sinalizadas e que cada versão implantada seja rastreável
  Para que a prefeitura não opere equipamentos fora da lei e saiba o que está em produção

  @positivo
  Cenário: Licença vencida é sinalizada mesmo com a situação desatualizada no cadastro
    Dado que a data de referência é 01/10/2026
    Quando consulto as licenças que vencem nos próximos 180 dias
    Então a API responde com o status 200
    E a resposta segue o contrato "licencas-vencimento"
    E a licença "LP-11.204/2025" aparece como vencida há 16 dias
    E a licença "LO-45.678/2023" não aparece na lista

  @positivo
  Cenário: Licença de operação entra no radar de renovação perto do vencimento
    Dado que a data de referência é 01/09/2027
    Quando consulto as licenças que vencem nos próximos 180 dias
    Então a API responde com o status 200
    E a licença "LO-45.678/2023" aparece a vencer em 90 dias

  @positivo @rastreabilidade
  Cenário: Versão implantada é rastreável
    Quando consulto as informações da versão implantada
    Então a API responde com o status 200
    E a resposta segue o contrato "info"
    E a resposta identifica a aplicação "GaiaOps ESG" na versão "1.0.0"

  @negativo
  Cenário: Prazo negativo não esconde licenças já vencidas
    Dado que a data de referência é 01/10/2026
    Quando consulto as licenças que vencem nos próximos -30 dias
    Então a API responde com o status 200
    E a licença "LP-11.204/2025" aparece como vencida há 16 dias
