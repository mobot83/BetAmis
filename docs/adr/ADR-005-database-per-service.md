# ADR-005 — PostgreSQL dédié par service (Database per Service pattern)

| Champ       | Valeur                        |
|-------------|-------------------------------|
| **Statut**  | Accepté                       |
| **Date**    | 2025-03-10                    |
| **Auteur**  | @mobot83                      |
| **Contexte**| Phase 1 — Fondations          |

---

## Contexte

Dans une architecture microservices, la gestion de la persistance est un choix structurant. La tentation naturelle est de partager une base de données entre plusieurs services pour simplifier les jointures et éviter la duplication de données. Cependant, cette approche crée un couplage fort au niveau des données qui contredit les principes d'isolation des microservices.

BetAmis compte 4 services avec des besoins de persistance : `prediction-service`, `match-service`, `scoring-service` et `league-service`.

## Décision

Chaque service possède sa **propre base de données PostgreSQL**, inaccessible aux autres services. Aucun service n'accède directement à la base de données d'un autre service.

## Schéma de déploiement

```
prediction-service  →  predictions_db (PostgreSQL)
match-service       →  matches_db     (PostgreSQL)
scoring-service     →  scores_db      (PostgreSQL)
league-service      →  leagues_db     (PostgreSQL)

Tous les services     →  Redis               (classements temps réel, cache)
```

En local (docker-compose) : une seule instance PostgreSQL avec 4 bases de données distinctes.
En production (Kubernetes) : un StatefulSet PostgreSQL par service (ou PostgreSQL managé cloud avec une base par service).

## Justification

### Isolation des Bounded Contexts

Chaque base de données correspond exactement à un Bounded Context DDD. Le schéma de données d'un service est un détail d'implémentation interne : il peut évoluer, être migré ou refactorisé sans impacter les autres services.

### Déploiement indépendant

Sans base partagée, chaque service peut être déployé, mis à l'échelle ou remplacé indépendamment. Les migrations de schéma (Flyway/Liquibase) d'un service n'impactent pas les autres.

### Scalabilité différenciée

Le `prediction-service` subira des pics de charge intenses avant chaque match. Son instance PostgreSQL peut être dimensionnée indépendamment des autres services qui ont des profils de charge différents.

### Liberté technologique

Bien que tous les services utilisent PostgreSQL aujourd'hui, cette décision laisse la porte ouverte à choisir le bon outil par service si le besoin évolue (ex: base time-series pour les métriques, document store pour les notifications).

## Conséquences

### Ce qui change par rapport à une base partagée

- **Pas de jointures cross-service** : si le frontend a besoin d'afficher "le pronostic de l'utilisateur + le résultat du match", cette agrégation se fait côté application (deux appels API) ou dans une vue matérialisée maintenue par événements.
- **Cohérence éventuelle** : la donnée est cohérente au sein d'un service (ACID), mais cohérente à terme entre services (via Kafka). C'est un compromis assumé.
- **Duplication partielle acceptée** : le `scoring-service` stocke une copie dénormalisée des informations de match nécessaires au calcul des points. Cette copie est maintenue à jour via les événements Kafka.

### Migrations de schéma

- Chaque service gère ses migrations avec **Flyway**, versionnées dans `src/main/resources/db/migration/`.
- Les migrations sont appliquées au démarrage du service (mode `validate` en prod, `migrate` en dev).

### Tests

- Les tests d'intégration utilisent **Testcontainers** : chaque service démarre sa propre instance PostgreSQL éphémère pendant les tests, garantissant l'isolation complète.

## Alternatives considérées

| Alternative | Raison du rejet |
|-------------|-----------------|
| Base de données partagée | Couplage fort, migrations risquées, impossible de déployer indépendamment |
| Schema partagé par namespace | Isolation insuffisante, toujours couplé au niveau des credentials et des migrations |
| Base de données par environnement uniquement | Ne résout pas le couplage entre services en production |
