# schemas

Définitions des schémas de messages échangés via Apache Kafka.

## Structure

```
schemas/
└── avro/          # Schémas Apache Avro (source de vérité des événements)
```

## Avro

Les schémas Avro définissent le contrat de chaque événement du bus Kafka.
Ils sont versionnés dans ce répertoire et publiés dans le Schema Registry au
démarrage des services.

Conventions :
- **Namespace** : `com.betamis.{domain}.event`
- **Nommage** : PascalCase (ex. `PredictionSubmitted`)
- **Compatibilité** : BACKWARD par défaut (les nouveaux consommateurs lisent
  les anciens messages)

Voir [ADR-004](../docs/adr/ADR-004-avro-schema-registry.md) pour la décision
d'architecture complète.

## Génération de code

Les classes Java sont générées à partir des schémas Avro lors de la compilation
(plugin Maven/Gradle). Ne pas éditer les classes générées manuellement.
