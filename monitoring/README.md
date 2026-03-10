# monitoring

Dashboards et configuration de monitoring pour BetAmis.

## Cible

- Dashboards **Grafana** pour les métriques applicatives et infrastructure
- Alertes sur les seuils critiques (latence, taux d'erreur, lag Kafka)

## Structure cible

```
monitoring/
└── dashboards/
    ├── betamis-overview.json       # Vue d'ensemble de tous les services
    ├── kafka-topics.json           # Lag des consumers, débit des topics
    ├── prediction-service.json     # Métriques Quarkus (JVM, HTTP, DB)
    └── scoring-service.json        # Temps de calcul, Redis
```

## Endpoints de métriques

Tous les services exposent leurs métriques au format Prometheus via l'endpoint Quarkus :

| Framework | Health | Métriques |
|-----------|--------|-----------|
| Quarkus | `/q/health` | `/q/metrics` |

## Stack

- **Prometheus** — collecte des métriques
- **Grafana** — dashboards et alertes
- **Alertmanager** — routage des alertes (Slack, email)
