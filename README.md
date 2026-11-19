# Rota do Fôlego — API REST

**Global Solution 2026/2 · CitizenTech · FIAP — Domain Driven Design Using Java**

Back-end do app **Rota do Fôlego**, que planeja trajetos a pé priorizando pontos de descanso
(bancos, sombras, marquises, bebedouros, banheiros acessíveis) para idosos, gestantes, pessoas com
mobilidade reduzida e cardíacos.

**ODS atendidos:** 11 (Cidades e Comunidades Sustentáveis) · 3 (Saúde e Bem-Estar) · 10 (Redução das Desigualdades)

---

## Integrantes

| Nome | RM | Turma |
|------|----|-------|
| Julia | RM571244 | 1TDSPO |
| Matheus Nezio | RM571702 | 1TDSPO |
| Alexandre Prazeres | RM573059 | 1TDSPO |

---

## Links

| Item | Endereço |
|---|---|
| API no Render | `https://rota-folego-api.onrender.com/` |
| Front (Vercel) | `https://rotafolego.vercel.app/` |
| GitHub | `(https://github.com/Nezio22/GS2026-ROTAFOLEGO)`
| GitHub | `https://github.com/juliaraalmeida77-ux/rota-folego-api` 
| Vídeo (YouTube não listado, até 5 min) | `https://youtu.be/XXXXXXXX` |

> A API no Render (plano gratuito) "dorme" após um tempo parada. A primeira chamada pode demorar cerca de um minuto.

---

## Tecnologias

- Java 17 e Spring Boot 3.3 (Web + Validation)
- **JDBC puro com `PreparedStatement`** (sem JPA, Hibernate ou Spring Data)
- Banco Oracle da FIAP
- ModelMapper (conversão entre DTO e modelo)
- Docker (multi-stage) e deploy no Render

---

## Arquitetura (um pacote por responsabilidade)

```
com.rotafolego
├─ model       → TipoPonto, PontoDescanso, Avaliacao (entidades do domínio)
├─ dao         → TipoPontoDao, PontoDescansoDao, AvaliacaoDao (JDBC + PreparedStatement)
├─ dto         → *Request (entrada + Bean Validation), *Response (saída), ErroResponse
├─ controller  → TipoPontoController, PontoController, AvaliacaoController, StatusController
├─ exception   → RegraNegocioException (409), RecursoNaoEncontradoException (404)
├─ handler     → GlobalExceptionHandler (@RestControllerAdvice → JSON de erro padronizado)
└─ config      → CorsConfig, DaoConfig (DataSource → DAOs), ModelMapperConfig
```

Caminho de uma requisição: **Controller → DTO (validação) → ModelMapper → DAO → Oracle**.

---

## Modelo de dados

O banco tem 6 tabelas (script em `sql/rota_do_folego.sql`):

| Tabela | Descrição |
|---|---|
| `TB_TIPO_PONTO` | Tipos de ponto de descanso (banco, sombra, bebedouro...) |
| `TB_PONTO_DESCANSO` | Pontos de descanso, com endereço, coordenadas e se é coberto |
| `TB_CIDADAO` | Cidadãos que usam o app |
| `TB_AVALIACAO` | Avaliações (nota e comentário) de um cidadão sobre um ponto |
| `TB_TRAJETO` | Trajetos feitos por um cidadão |
| `TB_TRAJETO_PONTO` | Tabela associativa que resolve o N:N entre trajeto e ponto |

Relacionamentos:

- Tipo de Ponto **1:N** Ponto de Descanso
- Cidadão **1:N** Avaliação
- Ponto de Descanso **1:N** Avaliação
- Cidadão **1:N** Trajeto
- Trajeto **N:N** Ponto de Descanso (via `TB_TRAJETO_PONTO`)

---

## Como rodar

### 1. Banco de dados

Rode o script `sql/rota_do_folego.sql` no Oracle da FIAP (SQL Developer → Executar Script / F5).
Ele cria as tabelas, as **sequences** (`sq_tb_*`, começando em 100) e os dados de exemplo.

> As linhas de `DROP` do script ficam comentadas (`--`). Descomente só se precisar recriar as tabelas.

### 2. Variáveis de ambiente

**Nenhuma senha fica no código.** Defina as variáveis (veja `.env.example`):

| Variável | Exemplo |
|---|---|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USER` | `RM00000` (seu RM) |
| `DB_PASSWORD` | _(sua senha)_ |
| `CORS_ORIGINS` | `http://localhost:5173,https://SEU-FRONT.vercel.app` |

### 3. Subir a API

**Pelo terminal (precisa de JDK 17+ e Maven):**

```bash
# Linux/macOS
export DB_URL=... DB_USER=... DB_PASSWORD=...
mvn spring-boot:run
```

```powershell
# Windows PowerShell
$env:DB_URL="..."; $env:DB_USER="..."; $env:DB_PASSWORD="..."
mvn spring-boot:run
```

**Pelo IntelliJ IDEA:**

1. Abra a pasta do projeto (`File → Open`) e aguarde o Maven carregar as dependências.
2. Em `File → Project Structure`, escolha um JDK 17 ou 21.
3. Abra a classe `RotaFolegoApplication` e rode o `main`.
4. Em `Edit Configurations → Environment variables`, informe as variáveis:
   `DB_URL=...;DB_USER=...;DB_PASSWORD=...`

