# Echo - Implementado para a primeira entrega

Camada de acesso a dados do sistema Echo (catálogo de músicas), feita com **Spring Boot**, **Spring Data JPA** e banco **H2 em memória**. Esta entrega contém as entidades, os repositórios (DAOs) e uma aplicação que executa um CRUD de demonstração ao iniciar.

## Regras de negócio

- **Usuário**: possui `username` único no sistema, senha guardada como **hash BCrypt** (nunca em texto puro), nome e um papel (`ADMIN` ou `USER`).
- **Artista**: nome, país de origem e uma imagem (opcional).
- **Álbum**: pertence a um artista já cadastrado. Não pode haver dois álbuns com o **mesmo título para o mesmo artista**. Tem gênero, ano de lançamento, capa e link do Spotify (opcionais).
- **Música**: pertence a um álbum já cadastrado. Não pode haver duas músicas com o **mesmo número de faixa dentro do mesmo álbum**. Tem duração em segundos, letra, marcação de conteúdo explícito e link do Spotify (opcionais).
- **Avaliação (SongRating)**: um usuário avalia uma música com uma **nota de 1 a 10**, um sentimento (`feeling`) e uma resenha opcional. Cada usuário só pode avaliar a **mesma música uma única vez** — a identidade da avaliação é o par (usuário, música). A data de criação é preenchida **automaticamente** no momento em que a avaliação é salva.
- **Validações gerais**: campos obrigatórios não podem ser nulos/vazios, há limites de tamanho de texto, a nota é restrita ao intervalo 1–10 e os links (capa, imagem, Spotify) são validados como URL quando informados.

## Como rodar

Pré-requisitos: **Java 21** e **Maven**.

Na raiz do projeto:

```bash
mvn spring-boot:run
```

O banco H2 é em memória e criado automaticamente a partir das entidades, então não é preciso instalar nem configurar nada.

## O que acontece ao rodar

A aplicação sobe sem servidor web e o Hibernate gera o schema a partir das classes `@Entity`. Em seguida, uma rotina de inicialização executa um **CRUD completo no console**:

1. **Create** — cria um usuário administrador e um usuário comum (com senha em hash BCrypt), o artista *NewJeans*, o álbum *Get Up*, três músicas e uma avaliação.
2. **Read** — lista todos os usuários, busca artista por id, álbuns por artista, músicas por álbum, músicas por artista (consulta JPQL), avaliações por usuário, a média de nota de uma música, busca de usuário por `username` e faz as checagens de unicidade.
3. **Update** — altera o título de uma música, atualiza uma avaliação (mantendo a data de criação) e muda o nome de um usuário.
4. **Delete** — remove uma avaliação e uma música, e imprime as contagens finais de cada tabela.

Toda a saída é exibida no terminal com separadores indicando cada etapa. Ao final da demonstração, a aplicação encerra.