# match-service

Service de gestion des matchs de la Coupe du Monde.

## Responsabilité

- Référencer les matchs (équipes, date, stade, phase de compétition)
- Déclencher les événements de début et fin de match
- Enregistrer les scores officiels

## Stack technique

- **Framework** : [Quarkus 3.x](https://quarkus.io)
- **Architecture** : Hexagonale (Ports & Adapters) — voir [ADR-002](../docs/adr/ADR-002-architecture-hexagonale.md)
- **Base de données** : PostgreSQL (`match_db`)
- **Migrations** : Flyway
- **Messagerie** : Apache Kafka — voir [ADR-003](../docs/adr/ADR-003-event-driven-kafka.md)

## Structure cible

```
match-service/
├── src/main/java/com/betamis/match/
│   ├── domain/
│   │   ├── model/          # Match, Team, Score
│   │   ├── event/          # MatchStarted, MatchFinished
│   │   ├── port/
│   │   │   ├── in/         # StartMatchUseCase, RecordResultUseCase
│   │   │   └── out/        # MatchRepository, EventPublisher
│   │   └── exception/
│   ├── application/
│   │   └── usecase/
│   ├── infrastructure/
│   │   ├── persistence/    # JPA repositories
│   │   └── messaging/      # KafkaMatchEventPublisher
│   └── interfaces/
│       └── rest/           # MatchController
├── src/main/resources/
│   └── db/migration/
└── src/test/
```

## Événements Kafka produits

| Topic | Consommateur(s) |
|-------|-----------------|
| `match.started` | prediction-service (fermeture pronostics) |
| `match.finished` | scoring-service (calcul des points) |

## Démarrage local

```bash
./mvnw quarkus:dev
```
