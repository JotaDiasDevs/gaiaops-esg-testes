# language: pt
@social @programas
Funcionalidade: Inclusão, diversidade e privacidade nos programas sociais
  Como gestora da Secretaria de Assistência Social
  Quero acompanhar os programas e seus beneficiários sem expor dados pessoais
  Para promover inclusão respeitando a LGPD

  @positivo @inclusao
  Cenário: Programa de qualificação garante recurso de acessibilidade a beneficiário com deficiência
    Quando consulto o programa social "PRG-TR-001"
    Então a API responde com o status 200
    E a resposta segue o contrato "programa-social"
    E o programa atende um beneficiário com deficiência com o recurso "interprete de libras"

  @positivo @lgpd
  Cenário: Nenhum programa expõe dados pessoais dos beneficiários
    Quando consulto todos os programas sociais
    Então a API responde com o status 200
    E a resposta segue o contrato "programas-sociais"
    E nenhum beneficiário expõe os campos "nome, cpf, rg, email, telefone"
    E todo beneficiário é identificado apenas por protocolo

  @positivo
  Cenário: Painel social consolida beneficiários e execução orçamentária
    Quando consulto o painel ESG do município
    Então o painel social soma 25 beneficiários em 11 programas ativos
    E a execução orçamentária dos programas é de 58,5%

  @negativo
  Cenário: Consulta de programa inexistente
    Quando consulto o programa social "PRG-XX-999"
    Então a API responde com o status 404
    E a resposta segue o contrato "problema"
    E a mensagem de erro contém "PRG-XX-999"
