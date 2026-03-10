# scripts

Scripts utilitaires pour le développement et l'exploitation de BetAmis.

## Contenu

### `postgres/init-databases.sql`

Script d'initialisation PostgreSQL exécuté automatiquement au démarrage du
conteneur Docker Compose. Crée les quatre bases de données des services :

| Base de données | Service |
|----------------|---------|
| `prediction_db` | prediction-service |
| `match_db` | match-service |
| `scoring_db` | scoring-service |
| `league_db` | league-service |

> Monté via le volume Docker Compose — ne pas exécuter manuellement en dev.

## Organisation

```
scripts/
├── postgres/          # Initialisation et utilitaires BDD
├── kafka/             # Création de topics, reset d'offsets (à venir)
└── ci/                # Scripts de pipeline CI/CD (à venir)
```
