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
(Será detalhado na 2ª entrega)
