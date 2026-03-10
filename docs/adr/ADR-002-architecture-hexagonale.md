# ADR-002 — Architecture Hexagonale comme style architectural

| Champ       | Valeur                        |
|-------------|-------------------------------|
| **Statut**  | Accepté                       |
| **Date**    | 2025-03-10                    |
| **Auteur**  | @mobot83                      |
| **Contexte**| Phase 1 — Fondations          |

---

## Contexte

BetAmis est structuré en microservices, chacun portant un **Bounded Context** DDD distinct. Le choix du style d'architecture interne à chaque service a un impact direct sur la testabilité, l'évolutivité et la clarté des responsabilités.

L'architecture en couches classique (Controller → Service → Repository) présente des dérives fréquentes : logique métier qui fuite dans les services applicatifs, dépendances directes sur les frameworks dans le domaine, tests d'intégration obligatoires pour tester la moindre règle métier.

## Décision

Tous les services de BetAmis adoptent l'**architecture hexagonale** (Ports & Adapters, Alistair Cockburn, 2005).

## Structure imposée

```
{service}/
├── domain/
│   ├── model/        # Agrégats, Entités, Value Objects
│   ├── event/        # Domain Events (POJOs purs)
│   ├── port/
│   │   ├── in/       # Ports entrants (use cases, interfaces)
│   │   └── out/      # Ports sortants (repository, messaging)
│   └── exception/    # Exceptions métier
├── application/
│   └── usecase/      # Implémentations des ports entrants
├── infrastructure/
│   ├── persistence/  # Adapters JPA/Panache
│   ├── messaging/    # Adapters Kafka
│   └── client/       # Adapters HTTP (API externes)
└── interfaces/
    ├── rest/         # Controllers REST + DTOs
    └── scheduler/    # Tâches planifiées
```

## Règle fondamentale

> **Le package `domain/` ne contient aucune dépendance vers un framework.**

Concrètement : aucun import `javax.*`, `jakarta.*`, `org.springframework.*`, `io.quarkus.*` dans `domain/`. Les seules dépendances autorisées sont Java SE et des librairies utilitaires sans contexte d'exécution (ex: `java.util`, `java.time`).

Cette règle est **vérifiable statiquement** via ArchUnit :

```java
@ArchTest
static final ArchRule domainHasNoDependencyOnFramework =
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "io.quarkus..", "jakarta..");
```

## Justification

**Testabilité maximale du domaine**
Les règles métier (calcul des points, validation d'un pronostic, règles d'invitation) sont testées avec des tests unitaires purs, sans Spring context, sans base de données, sans Kafka. Les tests s'exécutent en millisecondes.

**Indépendance vis-à-vis de l'infrastructure**
Remplacer PostgreSQL par une autre base, ou Kafka par RabbitMQ, ne nécessite que d'écrire un nouvel adapter. Le domaine et les use cases ne changent pas.

**Lisibilité des intentions métier**
Les ports entrants (`in/`) expriment explicitement ce que le service sait faire, indépendamment du protocole d'appel (REST, message, scheduler). Un nouveau développeur comprend les capacités du service en lisant uniquement `domain/port/in/`.

**Alignement avec DDD**
L'architecture hexagonale est complémentaire au DDD tactique : les agrégats, value objects et domain events vivent naturellement dans `domain/model/` et `domain/event/`, isolés de toute préoccupation technique.

## Conséquences

- Chaque Pull Request touchant `domain/` doit inclure des tests unitaires couvrant les règles métier modifiées.
- Les DTOs REST et les entités JPA sont des objets distincts des objets du domaine. Des mappers explicites (MapStruct ou manuels) assurent la conversion.
- Les Domain Events sont des POJOs immuables (records Java ou classes avec constructeur final). Ils sont convertis en messages Avro dans l'adapter Kafka, pas dans le domaine.
- La revue de code vérifie systématiquement l'absence de dépendances framework dans `domain/`.

## Alternatives considérées

| Alternative | Raison du rejet |
|-------------|-----------------|
| Architecture en couches classique | Dérives fréquentes, logique métier difficile à tester isolément |
| CQRS/Event Sourcing complet | Complexité disproportionnée pour ce projet à ce stade |
| Clean Architecture (Uncle Bob) | Similaire dans les principes, hexagonale plus idiomatique dans l'écosystème Java/DDD |
