# ADR-003 — Event-Driven avec Kafka comme bus d'intégration inter-services

| Champ       | Valeur                        |
|-------------|-------------------------------|
| **Statut**  | Accepté                       |
| **Date**    | 2025-03-10                    |
| **Auteur**  | @mobot83                      |
| **Contexte**| Phase 1 — Fondations          |

---

## Contexte

Les services de BetAmis doivent communiquer entre eux pour orchestrer les flux métier principaux :

1. Quand un match se termine → le `scoring-service` doit calculer les points
2. Quand les points sont calculés → le `notification-service` doit alerter les utilisateurs
3. Quand le classement est mis à jour → le frontend doit se rafraîchir

Deux approches d'intégration étaient envisageables : **appels REST synchrones** entre services, ou **messagerie asynchrone** via un bus d'événements.

## Décision

Les intégrations inter-services utilisent **Apache Kafka** comme bus d'événements. Les services communiquent exclusivement via des **Domain Events** publiés sur des topics Kafka. Aucun appel REST synchrone n'est effectué entre services backend.

## Topologie des topics

| Topic                  | Producteur           | Consommateur(s)                        |
|------------------------|----------------------|----------------------------------------|
| `match.started`        | match-service        | prediction-service (ferme les pronos)  |
| `match.finished`       | match-service        | scoring-service                        |
| `prediction.submitted` | prediction-service   | scoring-service                        |
| `prediction.closed`    | prediction-service   | —                                      |
| `points.calculated`    | scoring-service      | notification-service                   |
| `ranking.updated`      | scoring-service      | frontend via SSE                       |
| `notification.sent`    | notification-service | —                                      |

**Convention de nommage** : `{domaine}.{verbe-passe}` en snake_case. Les événements sont nommés au passé car ils décrivent un fait accompli.

## Justification

### Découplage temporel

Avec des appels REST synchrones, si le `scoring-service` est indisponible au moment où un match se termine, l'événement est perdu ou nécessite un mécanisme de retry complexe côté appelant. Avec Kafka, le `match-service` publie l'événement et le `scoring-service` le consomme quand il est disponible. La durée de rétention des messages (par défaut 7 jours) assure qu'aucun événement n'est perdu.

### Découplage structurel

Le `match-service` n'a pas besoin de connaître l'existence du `scoring-service`. L'ajout d'un nouveau consommateur (ex: un service de statistiques) ne nécessite aucune modification du producteur.

### Garanties de livraison

Kafka garantit la livraison **at-least-once** par défaut. Les consommateurs doivent donc être **idempotents** : traiter deux fois le même événement produit le même résultat. Cette contrainte est documentée et testée pour chaque consumer.

### Scalabilité

Les topics Kafka sont partitionnés. Le `scoring-service` peut être scalé horizontalement en augmentant le nombre de partitions et de consumers dans le même consumer group, sans modification du code.

## Conséquences

- Chaque service possède sa propre connexion Kafka configurée dans son adapter `infrastructure/messaging/`.
- Les consommateurs implémentent l'idempotence via une table de déduplication ou en vérifiant l'état courant avant d'appliquer une transformation.
- Les schemas des événements sont versionnés avec Avro + Schema Registry (voir ADR-004).
- La cohérence entre services est **éventuelle** : il existe une fenêtre de temps où le classement n'est pas encore mis à jour après la fin d'un match. Ce comportement est acceptable pour le cas d'usage (paris entre amis).
- Les tests d'intégration utilisent **Testcontainers** avec une instance Kafka réelle (pas de mock).
- Un Dead Letter Topic (`{topic}.dlq`) est configuré pour chaque consumer afin de capturer les messages en erreur sans bloquer le flux principal.

## Alternatives considérées

| Alternative | Raison du rejet |
|-------------|-----------------|
| REST synchrone entre services | Couplage temporel fort, cascade de pannes, retry à implémenter manuellement |
| RabbitMQ | Moins adapté aux flux haute volumétrie, pas de rétention native des messages, écosystème Java moins intégré |
| Redis Pub/Sub | Pas de persistance des messages, at-most-once delivery insuffisant pour les calculs de points |
| Outbox Pattern + polling | Complexité accrue, latence plus élevée, Kafka répond déjà au besoin |
