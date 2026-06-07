# Echo - Implementado para a primeira entrega

Camada de acesso a dados do sistema Echo (catálogo de músicas), feita com **Spring Boot**, **Spring Data JPA** e banco **MySQL**. Esta entrega contém as entidades, os repositórios (DAOs) e uma aplicação que executa um CRUD de demonstração ao iniciar.

## Regras de negócio

- **Usuário**: possui `username` único no sistema, senha guardada como **hash BCrypt** (nunca em texto puro), nome e um papel (`ADMIN` ou `USER`).
- **Artista**: nome, país de origem e uma imagem (opcional).
- **Álbum**: pertence a um artista já cadastrado. Não pode haver dois álbuns com o **mesmo título para o mesmo artista**. Tem gênero, ano de lançamento, capa e link do Spotify (opcionais).
- **Música**: pertence a um álbum já cadastrado. Não pode haver duas músicas com o **mesmo número de faixa dentro do mesmo álbum**. Tem duração em segundos, letra, marcação de conteúdo explícito e link do Spotify (opcionais).
- **Avaliação (SongRating)**: um usuário avalia uma música com uma **nota de 1 a 10**, um sentimento (`feeling`) e uma resenha opcional. Cada usuário só pode avaliar a **mesma música uma única vez**. A data de criação é preenchida **automaticamente** no momento em que a avaliação é salva.
- **Validações gerais**: campos obrigatórios não podem ser nulos/vazios, há limites de tamanho de texto, a nota é restrita ao intervalo 1–10 e os links(capa, imagem, Spotify) são validados como URL quando informados.

## Pré-requisitos

- **Java 21** e **Maven**.
- Um servidor **MySQL** em execução em `localhost:3306`.

As credenciais ficam em `src/main/resources/application.properties` (por padrão
usuário `root` / senha `123456`). Ajuste-as conforme o seu MySQL.

## Banco criado automaticamente

Não é preciso criar o schema manualmente. A URL de conexão usa `createDatabaseIfNotExist=true`, então o banco **`echo`** é **criado automaticamente na inicialização** caso ainda não exista. As tabelas são geradas pelo Hibernate a partir das entidades `@Entity` (`spring.jpa.hibernate.ddl-auto=update`).

> Para começar de um banco limpo a cada execução, troque `ddl-auto` para
> `create-drop` no `application.properties`.

## Como rodar

Pré-requisitos: **Java 21**, **MySQL** e **Maven**.

Na raiz do projeto:

```bash
mvn spring-boot:run
```

## O que acontece ao rodar

1. O banco `echo` é criado (se necessário) e as tabelas são geradas.
2. Uma rotina de inicialização carrega **dados de exemplo** (um usuário admin e um usuário comum com senha em hash BCrypt, o artista *NewJeans*, o álbum*Get Up*, três músicas e uma avaliação). Se os dados já existirem, a carga é pulada.
3. A aplicação então abre um **menu interativo no console** para executar o CRUD
   (criar, listar/buscar, atualizar e remover) de artistas, álbuns, músicas,
   usuários e avaliações, exercitando os repositórios e as consultas JPQL.

A saída é exibida no terminal com separadores indicando cada etapa.
