# prediction-service

Service de gestion des pronostics de matchs.

## Responsabilité

- Accepter et valider la soumission d'un pronostic (score prédit) par un utilisateur
- Appliquer les règles métier (délai avant le coup d'envoi, unicité par utilisateur/match)
- Fermer automatiquement les pronostics à l'arrivée de l'événement `match.started`
- Émettre `prediction.submitted` à destination du scoring-service

## Stack technique

- **Framework** : [Quarkus 3.x](https://quarkus.io) — voir [ADR-001](../docs/adr/ADR-001-choix-quarkus-prediction-service.md)
- **Architecture** : Hexagonale (Ports & Adapters) — voir [ADR-002](../docs/adr/ADR-002-architecture-hexagonale.md)
- **Base de données** : PostgreSQL (`prediction_db`) via Hibernate Panache
- **Migrations** : Flyway
- **Messagerie** : Apache Kafka — voir [ADR-003](../docs/adr/ADR-003-event-driven-kafka.md)
- **Tests de contrat** : Pact (provider) — voir [ADR-006](../docs/adr/ADR-006-pact-contract-testing.md)

## Structure cible

```
prediction-service/
├── src/main/java/com/betamis/prediction/
│   ├── domain/
│   │   ├── model/          # Prediction, Match (Value Objects)
│   │   ├── event/          # PredictionSubmitted, PredictionClosed
│   │   ├── port/
│   │   │   ├── in/         # SubmitPredictionUseCase, ClosePredictionsUseCase
│   │   │   └── out/        # PredictionRepository, EventPublisher
│   │   └── exception/      # PredictionAlreadyExistsException, etc.
│   ├── application/
│   │   └── usecase/        # SubmitPredictionService, ClosePredictionsService
│   ├── infrastructure/
│   │   ├── persistence/    # PanachePredictionRepository
│   │   └── messaging/      # KafkaPredictionEventPublisher + MatchEventConsumer
│   └── interfaces/
│       └── rest/           # PredictionResource (JAX-RS)
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/       # Scripts Flyway (V1__init.sql, …)
└── src/test/
    ├── java/               # Tests unitaires (domaine) et Testcontainers (infra)
    └── resources/
        └── pacts/          # Contrats Pact générés
```

## Événements Kafka

| Topic | Direction | Consommateur / Producteur |
|-------|-----------|---------------------------|
| `match.started` | consommé | match-service → fermeture des pronostics |
| `prediction.submitted` | produit | → scoring-service |
| `prediction.closed` | produit | confirmation de fermeture |

## Démarrage local

```bash
./mvnw quarkus:dev
```

Application disponible sur `http://localhost:8080` avec hot reload activé.
