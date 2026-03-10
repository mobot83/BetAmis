# ADR-006 — Tests de contrats Pact entre prediction-service et scoring-service

| Champ       | Valeur                        |
|-------------|-------------------------------|
| **Statut**  | Accepté                       |
| **Date**    | 2025-03-10                    |
| **Auteur**  | @mobot83                      |
| **Contexte**| Phase 1 — Fondations (Sprint 2)|

---

## Contexte

Le `scoring-service` consomme les événements `PredictionSubmitted` produits par le `prediction-service` sur Kafka pour calculer les points des utilisateurs. Si le format de cet événement change sans que le `scoring-service` en soit informé, le calcul des points échoue silencieusement ou génère des erreurs en production.

Dans une architecture microservices, les tests d'intégration end-to-end (démarrer tous les services + Kafka + bases de données) sont coûteux, lents et fragiles. Il faut un mécanisme plus léger pour garantir la compatibilité des contrats entre services.

## Décision

Nous utilisons **Pact** (Consumer-Driven Contract Testing) pour valider le contrat entre `prediction-service` (provider) et `scoring-service` (consumer) sur le topic Kafka `prediction.submitted`.

## Fonctionnement

### 1. Le consumer définit le contrat

Le `scoring-service` écrit un test Pact qui décrit le format minimal dont il a besoin :

```java
// Dans scoring-service (consumer)
@PactTestFor(providerName = "prediction-service", providerType = ProviderType.ASYNCH)
class PredictionSubmittedConsumerPactTest {

    @Pact(consumer = "scoring-service")
    MessagePact predictionSubmittedPact(MessagePactBuilder builder) {
        return builder
            .expectsToReceive("un PredictionSubmitted valide")
            .withContent(new PactDslJsonBody()
                .stringType("predictionId")
                .stringType("userId")
                .stringType("matchId")
                .stringType("leagueId")
                .integerType("homeScore")
                .integerType("awayScore")
                .datetime("submittedAt", "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            )
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "predictionSubmittedPact")
    void shouldProcessPredictionSubmitted(List<Message> messages) {
        // Vérifier que le scoring-service peut désérialiser et traiter le message
        PredictionSubmitted event = deserialize(messages.get(0));
        assertThat(scoringService.process(event)).isNotNull();
    }
}
```

Ce test génère un fichier de contrat JSON : `pacts/scoring-service-prediction-service.json`.

### 2. Le provider vérifie le contrat

Le `prediction-service` lit le fichier de contrat et vérifie qu'il peut produire un message conforme :

```java
// Dans prediction-service (provider)
@Provider("prediction-service")
@PactFolder("pacts")
class PredictionSubmittedProviderPactTest {

    @TestTarget
    AsyncTarget target = new AsyncTarget();

    @State("un PredictionSubmitted valide")
    void predictionSubmittedState() {
        // Préparer le message de test
        target.setMessage(buildSamplePredictionSubmitted());
    }
}
```

### 3. Versionnement du fichier pact

Le fichier `pacts/scoring-service-prediction-service.json` est commité dans le repository du `scoring-service` et référencé par le `prediction-service` via le path relatif (ou un Pact Broker si le projet évolue).

## Justification

**Feedback rapide**
Les tests Pact s'exécutent en quelques secondes sans infrastructure Kafka. Ils détectent les incompatibilités de contrat dès la phase de build, pas lors des tests d'intégration.

**Consumer-driven**
C'est le consumer (`scoring-service`) qui définit ce dont il a besoin, pas le provider qui impose son format. Cela aligne le contrat sur l'usage réel.

**Complémentarité avec Avro**
Avro + Schema Registry garantit la compatibilité structurelle des messages (le champ existe, il a le bon type). Pact garantit la compatibilité sémantique (le consumer peut réellement traiter le message et en extraire ce dont il a besoin).

**Documentation vivante**
Le fichier pact est une documentation exécutable du contrat inter-services, toujours à jour car validée à chaque build.

## Conséquences

- Le fichier `pacts/scoring-service-prediction-service.json` est versionné dans le repo du `scoring-service`.
- Le `prediction-service` référence ce fichier dans ses tests provider (path relatif via git submodule ou copie dans `src/test/resources/pacts/`).
- La CI du `prediction-service` inclut l'exécution des tests provider Pact. Un build échoue si le contrat n'est plus respecté.
- Le périmètre initial couvre uniquement le topic `prediction.submitted`. Les autres topics seront couverts au fur et à mesure (backlog).
- Si le projet évolue vers une équipe multi-développeurs, l'installation d'un **Pact Broker** (version open source auto-hébergée) sera envisagée pour centraliser la gestion des contrats.

## Périmètre des contrats à couvrir

| Consumer             | Provider             | Topic / Interface         | Priorité |
|----------------------|----------------------|---------------------------|----------|
| scoring-service      | prediction-service   | `prediction.submitted`    | Sprint 2 |
| scoring-service      | match-service        | `match.finished`          | Sprint 2 |
| notification-service | scoring-service      | `points.calculated`       | Sprint 6 |

## Alternatives considérées

| Alternative | Raison du rejet |
|-------------|-----------------|
| Tests d'intégration end-to-end uniquement | Lents, coûteux, fragiles, feedback tardif |
| Tests de schéma Avro uniquement | Valide la structure mais pas le comportement du consumer |
| Revue de code manuelle des contrats | Non automatisé, ne détecte pas les régressions |
| Spring Cloud Contract | Moins adapté aux messages Kafka asynchrones, plus orienté REST |
