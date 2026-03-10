# schemas/avro

Schémas Apache Avro des événements Kafka de BetAmis.

## Événements

| Fichier | Topic Kafka | Producteur | Consommateur(s) |
|---------|-------------|------------|-----------------|
| `PredictionSubmitted.avsc` | `prediction.submitted` | prediction-service | scoring-service |
| `PredictionClosed.avsc` | `prediction.closed` | prediction-service | — |
| `MatchStarted.avsc` | `match.started` | match-service | prediction-service |
| `MatchFinished.avsc` | `match.finished` | match-service | scoring-service |
| `PointsCalculated.avsc` | `points.calculated` | scoring-service | notification-service |
| `RankingUpdated.avsc` | `ranking.updated` | scoring-service | frontend (SSE) |
| `NotificationSent.avsc` | `notification.sent` | notification-service | — |

## Format d'un schéma

```json
{
  "namespace": "com.betamis.prediction.event",
  "type": "record",
  "name": "PredictionSubmitted",
  "fields": [
    { "name": "predictionId", "type": "string" },
    { "name": "userId",       "type": "string" },
    { "name": "matchId",      "type": "string" },
    { "name": "homeScore",    "type": "int" },
    { "name": "awayScore",    "type": "int" },
    { "name": "submittedAt",  "type": "string", "doc": "ISO-8601" }
  ]
}
```

## Schema Registry

Les schémas sont enregistrés automatiquement par les services producteurs au
démarrage. En local, le Schema Registry est accessible sur
`http://localhost:8081`.
