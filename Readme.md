# NF-e Ouro (Nota Fiscal Eletrônica do Ouro)

Este é um repositório de exemplo, em Java, de integração da Nota Fiscal Eletrônica do Ouro com os serviços disponibilizados pela respectiva API.

## Dependências

As dependências para este projeto encontram-se no arquivo [pom.xml](pom.xml). </br>
A assinatura foi realizada por biblioteca padrão do Java 17. Outras versões do Java não foram testadas.

## Uso

A [classe](src/main/java/gov/rfb/nfeouro/model/NotaOuroCliente.java) que representa o cliente retorna apenas o corpo da resposta HTTP às requisições.
O tratamento das respostas deve ser realizado na implementação do cliente pelos usuários.
Os exemplos de respostas podem ser encontrados no [*Swagger*](https://hom-nfoe.estaleiro.serpro.gov.br/API/swagger/index.html) da API.</br></br>
Exemplos de uso encontram-se na classe de [Testes](src/test/java/gov/rfb/Testes.java).

## Clientes em outras linguagens

O cliente em Python 3 pode ser encontrado em: </br>
[https://github.com/rscarvalho90/NF-e-Ouro-Python](https://github.com/rscarvalho90/NF-e-Ouro-Python)

O cliente em Node.js (TypeScript) pode ser encontrado em: </br>
[https://github.com/rscarvalho90/NF-e-Ouro-Node.js](https://github.com/rscarvalho90/NF-e-Ouro-Node.js)