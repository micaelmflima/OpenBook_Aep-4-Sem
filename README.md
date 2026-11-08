# PROJETO OPEN BOOK — Banco Comunitário de Livros e Materiais Didáticos

*Instituição:* UniCesumar (Maringá – PR)  
*Curso:* Engenharia de Software (Esoft) — AEP 4º Semestre (1º Bimestre)  
*ODS:* ODS 4 (Educação de Qualidade) e ODS 10 (Redução das Desigualdades)

*Integrantes:* Henrique, Micael e Felipe

---

## 1. Concepção e Objetivo
O *Open Book* visa resolver a disparidade e o desperdício de materiais didáticos entre estudantes, criando um catálogo centralizado e confiável para doação e empréstimo temporário de livros, apostilas e equipamentos de apoio acadêmico.

---

## 2. Requisitos Funcionais

* *RF01:* O sistema deve permitir o cadastro de usuários (estudantes), contendo nome completo, e-mail institucional, curso e telefone de contato.
* *RF02:* O sistema deve permitir o cadastro de itens acadêmicos disponibilizados para doação ou empréstimo, diferenciando-os por tipo: Livro Didático, Apostila e Equipamento de Apoio, cada um com seus atributos específicos.
* *RF03:* O sistema deve permitir que um usuário registre a solicitação de empréstimo ou doação de um item disponível, calculando automaticamente o prazo de devolução de acordo com o tipo do item.
* *RF04:* O sistema deve permitir a atualização do status de um empréstimo (Emprestado, Devolvido, Atrasado ou Doado), mantendo o histórico de movimentações de cada item.
* *RF05:* O sistema deve permitir a consulta do catálogo de itens disponíveis, com filtro por tipo de item, disciplina relacionada e status de disponibilidade.

---

## 3. Planejamento Ágil (Cronograma / Backlog)

| Sprint / Data | Épico | User Story | Responsável |
|---|---|---|---|
| *Sprint 1*<br>05/09 – 19/09 | Gestão de Usuários | COMO UM estudante EU QUERO me cadastrar e gerenciar meu perfil PARA QUE eu possa doar e solicitar materiais na plataforma. | Henrique |
| *Sprint 2*<br>20/09 – 04/10 | Catálogo de Itens | COMO UM estudante EU QUERO cadastrar livros, apostilas e equipamentos que não uso mais PARA QUE outros colegas possam encontrá-los no catálogo. | Micael |
| *Sprint 3*<br>05/10 – 19/10 | Empréstimos e Prazos | COMO UM estudante EU QUERO solicitar o empréstimo de um item PARA QUE eu tenha um prazo de devolução automático de acordo com o tipo de material. | Felipe |
| *Sprint 4*<br>20/10 – 03/11 | Histórico e Status | COMO UM estudante EU QUERO acompanhar o status dos meus empréstimos e doações PARA QUE eu saiba quando devolver um item ou confirmar uma doação. | Henrique, Micael e Felipe |

---

## 4. Justificativa Técnica e Arquitetural
* *Back-end:* Java com Spring Boot (API REST)
* *Banco de Dados:* PostgreSQL
* *Modelagem:* Abstração, Herança, Encapsulamento e Polimorfismo no cálculo de prazos de devolução.

---

## 5. Como Executar

### Requisitos

- JDK 17 ou superior e Maven 3.6.3 ou superior.
- PostgreSQL em execução e acesso para criar um banco e tabelas.
- Acesso à internet na primeira execução para baixar as dependências Maven.

### Banco e configuração

Crie um banco vazio chamado `openbook` com o pgAdmin ou `createdb -U postgres openbook`. Execute o script uma única vez nesse banco:

```powershell
psql -U postgres -d openbook -f DataBase/schema.sql
```

Configure as variáveis de ambiente no terminal. Use o usuário e a senha do seu PostgreSQL; não salve a senha no repositório. A URL e o usuário abaixo são os padrões e podem ser alterados para sua instalação:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/openbook'
$env:DB_USER = 'postgres'
$env:DB_PASSWORD = Read-Host 'Senha do PostgreSQL'
mvn spring-boot:run
```

Abra `http://localhost:8080`. Primeiro cadastre um estudante; depois, um material vinculado a ele. Na aba **Movimentações**, selecione outro estudante e um material disponível para registrar empréstimo ou doação. Os comandos acima são para PowerShell; em Linux/macOS, configure as mesmas variáveis com `export`.

