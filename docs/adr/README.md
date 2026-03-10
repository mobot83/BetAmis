# Architecture Decision Records (ADR)

Ce répertoire contient les décisions d'architecture du projet BetAmis.

Chaque ADR documente une décision technique significative : le contexte, les options
envisagées, la décision retenue et ses conséquences.

## Format

Les ADRs suivent le format proposé par Michael Nygard :

- **Statut** : Proposé / Accepté / Déprécié / Supersédé
- **Contexte** : Pourquoi cette décision est nécessaire
- **Décision** : Ce qui a été choisi
- **Conséquences** : Impacts positifs et négatifs

## Index

| ADR | Titre | Statut |
|-----|-------|--------|
| [ADR-001](ADR-001-choix-quarkus-prediction-service.md) | Choix de Quarkus pour le prediction-service | Accepté |
| [ADR-002](ADR-002-architecture-hexagonale.md) | Architecture Hexagonale (Ports & Adapters) | Accepté |
| [ADR-003](ADR-003-event-driven-kafka.md) | Communication événementielle avec Kafka | Accepté |
| [ADR-004](ADR-004-avro-schema-registry.md) | Sérialisation Avro + Schema Registry | Accepté |
| [ADR-005](ADR-005-database-per-service.md) | Base de données par service | Accepté |
| [ADR-006](ADR-006-pact-contract-testing.md) | Tests de contrat avec Pact | Accepté |

## Contribuer

Pour proposer une nouvelle décision d'architecture, créer un fichier
`ADR-00N-titre-court.md` en suivant le format des ADRs existants et l'ajouter
à l'index ci-dessus.
