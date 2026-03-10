# ADR-004 — Avro + Schema Registry pour la sérialisation des Domain Events

| Champ       | Valeur                        |
|-------------|-------------------------------|
| **Statut**  | Accepté                       |
| **Date**    | 2025-03-10                    |
| **Auteur**  | @mobot83                      |
| **Contexte**| Phase 1 — Fondations          |

---

## Contexte

Les Domain Events transitant sur Kafka doivent être sérialisés dans un format que producteurs et consommateurs comprennent. Plusieurs options existent : JSON brut, JSON avec validation de schema (JSON Schema), Protocol Buffers, ou Avro.

Le choix du format de sérialisation impacte directement la capacité à faire **évoluer les schemas** sans casser les consommateurs existants, un enjeu critique dans une architecture microservices où les services sont déployés indépendamment.

## Décision

Les messages Kafka utilisent **Apache Avro** comme format de sérialisation, couplé à **Confluent Schema Registry** pour la gestion et la validation des schemas.

## Format d'un schema Avro (exemple : `PredictionSubmitted`)

```json
{
  "namespace": "com.betamis.prediction.event",
  "type": "record",
  "name": "PredictionSubmitted",
  "doc": "Emis quand un utilisateur soumet un pronostic pour un match",
  "fields": [
    { "name": "predictionId", "type": "string", "doc": "UUID de la prediction" },
    { "name": "userId",       "type": "string" },
    { "name": "leagueId",     "type": "string" },
    { "name": "matchId",      "type": "string" },
    { "name": "homeScore",    "type": "int" },
    { "name": "awayScore",    "type": "int" },
    { "name": "submittedAt",  "type": "long", "logicalType": "timestamp-millis" },
    { "name": "version",      "type": "string", "default": "1.0" }
  ]
}
```

Les schemas sont versionnés dans `schemas/avro/` à la racine du monorepo.

## Justification

### Compatibilité évolutive des schemas

Avro + Schema Registry enforces des règles de compatibilité configurables :

- **BACKWARD** (défaut) : un nouveau schema peut lire les messages produits par l'ancien schema. Cela permet de déployer les consommateurs avant les producteurs.
- **FORWARD** : l'ancien schema peut lire les messages du nouveau. Permet le déploiement inverse.
- **FULL** : les deux. C'est le mode cible pour BetAmis une fois le système stabilisé.

En pratique : ajouter un champ optionnel avec une valeur par défaut est toujours rétrocompatible. Supprimer ou renommer un champ est une **breaking change** qui nécessite une migration explicite.

### Validation à la sérialisation

Le Schema Registry valide chaque message au moment de la production. Un producteur qui tente d'envoyer un message non conforme au schema reçoit une erreur immédiatement, avant que le message n'atteigne Kafka. Cela détecte les bugs de contrat au plus tôt.

### Efficacité

Avro est un format binaire compact. Comparé à JSON, la taille des messages est réduite de 30 à 70% selon la structure. Les messages Kafka contiennent uniquement l'ID du schema (4 octets) + le payload binaire, pas la définition complète.

### Source de vérité partagée

Le Schema Registry est la source de vérité pour tous les contrats d'événements. N'importe quel service peut consulter le schema d'un topic sans lire le code source d'un autre service.

## Conventions

- **Un schema par Domain Event**, nommé `{NomEvenement}` en PascalCase.
- **Namespace** : `com.betamis.{domaine}.event`
- **Tout nouveau champ** doit avoir une valeur `default` pour garantir la compatibilité backward.
- **Champs supprimés** : déprécier avec `"doc": "DEPRECATED - sera supprimé en v2"` pendant au moins un sprint avant suppression effective.
- Les schemas sont soumis à revue de code comme tout autre code.

## Conséquences

- Le Schema Registry (Confluent Community Edition) est inclus dans le `docker-compose.yml` local et dans le Helm chart `betamis-infra`.
- Les services Java utilisent `kafka-avro-serializer` de Confluent pour la sérialisation/désérialisation automatique.
- La CI vérifie la compatibilité des schemas modifiés avant merge (`schema-registry-maven-plugin`).
- Les classes Java correspondant aux schemas sont **générées** à la compilation via le plugin Maven Avro, pas écrites à la main.
- Les Domain Events du domaine (`domain/event/`) restent des POJOs Java purs. La conversion vers/depuis les classes Avro générées s'effectue dans l'adapter Kafka (`infrastructure/messaging/`).

## Alternatives considérées

| Alternative | Raison du rejet |
|-------------|-----------------|
| JSON brut | Pas de validation de schema, pas de compatibilité évolutive garantie, verbeux |
| JSON Schema | Validation possible mais pas de gestion de compatibilité native, pas de génération de code |
| Protocol Buffers | Excellent choix technique, mais outillage Kafka moins mature qu'Avro, moins standard dans l'écosystème Confluent |
| Sans Schema Registry (Avro inline) | Perd la validation centralisée et la découvrabilité des schemas |
