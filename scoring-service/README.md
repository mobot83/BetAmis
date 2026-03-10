# scoring-service

Service de calcul des points et gestion des classements.

## Responsabilité

- Calculer les points de chaque pronostic après la fin d'un match
- Maintenir le classement général et par ligue en temps réel (Redis)
- Émettre les événements `points.calculated` et `ranking.updated`

## Règles de scoring

| Résultat | Points |
|----------|--------|
| Score exact | 3 pts |
| Bon vainqueur + bonne différence de buts | 2 pts |
| Bon vainqueur uniquement | 1 pt |
| Mauvais pronostic | 0 pt |

## Stack technique

- **Framework** : [Quarkus 3.x](https://quarkus.io)
- **Architecture** : Hexagonale (Ports & Adapters) — voir [ADR-002](../docs/adr/ADR-002-architecture-hexagonale.md)
- **Base de données** : PostgreSQL (`scoring_db`)
- **Cache / Classements** : Redis
- **Migrations** : Flyway
- **Messagerie** : Apache Kafka — voir [ADR-003](../docs/adr/ADR-003-event-driven-kafka.md)
- **Tests de contrat** : Pact (consumer) — voir [ADR-006](../docs/adr/ADR-006-pact-contract-testing.md)

## Événements Kafka

| Topic | Direction | Rôle |
|-------|-----------|------|
| `match.finished` | consommé | Déclenche le calcul des points |
| `prediction.submitted` | consommé | Enregistre les pronostics à évaluer |
| `points.calculated` | produit | Notifie notification-service |
| `ranking.updated` | produit | Diffuse le classement vers le frontend (SSE) |

## Démarrage local

```bash
./mvnw spring-boot:run
```