O script cria as tabelas e não deve ser repetido sobre um banco já inicializado. Os dados permanecem no PostgreSQL entre reinicializações. Para ajustar os prazos, defina `PRAZO_LIVRO`, `PRAZO_APOSTILA` e `PRAZO_EQUIPAMENTO` com números inteiros positivos antes de iniciar; os padrões são 14, 14 e 7 dias, respectivamente.

### Regras implementadas

- Apenas materiais disponíveis podem ser solicitados. Um empréstimo pode passar a atrasado ou devolvido; um empréstimo atrasado pode ser devolvido. A devolução disponibiliza novamente o material.
- Doações são definitivas e não geram prazo de devolução. Toda movimentação registra seu estado inicial e as alterações posteriores.
- Materiais e estudantes com registros vinculados não podem ser excluídos, preservando o histórico. A disponibilidade é controlada pelas movimentações, não pela edição do material.
- O e-mail precisa ter formato válido. A aplicação não verifica se o domínio é institucional, pois o projeto não define uma lista de domínios permitidos.

### API REST

Todas as rotas começam com `/api` e usam JSON. Erros de validação, registro inexistente e conflitos retornam, respectivamente, HTTP 400, 404 e 409, com uma mensagem no campo `erro`.

| Método | Rota | Uso |
|---|---|---|
| GET, POST | `/usuarios` | Listar e cadastrar estudantes |
| GET, PUT, DELETE | `/usuarios/{id}` | Consultar, atualizar e excluir estudante |
| GET | `/usuarios/{id}/movimentacoes` | Consultar estudante e suas movimentações |
| GET, POST | `/itens` | Listar e cadastrar materiais; GET aceita `tipo`, `disciplina` e `disponivel` |
| GET, PUT, DELETE | `/itens/{id}` | Consultar, atualizar e excluir material |
| GET, POST | `/movimentacoes` | Listar e registrar movimentações |
| GET | `/movimentacoes/{id}` | Consultar movimentação e histórico |
| PATCH | `/movimentacoes/{id}/status` | Alterar status para `ATRASADO` ou `DEVOLVIDO` quando permitido |

Exemplo de cadastro de estudante no sistema:

```json
{"nome":"Ana Silva","email":"ana@universidade.edu.br","curso":"Engenharia de Software","telefone":"44999999999"}
```

Exemplo de item (`usuarioId` deve ser um estudante já cadastrado):

```json
{"usuarioId":1,"titulo":"Introdução a algoritmos","tipo":"LIVRO","disciplina":"Programação","numeracao":"1","edicao":"3"}
```

Para apostila use `tipo: "APOSTILA"` e `numeroPaginas` positivo; para equipamento use `tipo: "EQUIPAMENTO"`, `tipoEquipamento` e `estadoConservacao`. Para criar movimentação envie, por exemplo, `{"usuarioId":2,"itemId":1,"tipo":"EMPRESTIMO"}`. Para alterar status envie `{"status":"DEVOLVIDO"}` na rota PATCH.

### Testes e diagramas

Execute `mvn test` e `mvn package`. Os testes de integração com PostgreSQL ficam desativados por padrão; para executá-los, configure as variáveis de conexão para um **banco de testes isolado** com o script aplicado e defina `$env:OPENBOOK_TEST_DB = 'true'` antes de executar `mvn test`. Eles usam transações revertidas ao final de cada teste.

O desenho atualizado das classes e tabelas está em [Docs/modelagem.md](Docs/modelagem.md). As imagens anteriores permanecem na pasta `Docs/images` como registro da primeira modelagem.

### Estilos e fotografia

O CSS gerado do Tailwind acompanha o projeto; Node.js não é necessário apenas para executar o OpenBook. Para alterar as classes ou o arquivo `src/main/frontend/tailwind.css`, instale Node.js, rode `npm install` e depois `npm run build:css`. Durante o desenvolvimento, `npm run watch:css` atualiza o CSS a cada alteração. O CSS próprio da interface está em `src/main/resources/static/style.css`.

Fotografia da estante: [Peter Barrett Bryan no Unsplash](https://unsplash.com/photos/books-neatly-stacked-on-a-wooden-bookshelf-p-diyGc2ldk), usada de acordo com a [licença do Unsplash](https://unsplash.com/license).
