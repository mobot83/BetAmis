# helm

Charts Helm pour le déploiement de BetAmis sur Kubernetes.

## Structure cible

```
helm/
├── betamis/                  # Chart parent (umbrella chart)
│   ├── Chart.yaml
│   ├── values.yaml
│   ├── values-staging.yaml
│   └── charts/               # Sous-charts (dépendances)
├── prediction-service/
├── match-service/
├── scoring-service/
├── league-service/
└── notification-service/
```

## Déploiement

```bash
# Staging
helm upgrade --install betamis ./helm/betamis \
  --namespace betamis \
  --values ./helm/betamis/values-staging.yaml
```

## Prérequis

- Kubernetes 1.28+
- Helm 3.x
- PostgreSQL et Kafka déployés (Bitnami charts ou cloud-managed)