### 4. Conferir

- `GET http://localhost:8080/` → `{"api":"Rota do Folego","status":"no ar"}`
- `GET http://localhost:8080/pontos` → lista dos 16 pontos de exemplo

### Docker

```bash
docker build -t rota-folego-api .
docker run -p 8080:8080 \
  -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... \
  -e CORS_ORIGINS=http://localhost:5173 \
  rota-folego-api
```

---

## Deploy no Render

1. Suba o código no GitHub (sem `target/` e sem `.env`).
2. No Render: **New + → Web Service** → escolha o repositório → **Runtime: Docker** (usa o `Dockerfile`).
3. Em **Environment**, crie `DB_URL`, `DB_USER`, `DB_PASSWORD` e `CORS_ORIGINS` (URL da Vercel + `http://localhost:5173`).
4. Depois do deploy, abra `https://SEU-SERVICO.onrender.com/pontos` em aba anônima para conferir.

---

## Endpoints

| Método | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | `/tipos` | 201 + Location | 400 · 409 |
| GET | `/tipos` · `/tipos/{id}` | 200 | 404 (por id) |
| PUT | `/tipos/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/tipos/{id}` | 204 | 404 · 409 |
| POST | `/pontos` | 201 + Location | 400 · 409 |
| GET | `/pontos` | 200 | — |
| GET | `/pontos/{id}` | 200 | 404 |
| PUT | `/pontos/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/pontos/{id}` | 204 | 404 · 409 |
| GET | `/pontos/{id}/avaliacoes` | 200 | 404 |
| POST | `/pontos/{id}/avaliacoes` | 201 + Location | 400 · 404 · 409 |
| GET | `/avaliacoes` · `/avaliacoes/{id}` | 200 | 404 (por id) |
| PUT | `/avaliacoes/{id}` | 200 | 400 · 404 |
| DELETE | `/avaliacoes/{id}` | 204 | 404 |

Método não suportado → **405**. Erro inesperado → **500** com mensagem genérica.

### Exemplos de JSON

`POST /pontos`

```json
{
  "nmPonto": "Banco da Praca Ramos",
  "dsEndereco": "Praca Ramos de Azevedo - Republica, Sao Paulo",
  "vlLatitude": -23.5456,
  "vlLongitude": -46.6391,
  "coberto": false,
  "cdTipo": 1
}
```

`GET /pontos/1` → 200

```json
{
  "cdPonto": 1,
  "nmPonto": "Banco da Praca da Republica",
  "dsEndereco": "Praca da Republica - Republica, Sao Paulo",
  "vlLatitude": -23.5433,
  "vlLongitude": -46.6421,
  "coberto": false,
  "dtCadastro": "2026-09-01T00:00:00",
  "cdTipo": 1,
  "tipo": "banco",
  "notaMedia": 4.5
}
```

`POST /pontos/4/avaliacoes`

```json
{ "nrNota": 5, "dsComentario": "Banco confortavel", "cdCidadao": 8 }
```

Erro padronizado (400):

```json
{
  "timestamp": "2026-11-12T10:15:30",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados de entrada invalidos",
  "caminho": "/pontos",
  "detalhes": [
    { "campo": "nmPonto", "mensagem": "nmPonto e obrigatorio" }
  ]
}
```

---

## Regras de negócio e validações

- **Regra principal:** um cidadão **não pode avaliar o mesmo ponto duas vezes** → `RegraNegocioException` → **409**
  (também protegido pela constraint `UNIQUE (cd_cidadao, cd_ponto)` no banco).
- Não pode haver dois pontos com o mesmo nome e endereço → 409.
- Remover tipo ou ponto que possui registros dependentes → 409.
- `PontoRequest`: `nmPonto` e `dsEndereco` com `@NotBlank/@Size`, `coberto` com `@NotNull`,
  `cdTipo` com `@NotNull @Positive`, latitude e longitude dentro do intervalo válido.
- `AvaliacaoRequest`: `nrNota` com `@NotNull @Min(1) @Max(5)`, `cdCidadao` com `@NotNull @Positive`,
  `dsComentario` com `@Size(max=200)`.

---

## Testes realizados (Postman)

| # | Requisição | Resultado esperado | O que valida |
|---|---|---|---|
| 1 | `GET /pontos` | 200 | Lista todos os pontos |
| 2 | `POST /pontos` | 201 + header `Location` | Criação de ponto |
| 3 | `POST /pontos` com `nmPonto` vazio | 400 com lista de campos | Validação de entrada |
| 4 | `GET /pontos/9999` | 404 | Ponto inexistente |
| 5 | `POST /pontos/1/avaliacoes` (cidadão 1) | 409 | Avaliação duplicada |
| 6 | `POST /pontos/4/avaliacoes` (cidadão 8) | 201 | Avaliação válida |
| 7 | `PUT /pontos/{id}` | 200 | Atualização de ponto |
| 8 | `DELETE /pontos/{id}` | 204 | Remoção de ponto |
| 9 | `DELETE /pontos/1` | 409 | Ponto com avaliações não pode ser removido |
| 10 | `PATCH /pontos` | 405 | Método não permitido |

---

## Declaração de uso de IA

Partes deste projeto foram geradas com apoio de IA (Claude) e depois lidas, testadas e ajustadas pelo grupo.
