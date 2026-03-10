# ADR-001 — Choix de Quarkus pour tous les services

| Champ       | Valeur                        |
|-------------|-------------------------------|
| **Statut**  | Accepté                       |
| **Date**    | 2025-03-10                    |
| **Auteur**  | @mobot83                      |
| **Contexte**| Phase 1 — Fondations          |

---

## Contexte

BetAmis repose sur plusieurs microservices Java. Chacun sera déployé dans un environnement Kubernetes et soumis à des pics de charge (notamment autour des coups d'envoi). Un choix de framework uniforme simplifie la maintenance, la cohérence de l'outillage et le onboarding des développeurs.

Deux frameworks Java étaient en lice : **Quarkus** et **Spring Boot**.

## Décision

Nous utilisons **Quarkus 3.x** pour **l'ensemble des services** BetAmis.

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

**Uniformité de la stack**
Un seul framework pour tous les services réduit la charge cognitive, facilite le partage de code commun (bibliothèques internes) et simplifie la CI/CD.

### Compromis acceptés

- **Écosystème moins mature** : certaines librairies tierces n'ont pas d'extension Quarkus officielle. Dans ce cas, les librairies Java standard restent utilisables mais sans optimisation build-time.
- **Courbe d'apprentissage** : les spécificités Quarkus (injection CDI, build-time processing) diffèrent de Spring. Un temps d'adaptation est à prévoir.
- **Outillage IDE** : le support IntelliJ/VS Code est bon mais légèrement inférieur à Spring Boot DevTools.

## Conséquences

- Tous les services (`prediction-service`, `match-service`, `scoring-service`, `league-service`, `notification-service`) utilisent Quarkus 3.x.
- Les extensions communes : `quarkus-hibernate-orm-panache`, `quarkus-smallrye-reactive-messaging-kafka`, `quarkus-opentelemetry`.
- Les images Docker utilisent le base image `ubi8/openjdk-21-runtime` (JVM mode) en première intention. La compilation native est envisagée en Phase 4 si les coûts d'infrastructure le justifient.
- Les health checks Kubernetes pointent sur `/q/health/live` et `/q/health/ready`.
- Les métriques Prometheus sont exposées sur `/q/metrics`.

## Alternatives considérées

| Alternative | Raison du rejet |
|-------------|-----------------|
| Spring Boot pour tous les services | Performances au démarrage et empreinte mémoire moins favorables en environnement Kubernetes |
| Mix Quarkus / Spring Boot | Complexité opérationnelle accrue, incohérence de l'outillage et du monitoring |
| Micronaut | Moins de traction communautaire, écosystème plus restreint |
| Vert.x pur | Trop bas niveau, complexité accrue sans bénéfice décisif sur ce cas d'usage |
