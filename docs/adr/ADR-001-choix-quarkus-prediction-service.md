# ADR-001 — Choix de Quarkus pour le prediction-service

| Champ       | Valeur                        |
|-------------|-------------------------------|
| **Statut**  | Accepté                       |
| **Date**    | 2025-03-10                    |
| **Auteur**  | @mobot83                      |
| **Contexte**| Phase 1 — Fondations          |

---

## Contexte

Le `prediction-service` est le service central de BetAmis : il reçoit et valide tous les pronostics des utilisateurs. Ce service sera soumis à des **pics de charge importants** dans les minutes précédant chaque coup d'envoi (potentiellement des centaines de soumissions simultanées lors d'un match de Coupe du Monde).

Deux frameworks Java étaient en lice : **Quarkus** et **Spring Boot**.

## Décision

Nous utilisons **Quarkus** pour le `prediction-service`.

## Justification

### Arguments en faveur de Quarkus

**Performances au démarrage et empreinte mémoire**
Quarkus déplace une grande partie du travail de bootstrap à la compilation (build-time processing). Le résultat est un temps de démarrage de l'ordre de **50–200ms** contre 2–5s pour Spring Boot, et une consommation mémoire divisée par 2 à 3 en mode JVM. Dans un contexte Kubernetes avec HorizontalPodAutoscaler, cela signifie que les nouveaux pods sont opérationnels quasi immédiatement lors d'un scale-up.

**Mode natif GraalVM (option future)**
Quarkus est conçu pour la compilation native GraalVM. Si les contraintes de coût cloud l'exigent, nous pouvons générer un binaire natif sans modifier le code applicatif. Cette option reste ouverte sans dette technique.

**Quarkus Panache**
Panache simplifie la couche repository avec un pattern Active Record ou Repository idiomatique, tout en restant compatible JPA/Hibernate. Cela réduit le boilerplate sans sacrifier la testabilité.

**Cohérence avec l'écosystème cloud-native**
Quarkus est orienté conteneurs dès la conception : health checks (`/q/health`), métriques Prometheus (`/q/metrics`) et OpenTelemetry sont disponibles sans configuration supplémentaire.

### Compromis acceptés

- **Écosystème moins mature** : certaines librairies tierces n'ont pas d'extension Quarkus officielle. Dans ce cas, les librairies Java standard restent utilisables mais sans optimisation build-time.
- **Courbe d'apprentissage** : les spécificités Quarkus (injection CDI, build-time processing) diffèrent de Spring. Un temps d'adaptation est à prévoir.
- **Outillage IDE** : le support IntelliJ/VS Code est bon mais légèrement inférieur à Spring Boot DevTools.

### Pourquoi pas Spring Boot sur ce service

Spring Boot reste le choix sur `match-service` (service moins critique en termes de latence de démarrage, avec un écosystème d'intégration plus riche). Utiliser les deux frameworks dans le projet est également une décision délibérée pour la valeur de vitrine du projet (démontrer la maîtrise des deux).

## Conséquences

- Le `prediction-service` utilise Quarkus 3.x avec l'extension `quarkus-hibernate-orm-panache`, `quarkus-kafka-streams` et `quarkus-opentelemetry`.
- Le `match-service` et le `notification-service` utilisent Spring Boot 3.x.
- Les images Docker Quarkus utilisent le base image `ubi8/openjdk-21-runtime` (JVM mode) en première intention. La compilation native est envisagée en Phase 4 si les coûts d'infrastructure le justifient.
- Les health checks Kubernetes pointent sur `/q/health/live` et `/q/health/ready`.

## Alternatives considérées

| Alternative | Raison du rejet |
|-------------|-----------------|
| Spring Boot pour tous les services | Uniformité au détriment de la démonstration de compétences et des performances au démarrage |
| Micronaut | Moins de traction communautaire, écosystème plus restreint |
| Vert.x pur | Trop bas niveau, complexité accrue sans bénéfice décisif sur ce cas d'usage |
