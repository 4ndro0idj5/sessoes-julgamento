# Sessoes de Julgamento

Aplicacao web para consulta publica e gerenciamento de sessoes de julgamento, pautas e documentos.

## Funcionalidades

- Consulta publica de sessoes por texto e intervalo de datas.
- Exibicao das sessoes do mes atual por padrao.
- Cadastro, edicao, cancelamento e reativacao de sessoes.
- Upload de pautas, aditamentos, pautas de mesa e preferencias.
- Autenticacao com JWT e senhas protegidas por BCrypt.
- Perfis ADMIN e GESTOR.
- Gerenciamento de usuarios restrito a administradores.
- Migrations de banco de dados com Flyway.

## Tecnologias

- Java 17+
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL 16
- Flyway
- Maven
- HTML, CSS e JavaScript
- Docker Compose

## Estrutura do projeto

~~~text
src/main/java/br/jus/sessoes/
|-- config/          Configuracoes do Spring e seguranca
|-- controller/      Endpoints REST
|-- domain/          Entidades e enums
|-- dto/             Objetos de entrada e saida
|-- repository/      Repositorios JPA
|-- security/        JWT e contexto de autenticacao
\-- service/         Regras de negocio

src/main/resources/
|-- application.properties
|-- application-dev.properties
|-- db/
|   |-- migration/   Migrations executadas em todos os ambientes
|   \-- devdata/     Dados ficticios exclusivos do perfil dev
\-- static/
    |-- css/
    |-- js/
    |-- index.html
    |-- login.html
    |-- admin.html
    \-- usuarios.html

uploads/             Documentos enviados, fora do Git e do JAR
~~~

## Pre-requisitos

- Java 17 ou superior
- Maven 3.9 ou superior
- Docker e Docker Compose

Verifique as instalacoes:

~~~bash
java -version
mvn -version
docker --version
docker compose version
~~~

## Configuracao do ambiente

Crie o arquivo local a partir do modelo:

~~~bash
cp .env.example .env
~~~

Variaveis necessarias:

~~~dotenv
DATABASE_URL=jdbc:postgresql://localhost:5432/sessoes_julgamento
DATABASE_USERNAME=sessoes_user
DATABASE_PASSWORD=sessoes_password
JWT_SECRET=gere-uma-chave-longa-e-aleatoria
UPLOAD_DIR=uploads
~~~

Gere uma chave JWT segura:

~~~bash
openssl rand -base64 64
~~~


## Executar localmente

Inicie o PostgreSQL:

~~~bash
docker compose up -d postgres
~~~


Inicie a aplicacao:

~~~bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
~~~

Acesse:

- Pagina pública: http://localhost:8080/
- Login: http://localhost:8080/login.html
- Gestao de sessoes: http://localhost:8080/admin.html
- Gestao de usuarios: http://localhost:8080/usuarios.html



Credenciais locais:

~~~text
Login: admin@localhost
Senha: Admin@123
~~~



## Perfis de acesso

### ADMIN

- Gerencia sessoes e documentos.
- Cadastra e edita usuarios.
- Define perfis e ativa ou desativa usuarios.

### GESTOR

- Gerencia sessoes e documentos.
- Nao possui acesso ao gerenciamento de usuarios.

## Endpoints principais

### Autenticacao

- POST /api/auth/login

### Sessoes

- GET /api/sessoes
- GET /api/sessoes/{id}
- POST /api/sessoes
- PUT /api/sessoes/{id}
- PATCH /api/sessoes/{id}/cancelar
- PATCH /api/sessoes/{id}/reativar
- POST /api/sessoes/{id}/documentos

### Usuarios

- GET /api/usuarios
- POST /api/usuarios
- PUT /api/usuarios/{id}
