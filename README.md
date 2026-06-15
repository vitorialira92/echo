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

> Para começar de um banco limpo a cada execução, troque `ddl-auto` para `create-drop` no `application.properties`.

### Como rodar

Na raiz do projeto:

```bash
mvn spring-boot:run
```

Depois, abra o navegador em **http://localhost:8080**.

### Usuários populados na inicialização

Ao subir pela primeira vez, uma rotina de inicialização cria **dados de exemplo** (caso ainda não existam): o artista *NewJeans*, o álbum *Get Up*, três músicas, uma avaliação e os dois usuários abaixo (senhas em hash BCrypt):

| Usuário | Senha      | Papel   | Acesso                                              |
|---------|------------|---------|-----------------------------------------------------|
| `admin` | `admin123` | `ADMIN` | Área administrativa (catálogo e novos admins)       |
| `luna`  | `luna123`  | `USER`  | Avaliar músicas e gerenciar as próprias avaliações  |

### Como navegar

- **Catálogo público**: `http://localhost:8080/` (raiz, redireciona para o catálogo).
- **Entrar**: link **Entrar** no topo, ou `http://localhost:8080/login`.
- **Criar conta**: link **Criar conta** no topo, ou `http://localhost:8080/signup`.
- **Minhas avaliações** (logado como `USER`): link **Minhas avaliações** no topo.
- **Área administrativa** (logado como `ADMIN`): link **Estúdio** no topo, ou `http://localhost:8080/admin`.

### Idiomas (i18n)

A interface tem um seletor **EN · PT · FR** no topo. Também é possível trocar pela URL, com o parâmetro `lang`: `?lang=pt`, `?lang=en` ou `?lang=fr` (por exemplo, `http://localhost:8080/catalog?lang=fr`). O idioma padrão é o inglês.
