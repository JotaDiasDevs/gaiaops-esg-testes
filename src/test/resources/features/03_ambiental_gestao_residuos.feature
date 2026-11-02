# language: pt
@ambiental @residuos
Funcionalidade: Gestão de resíduos e reciclagem
  Como coordenador da coleta seletiva
  Quero acompanhar quanto dos resíduos coletados deixa de ir para o aterro
  Para cumprir a Política Nacional de Resíduos Sólidos e valorizar as cooperativas

  @positivo
  Cenário: Painel ESG calcula a taxa de desvio de aterro
    Quando consulto o painel ESG do município
    Então a API responde com o status 200
    E a resposta segue o contrato "painel-esg"
    E o total de resíduos coletados é de 83702 kg
    E a taxa de desvio de aterro é de 49,0%

  @positivo
  Cenário: Resumo por categoria mostra para onde vai cada resíduo
    Quando consulto o resumo de resíduos por categoria
    Então a API responde com o status 200
    E a resposta segue o contrato "residuos-resumo"
    E a categoria "rejeito" com destinação "aterro_sanitario" soma 42200 kg
    E a categoria "organico" com destinação "compostagem" soma 6712 kg

  @negativo
  Cenário: Consulta de categoria sem coletas não inventa dados
    Quando consulto as coletas da categoria "radioativo"
    Então a API responde com o status 200
    E a resposta segue o contrato "residuos"
    E nenhuma coleta é retornada
