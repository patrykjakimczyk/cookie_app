# Cookie - managing household pantry, shopping lists and planning meals

It's a web app used for managing pantry and supporting shopping and food preparation in your household. The main functionalities are: keeping track of the items you own using a pantry, creating shopping lists, managing a group of household members, viewing ready-made recipes and planning meals using a calendar. The web app is built using Angular that communicates with a monolithic REST API, which is built using Spring Boot. The API connects to the PostgreSQL database to store the data.

## Used technologies, frameworks and libraries

- Java 17
- Spring Boot 3.1.4
- JUnit 5
- Mockito
- Maven
- OpenAPI
- Angular 16.2.8
- Typescript
- SCSS
- Angular Materials
- FullCalendar
- Docker
- PostgreSQL 16.2

## Requirements

- Docker with docker compose

## How to run?

Clone the repository in a desired directory

```bash
git clone https://github.com/patrykjakimczyk/cookie_app
```

Go to a **project directory** and create `.env` file with this content

```
DB_PASSWORD=<your_db_password>
DB_NAME=<your_db_name>
```

After that, navigate to **./cookie_backend/src/main/resources**, create `env` folder, and `env-docker.yaml` in it, with this content

```
DB_USERNAME: postgres
DB_PASSWORD: <your_db_password>
DB_URL: jdbc:postgresql://postgres:5432/<your_db_name>
JWT_SECRET: <your_secret>
```

The next step is to navigate back to the root folder of the application and run

```bash
docker compose build
```

Then, start the application with

```bash
docker compose up
```

To see the application, go to **localhost**:80

## Presentation

The main view for unauthenticated user is:
![landing_page](https://github.com/patrykjakimczyk/cookie_app/blob/master/images/landing_page.png?raw=true)
If you want to log in, firstly you have to register your account:
![register](https://github.com/patrykjakimczyk/cookie_app/blob/master/images/registration.png?raw=true)
Then, you can log in. First thing you will see is the calendar page:
![register](https://github.com/patrykjakimczyk/cookie_app/blob/master/images/calendar.png?raw=true)
Another interesting view is recipe search, which look like this:
![register](https://github.com/patrykjakimczyk/cookie_app/blob/master/images/calendar.png?raw=true)
