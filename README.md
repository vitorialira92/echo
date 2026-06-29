# Echo

Sistema web do Echo (catálogo de músicas), feito com **Spring Boot**, **Spring MVC**, **Thymeleaf**, **Spring Data JPA**, **Spring Security** e banco **MySQL**. Esta entrega acrescenta uma **API REST** (sob `/api`) sobre a aplicação MVC já existente, mantendo o catálogo público, o cadastro de usuários, a avaliação de músicas, a área administrativa e a internacionalização da interface (português, inglês e francês) exatamente como na entrega anterior, e reaproveitando a mesma camada de domínio, serviços e repositórios.

## Funcionalidades

- **Catálogo público**: uma página única lista todos os artistas com filtro por nome; a partir dela navega-se para o perfil do artista (com seus álbuns) e para o álbum (com suas faixas). Cada música exibe a **média pública das notas** e o **sentimento predominante**.
- **Cadastro de usuário**: qualquer visitante pode criar uma conta (papel `USER`).
- **Avaliações**: usuários autenticados avaliam uma música com nota de 1 a 10, um sentimento e uma resenha opcional, e gerenciam (editam/removem) suas próprias avaliações.
- **Área administrativa**: administradores fazem o CRUD de artistas, álbuns e músicas e cadastram novos administradores.
- **Idiomas**: a interface está disponível em **português, inglês e francês**.
- **API REST**: além da interface web, o sistema expõe uma API REST em JSON sob `/api` para usuários, artistas, álbuns, músicas e avaliações, com autenticação HTTP Basic. Veja a seção [API REST](#api-rest).

## Regras de negócio

- **Usuário**: possui `username` único no sistema, senha guardada como **hash BCrypt** (nunca em texto puro), nome e um papel (`ADMIN` ou `USER`).
- **Artista**: nome, país de origem e uma imagem (opcional).
- **Álbum**: pertence a um artista já cadastrado. Não pode haver dois álbuns com o **mesmo título para o mesmo artista**. Tem gênero, ano de lançamento, capa e link do Spotify (opcionais).
- **Música**: pertence a um álbum já cadastrado. Não pode haver duas músicas com o **mesmo número de faixa dentro do mesmo álbum**. Tem duração em segundos, letra, marcação de conteúdo explícito e link do Spotify (opcionais).
- **Avaliação (SongRating)**: um usuário avalia uma música com uma **nota de 1 a 10**, um sentimento (`feeling`) e uma resenha opcional. Cada usuário só pode avaliar a **mesma música uma única vez**. A data de criação é preenchida **automaticamente** no momento em que a avaliação é salva.
- **Validações gerais**: campos obrigatórios não podem ser nulos/vazios, há limites de tamanho de texto, a nota é restrita ao intervalo 1–10 e os links (capa, imagem, Spotify) são validados como URL quando informados.

## Controle de acesso

- **Público (sem login)**: navega por todo o catálogo, mas não avalia músicas.
- **`USER`**: tudo do público, mais avaliar músicas e gerenciar as próprias avaliações.
- **`ADMIN`**: gerencia o catálogo (artistas, álbuns e músicas) e cadastra novos administradores.

## API REST

Além da interface web em Thymeleaf, esta entrega expõe uma **API REST** em JSON sob o prefixo `/api`, implementada com **controladores REST do Spring MVC** sobre os mesmos serviços e repositórios **Spring Data JPA** que já sustentam a aplicação. A interface web (com login por formulário e sessão) continua funcionando exatamente como antes, porque a API vive em uma cadeia de segurança própria e independente.

Os corpos de requisição e de resposta são sempre JSON. As respostas nunca devolvem a entidade JPA diretamente: cada recurso tem um DTO próprio, e o **hash da senha do usuário jamais é exposto**. Quando um recurso referencia outro (um álbum referencia o artista, uma música referencia o álbum, uma avaliação referencia o usuário e a música), essa referência sai apenas como `id`.

### Autenticação e autorização

A API é **stateless** e usa **HTTP Basic** (nome de usuário e senha em cada requisição), no lugar do login por formulário e sessão da interface web; por isso o CSRF fica desabilitado apenas nas rotas `/api/**`. As regras de acesso seguem os requisitos do sistema:

- **Leitura pública do catálogo (R6)**: listar e visualizar artistas, álbuns e músicas, além da média pública de uma música (`/api/ratings/avg/{songId}`), não exige login.
- **Escrita do catálogo (R1 a R3)**: criar, atualizar e remover artistas, álbuns e músicas exige o perfil **`ADMIN`**.
- **Avaliações (R7 e R8)**: registrar, atualizar, remover e listar avaliações exige o perfil **`USER`**.
- **Usuários**: criar é público (R5), enquanto listar, ver, atualizar e remover são operações administrativas (`ADMIN`). A criação de administradores (R4) é tratada à parte, como explicado em [Decisão de projeto nos endpoints de usuário](#decisão-de-projeto-nos-endpoints-de-usuário).

Para testar, use os usuários do seed: **`admin` / `admin123`** (perfil `ADMIN`) e **`luna` / `luna123`** (perfil `USER`).

Quem chama uma rota protegida sem credenciais válidas recebe **401**; quem está autenticado mas sem o perfil necessário recebe **403**. Nos dois casos o corpo é um JSON de erro no mesmo formato das demais respostas.

### Endpoints

**Usuários**

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/users` | Público (criar `ADMIN` exige `ADMIN`) | Cria um usuário |
| GET | `/api/users` | `ADMIN` | Lista os usuários |
| GET | `/api/users/{id}` | `ADMIN` | Retorna o usuário de id `{id}` |
| PUT | `/api/users/{id}` | `ADMIN` | Atualiza o usuário de id `{id}` |
| DELETE | `/api/users/{id}` | `ADMIN` | Remove o usuário de id `{id}` |

**Artistas**

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/artists` | `ADMIN` | Cria um artista |
| GET | `/api/artists` | Público | Lista os artistas |
| GET | `/api/artists/{id}` | Público | Retorna o artista de id `{id}` |
| PUT | `/api/artists/{id}` | `ADMIN` | Atualiza o artista de id `{id}` |
| DELETE | `/api/artists/{id}` | `ADMIN` | Remove o artista de id `{id}` |

**Álbuns**

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/albums/artists/{id}` | `ADMIN` | Cria um álbum para o artista de id `{id}` |
| GET | `/api/albums/artists/{id}` | Público | Lista os álbuns do artista de id `{id}` |
| GET | `/api/albums/{id}` | Público | Retorna o álbum de id `{id}` |
| PUT | `/api/albums/{id}` | `ADMIN` | Atualiza o álbum de id `{id}` |
| DELETE | `/api/albums/{id}` | `ADMIN` | Remove o álbum de id `{id}` |

**Músicas**

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/songs/albums/{id}` | `ADMIN` | Cria uma música no álbum de id `{id}` |
| GET | `/api/songs/albums/{id}` | Público | Lista as músicas do álbum de id `{id}` |
| GET | `/api/songs/artists/{id}` | Público | Lista as músicas do artista de id `{id}` |
| GET | `/api/songs/{id}` | Público | Retorna a música de id `{id}` |
| PUT | `/api/songs/{id}` | `ADMIN` | Atualiza a música de id `{id}` |
| DELETE | `/api/songs/{id}` | `ADMIN` | Remove a música de id `{id}` |

**Avaliações**

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/ratings/{userId}` | `USER` | Lista as avaliações do usuário de id `{userId}` |
| GET | `/api/ratings/avg/{songId}` | Público | Média e sentimento predominante da música de id `{songId}` |
| POST | `/api/ratings/{songId}` | `USER` | Registra a avaliação do usuário autenticado para a música `{songId}` |
| PUT | `/api/ratings/{songId}` | `USER` | Atualiza a avaliação do usuário autenticado para a música `{songId}` |
| DELETE | `/api/ratings/{songId}` | `USER` | Remove a avaliação do usuário autenticado para a música `{songId}` |

Nas avaliações o **usuário é sempre o autenticado** (vem das credenciais Basic), nunca do corpo da requisição, e a **data de criação é gerada pelo sistema**. Por isso `POST` e `PUT` recebem somente `rating`, `feeling` e `review`.

### Códigos de status

- **200 OK**: leitura ou atualização bem-sucedida.
- **201 Created**: criação bem-sucedida; o cabeçalho `Location` aponta para o recurso recém-criado.
- **204 No Content**: remoção bem-sucedida, sem corpo de resposta.
- **400 Bad Request**: falha de validação ou JSON malformado. Em falhas de validação, o corpo traz um mapa `fieldErrors` com a mensagem de cada campo.
- **401 Unauthorized**: rota protegida acessada sem credenciais válidas.
- **403 Forbidden**: requisição autenticada, mas sem o perfil necessário (inclui a tentativa de criar um `ADMIN` sem estar autenticado como `ADMIN`).
- **404 Not Found**: recurso inexistente, ou seja, um id que não existe.
- **409 Conflict**: violação de unicidade ou de integridade, como nome de usuário repetido, título de álbum repetido para o mesmo artista, número de faixa repetido no mesmo álbum, segunda avaliação para a mesma música, ou remoção de um registro que ainda tem filhos.
- **500 Internal Server Error**: erro inesperado no servidor.

Todo erro usa o mesmo formato de corpo:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "This artist already has an album with that title.",
  "timestamp": "2025-01-01T12:00:00Z",
  "fieldErrors": {
    "title": "must not be blank"
  }
}
```

O campo `fieldErrors` só aparece nas falhas de validação (400). As mensagens da API são devolvidas **em inglês**: a internacionalização exigida pelo R9 cobre a **interface web** (Thymeleaf), ao passo que a API responde com um erro estruturado em JSON, pensado para ser consumido por outro sistema e não exibido diretamente a um usuário final.

### Decisão de projeto nos endpoints de usuário

O documento de requisitos pede dois comportamentos que, à primeira vista, parecem conflitar no mesmo recurso: qualquer pessoa pode criar uma conta de usuário comum (R5), mas apenas um administrador pode cadastrar novos administradores (R4). Como a tabela de endpoints define um único `POST /api/users`, a solução adotada foi a seguinte:

- **`POST /api/users` é público**. Quando o corpo não informa `role`, ou informa `USER`, a conta é criada como usuário comum, atendendo ao R5.
- Quando o corpo pede `role: "ADMIN"`, o controlador verifica se quem chama está autenticado **como `ADMIN`**. Se estiver, cria o administrador (R4); caso contrário, responde **403**. Assim o mesmo endpoint atende aos dois requisitos sem abrir uma brecha para qualquer pessoa criar administradores.
- As demais operações sobre usuários (listar, ver, atualizar e remover) são **administrativas** e exigem `ADMIN`, já que expõem ou alteram dados de todas as contas.

Na atualização (`PUT /api/users/{id}`), a senha é **opcional**: se vier em branco ou ausente, o hash atual é preservado; se vier preenchida, é regravada como **hash BCrypt**.

### Exemplos com `curl`

Os exemplos abaixo assumem a aplicação rodando em `http://localhost:8080` (veja o [Roteiro de execução](#roteiro-de-execução)) com os usuários do seed.

Leitura pública, sem login:

```bash
# Lista todos os artistas
curl http://localhost:8080/api/artists

# Média e sentimento predominante da música de id 1
curl http://localhost:8080/api/ratings/avg/1
```

Cadastro público de um usuário comum (R5, sem login):

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"novo","password":"senha123","name":"Novo Usuário"}'
```

Operações de administrador, com HTTP Basic usando `admin:admin123`:

```bash
# Cria um artista
curl -X POST http://localhost:8080/api/artists \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"name":"NewJeans","country":"KR","imageUrl":null}'

# Cria um álbum para o artista de id 1
curl -X POST http://localhost:8080/api/albums/artists/1 \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"title":"Get Up","genre":"KPOP","releaseYear":2023,"coverUrl":null,"spotifyUrl":null}'

# Cria uma música no álbum de id 1
curl -X POST http://localhost:8080/api/songs/albums/1 \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"title":"Super Shy","trackNumber":1,"durationSeconds":154,"explicitContent":false}'

# Cria um novo administrador (exige estar autenticado como ADMIN)
curl -X POST http://localhost:8080/api/users \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"username":"admin2","password":"admin234","name":"Segundo Admin","role":"ADMIN"}'
```

Avaliações como usuário comum, com HTTP Basic usando `luna:luna123`:

```bash
# Registra a avaliação da música de id 1 (uma por música)
curl -X POST http://localhost:8080/api/ratings/1 \
  -u luna:luna123 \
  -H "Content-Type: application/json" \
  -d '{"rating":9,"feeling":"EXCITED","review":"Grudenta e viciante!"}'

# Atualiza a avaliação da mesma música
curl -X PUT http://localhost:8080/api/ratings/1 \
  -u luna:luna123 \
  -H "Content-Type: application/json" \
  -d '{"rating":8,"feeling":"HAPPY","review":"Continua ótima."}'

# Lista as avaliações do usuário de id 2
curl -u luna:luna123 http://localhost:8080/api/ratings/2
```

### Coleção Postman

Para facilitar os testes, há uma coleção Postman em `resources/postman/echo-t7.postman_collection.json`. Importe-a no Postman, deixe a aplicação rodando em `http://localhost:8080` e dispare as requisições uma a uma, ou use o Collection Runner para rodar o fluxo completo de ponta a ponta. A coleção já traz no próprio nível dela todas as variáveis necessárias, ou seja, a URL base e as credenciais dos usuários do seed (`admin` e `luna`), então funciona assim que é importada, sem precisar de nenhum environment. As leituras públicas dispensam autenticação, e os identificadores criados ao longo da execução são capturados e reutilizados automaticamente nas requisições seguintes. A execução em sequência cria artista, álbum e música, cria usuários, registra e atualiza uma avaliação, demonstra os casos de 401, 403 e 404 e, por fim, remove tudo na ordem que respeita as restrições de integridade.

## Roteiro de execução

### Pré-requisitos

- **Java 21** e **Maven**.
- Um servidor **MySQL** em execução em `localhost:3306`.

### Banco de dados

- **SGBD**: MySQL.
- **Nome do banco**: `echo`.
- **Scripts SQL**: não é necessário rodar nenhum script manualmente. A URL de conexão usa `createDatabaseIfNotExist=true`, então o banco **`echo`** é **criado automaticamente** na inicialização, e as tabelas são geradas pelo Hibernate a partir das entidades (`spring.jpa.hibernate.ddl-auto=update`).

As credenciais ficam em `src/main/resources/application.properties` (por padrão usuário `root` / senha `123456`). Ajuste-as conforme o seu MySQL.

> Para começar de um banco limpo a cada execução, troque `ddl-auto` para `create-drop` no `application.properties`. Isso também é a forma mais simples de **recarregar os dados de exemplo** depois de editar um arquivo de seed (veja a seção [Dados de exemplo (seed)](#dados-de-exemplo-seed)).

### Como rodar

Na raiz do projeto:

```bash
mvn spring-boot:run
```

Depois, abra o navegador em **http://localhost:8080**.

### Dados de exemplo (seed)

Os dados de exemplo **não** são inseridos por script SQL nem na mão: eles ficam em três arquivos **JSON** em `src/main/resources/seed/`, e são carregados pela própria aplicação na primeira subida pela rotina `DataInitializer`.

| Arquivo | Conteúdo |
|---|---|
| `music_seed.json` | Catálogo completo: artistas → álbuns → faixas. |
| `users_seed.json` | Usuários iniciais. |
| `ratings_seed.json` | Avaliações de exemplo. |

#### Como a carga funciona

- Roda automaticamente na inicialização (`CommandLineRunner`), na ordem **usuários → catálogo → avaliações**.
- **Idempotente por tabela**: cada bloco só roda se a tabela correspondente estiver **vazia** (`count() == 0`). Em subidas seguintes nada é reinserido e nada é duplicado.
- **Consequência prática**: para recarregar depois de editar qualquer seed, comece de um **banco vazio** (veja [Recarregar do zero](#recarregar-do-zero)). Se uma tabela já tem linhas, aquele seed é ignorado por completo.
- As senhas dos usuários são gravadas como **hash BCrypt**; no JSON elas ficam em texto puro só para facilitar o seed.
- Chaves desconhecidas no JSON são **ignoradas** (`@JsonIgnoreProperties`), então dá para acrescentar campos extras sem quebrar a carga.

#### Esquema do `music_seed.json`

Um objeto com a lista `artists`; cada artista tem `albums`, e cada álbum tem `songs`:

```jsonc
{
  "artists": [
    {
      "name": "NewJeans",           // obrigatório (artista sem nome é ignorado)
      "country": "KR",              // enum Country (BR, US, CA, GB, FR, KR, ...) — opcional
      "imageUrl": "https://...",    // opcional
      "albums": [
        {
          "name": "Get Up",         // obrigatório (álbum sem nome é ignorado)
          "year": 2023,             // opcional (ausente -> 0)
          "genre": "KPOP",          // enum Genre (POP, RAP, ROCK, KPOP, MPB, ...) — ausente -> POP
          "coverUrl": "https://...",// opcional
          "spotifyUrl": null,       // opcional
          "songs": [
            {
              "title": "Super Shy", // obrigatório (faixa sem título é ignorada)
              "trackNumber": 1,     // opcional (ver "Saneamento na carga")
              "durationSeconds": 154,// opcional (ausente/<1 -> 1)
              "lyrics": null,       // opcional
              "spotifyUrl": null,   // opcional
              "explicit": false     // opcional (ausente -> false)
            }
          ]
        }
      ]
    }
  ]
}
```

#### Esquema do `users_seed.json`

Uma lista de usuários:

```json
[
  { "username": "admin", "password": "admin123", "name": "Administrador", "role": "ADMIN" },
  { "username": "luna",  "password": "luna123",  "name": "Luna Silva",     "role": "USER"  }
]
```

- `username` é único; `password` vai em texto puro e vira **hash BCrypt** na carga; `role` ∈ {`ADMIN`, `USER`}.

Esses dois usuários são os que sobem por padrão:

| Usuário | Senha      | Papel   | Acesso                                              |
|---------|------------|---------|-----------------------------------------------------|
| `admin` | `admin123` | `ADMIN` | Área administrativa (catálogo e novos admins)       |
| `luna`  | `luna123`  | `USER`  | Avaliar músicas e gerenciar as próprias avaliações  |

#### Esquema do `ratings_seed.json`

Uma lista de avaliações. Cada uma aponta para a música por **nome do artista + título do álbum + título da faixa**:

```json
[
  {
    "username": "luna",
    "artist": "NewJeans",
    "album": "Get Up",
    "song": "Super Shy",
    "rating": 9,
    "feeling": "EXCITED",
    "review": "Grudenta e viciante!"
  }
]
```

- O trio **artista / álbum / faixa** é casado de forma **exata, mas sem diferenciar maiúsculas**. Se não casar com nada do `music_seed.json`, a avaliação é apenas **ignorada com um aviso no log** — não derruba a subida.
- `rating` é inteiro de 1 a 10; `feeling` ∈ {`HAPPY`, `SAD`, `ANGRY`, `EXCITED`, `RELAXED`, `NOSTALGIC`}; `review` é opcional.
- Os campos de enum (`country`, `genre`, `role`, `feeling`) precisam usar **exatamente** um dos valores válidos do enum.

> **Ao trocar o `music_seed.json`** (por exemplo, por um catálogo maior gerado pelo script), confira se o `ratings_seed.json` ainda referencia um artista/álbum/faixa **existente** no novo catálogo; caso contrário, aquela avaliação de exemplo simplesmente não é criada.

#### Saneamento na carga (catálogo)

Para o seed nunca derrubar a aplicação por dado inconsistente, ao inserir o catálogo o `DataInitializer` ajusta automaticamente:

- **Faixa sem número, com número < 1 ou repetido no álbum** → recebe o **menor inteiro positivo ainda não usado** naquele álbum (garante `trackNumber` não nulo, ≥ 1 e único por álbum, respeitando a restrição `(album, track_number)`).
- **Duração ausente ou < 1** → vira **1**.
- **`explicit` ausente** → **false**; **gênero ausente** → **POP**; **ano ausente** → **0**.
- **Artista/álbum sem nome** ou **faixa sem título** → são **ignorados**.

#### Recarregar do zero

Como cada seed só roda com a tabela vazia, para aplicar um seed novo:

- **Opção 1 — MySQL**: `DROP DATABASE echo;`. Na próxima subida a aplicação recria o banco vazio (via `createDatabaseIfNotExist`) e o seed roda de novo.
- **Opção 2 — propriedades**: use `spring.jpa.hibernate.ddl-auto=create-drop` no `application.properties` para zerar o schema a cada execução.

#### Como o `music_seed.json` é gerado

O catálogo é produzido por um script Python (`generate_seed.py`), disponível no diretório **resources/scripts/generate_seed.py**, que consulta **APIs públicas, sem chaves nem login**:

- **Deezer** (`api.deezer.com`) para artistas, álbuns e faixas;
- **MusicBrainz** para o país de origem do artista;
- **opcionalmente** o **Genius** (variável `GENIUS_ACCESS_TOKEN` num `.env`) para letras.

Detalhes úteis:

- Dependências: `pip install requests lyricsgenius musicbrainzngs python-dotenv`.
- Por padrão traz **apenas álbuns** (`INCLUDE_RECORD_TYPES = ("album",)`); para incluir EPs e singles, troque para `("album", "ep", "single")`.
- O Deezer não fornece links do Spotify, então **`spotifyUrl` sai como `null`** em tudo. O campo é opcional e renderiza normalmente vazio — dá para preenchê-lo manualmente depois (artista por artista) sem qualquer efeito sobre a carga.

### Como navegar

- **Catálogo público**: `http://localhost:8080/` (raiz, redireciona para o catálogo).
- **Entrar**: link **Entrar** no topo, ou `http://localhost:8080/login`.
- **Criar conta**: link **Criar conta** no topo, ou `http://localhost:8080/signup`.
- **Minhas avaliações** (logado como `USER`): link **Minhas avaliações** no topo.
- **Área administrativa** (logado como `ADMIN`): link **Estúdio** no topo, ou `http://localhost:8080/admin`.

### Idiomas (i18n)

A interface tem um seletor **EN · PT · FR** no topo. Também é possível trocar pela URL, com o parâmetro `lang`: `?lang=pt`, `?lang=en` ou `?lang=fr` (por exemplo, `http://localhost:8080/catalog?lang=fr`). O idioma padrão é o inglês.
