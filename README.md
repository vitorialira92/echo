# Echo

Sistema web do Echo (catálogo de músicas), feito com **Spring Boot**, **Spring MVC**, **Thymeleaf**, **Spring Data JPA**, **Spring Security** e banco **MySQL**. Esta entrega contém o catálogo público, o cadastro de usuários, a avaliação de músicas, a área administrativa e a internacionalização da interface (português, inglês e francês), sobre a mesma camada de domínio e repositórios da entrega anterior.

## Funcionalidades

- **Catálogo público**: uma página única lista todos os artistas com filtro por nome; a partir dela navega-se para o perfil do artista (com seus álbuns) e para o álbum (com suas faixas). Cada música exibe a **média pública das notas** e o **sentimento predominante**.
- **Cadastro de usuário**: qualquer visitante pode criar uma conta (papel `USER`).
- **Avaliações**: usuários autenticados avaliam uma música com nota de 1 a 10, um sentimento e uma resenha opcional, e gerenciam (editam/removem) suas próprias avaliações.
- **Área administrativa**: administradores fazem o CRUD de artistas, álbuns e músicas e cadastram novos administradores.
- **Idiomas**: a interface está disponível em **português, inglês e francês**.

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
  { "username": "luna",  "password": "luna123",  "name": "Luna Park",     "role": "USER"  }
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
