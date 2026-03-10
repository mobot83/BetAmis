# league-service

Service de gestion des ligues et groupes d'amis.

## Responsabilité

- Créer et administrer des ligues privées entre amis
- Gérer les membres (invitation, acceptation, exclusion)
- Exposer le classement par ligue (alimenté via les événements du scoring-service)

## Stack technique

- **Framework** : [Quarkus 3.x](https://quarkus.io)
- **Architecture** : Hexagonale (Ports & Adapters) — voir [ADR-002](../docs/adr/ADR-002-architecture-hexagonale.md)
- **Base de données** : PostgreSQL (`league_db`)
- **Migrations** : Flyway
- **Messagerie** : Apache Kafka — voir [ADR-003](../docs/adr/ADR-003-event-driven-kafka.md)

## Structure cible

```
league-service/
├── src/main/java/com/betamis/league/
│   ├── domain/
│   │   ├── model/          # League, Member, Invitation
│   │   ├── port/
│   │   │   ├── in/         # CreateLeagueUseCase, InviteMemberUseCase
│   │   │   └── out/        # LeagueRepository
│   │   └── exception/      # LeagueNotFoundException, MemberAlreadyExistsException
│   ├── application/
│   │   └── usecase/
│   ├── infrastructure/
│   │   ├── persistence/
│   │   └── messaging/      # Consomme ranking.updated pour les classements par ligue
│   └── interfaces/
│       └── rest/           # LeagueController
├── src/main/resources/
│   └── db/migration/
└── src/test/
```

## Événements Kafka consommés

| Topic | Action |
|-------|--------|
| `ranking.updated` | Met à jour le classement de chaque ligue |

## Démarrage local

```bash
./mvnw quarkus:dev
```
