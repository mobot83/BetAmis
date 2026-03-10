# BetAmis

Application de paris entre amis pour la Coupe du Monde FIFA.

## Présentation

BetAmis permet à des groupes d'amis de soumettre des pronostics sur les matchs de la
Coupe du Monde, de rivaliser sur un classement en temps réel et de recevoir des
notifications après chaque match. L'application repose sur une **architecture
microservices orientée événements**.

## Architecture

```
betamis/
├── prediction-service/    # Soumission et gestion des pronostics (Quarkus)
├── match-service/         # Données et résultats des matchs (Quarkus)
├── scoring-service/       # Calcul des points et classements
├── league-service/        # Gestion des ligues et groupes d'amis
├── notification-service/  # Notifications email (Quarkus)
├── frontend/              # Interface utilisateur
├── schemas/avro/          # Schémas Avro des événements Kafka
├── helm/                  # Charts Kubernetes
├── monitoring/            # Dashboards Grafana / Prometheus
├── scripts/               # Scripts utilitaires (BDD, etc.)
└── docs/adr/              # Décisions d'architecture (ADRs)
```

## Démarrage local

```bash
docker-compose up -d
```

Services démarrés :

| Service | Port |
|---------|------|
| Kafka | 9092 |
| Schema Registry | 8081 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Mailhog (SMTP dev) | 1025 / 8025 |

## Décisions techniques clés

| Sujet | Décision | ADR |
|-------|----------|-----|
| prediction-service | Quarkus 3.x | [ADR-001](docs/adr/ADR-001-choix-quarkus-prediction-service.md) |
| match / notification / league / scoring | Quarkus 3.x | [ADR-001](docs/adr/ADR-001-choix-quarkus-prediction-service.md) |
| Architecture | Hexagonale (Ports & Adapters) | [ADR-002](docs/adr/ADR-002-architecture-hexagonale.md) |
| Communication inter-services | Apache Kafka (événements uniquement) | [ADR-003](docs/adr/ADR-003-event-driven-kafka.md) |
| Sérialisation | Apache Avro + Schema Registry | [ADR-004](docs/adr/ADR-004-avro-schema-registry.md) |
| Base de données | PostgreSQL (une base par service) | [ADR-005](docs/adr/ADR-005-database-per-service.md) |
| Tests de contrat | Pact (Consumer-Driven) | [ADR-006](docs/adr/ADR-006-pact-contract-testing.md) |

## Documentation

- [ADRs](docs/adr/) — Toutes les décisions d'architecture
- [Roadmap](docs/Roadmap.docx)
