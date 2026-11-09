# Arcade Leaderboard Service

A small Java web service for players in an arcade competition. Each player has an ID, nickname, and total score. New players start with zero points. Data is stored in memory and resets when the service stops.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer

## Run

```bash
mvn test
mvn exec:java
```

The service listens on port `7070`. Set `PORT` to use another port.

If you work from the course's `2026:2027` folder on macOS, copy or clone this repository to a path without `:` before running Maven. Java uses `:` as a classpath separator.

## HTTP API

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/health` | Returns `{"status":"ok"}`. |
| `GET` | `/api/players` | Lists all players. |
| `GET` | `/api/players/{id}` | Returns one player, or `404` if it does not exist. |
| `POST` | `/api/players` | Creates a player from `{"nickname":"Ada"}`; returns `201` and a `Location` header. Nicknames must be unique (ignoring case); a blank or duplicate nickname returns `400`. |
| `POST` | `/api/players/{id}/scores` | Adds points from `{"points":10}` to the player's total and returns the updated player (`200`). Unknown player: `404`; zero, negative or invalid points: `400`. |
| `GET` | `/api/leaderboard` | Lists players ordered by total score (highest first), ties broken by nickname alphabetically. Optional positive `limit` parameter; an invalid `limit` returns `400`. |

Example:

```bash
curl -i -X POST http://localhost:7070/api/players \
  -H 'Content-Type: application/json' \
  -d '{"nickname":"Ada"}'
```

Recording points and consulting the leaderboard:

```bash
curl -i -X POST http://localhost:7070/api/players/1/scores \
  -H 'Content-Type: application/json' \
  -d '{"points":10}'

curl -i 'http://localhost:7070/api/leaderboard?limit=3'
```

## Versions

- `v1.0.0`: initial service.
- `v1.0.1`: fix, duplicate nicknames are now rejected.
- `v1.1.0`: record points and leaderboard.

## Architecture

The project uses a small hexagonal architecture:

- `architecture/model/` contains `Player`, independently of HTTP and storage.
- `architecture/domain/` contains `PlayerFactory`, which applies the rules for creating a player.
- `architecture/ports/` contains `PlayerRepository` and its `PlayerRepositoryImpl` implementation.
- `architecture/usecases/` contains the `PlayerUseCase` interface and its `PlayerUseCaseImpl` implementation.
- `infrastructure/web/` contains the Javalin HTTP adapter and request data.
- `infrastructure/App.java` receives a use case and holds the Javalin application.
- `Main.java`, outside those packages, creates the repository and use case, then instantiates and starts `App`.

The use case depends on the interfaces in `architecture/ports/`, while Javalin stays in `infrastructure/`.
