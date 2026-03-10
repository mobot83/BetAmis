# notification-service

Service d'envoi de notifications aux utilisateurs.

## Responsabilité

- Envoyer des emails de confirmation (pronostic soumis, résultat disponible)
- Notifier les utilisateurs de leur score et classement après chaque match
- Gérer les préférences de notification

## Stack technique

- **Framework** : [Quarkus 3.x](https://quarkus.io)
- **Architecture** : Hexagonale (Ports & Adapters) — voir [ADR-002](../docs/adr/ADR-002-architecture-hexagonale.md)
- **Email (dev)** : Mailhog — SMTP port 1025, interface web `http://localhost:8025`
- **Messagerie** : Apache Kafka — voir [ADR-003](../docs/adr/ADR-003-event-driven-kafka.md)

## Structure cible

```
notification-service/
├── src/main/java/com/betamis/notification/
│   ├── domain/
│   │   ├── model/          # Notification, UserPreferences
│   │   ├── event/          # NotificationSent
│   │   ├── port/
│   │   │   ├── in/         # SendNotificationUseCase
│   │   │   └── out/        # EmailSender, NotificationRepository
│   │   └── exception/
│   ├── application/
│   │   └── usecase/
│   ├── infrastructure/
│   │   ├── messaging/      # KafkaNotificationConsumer
│   │   └── email/          # SmtpEmailSender
│   └── interfaces/
├── src/main/resources/
│   └── db/migration/
└── src/test/
```

## Événements Kafka

| Topic | Direction | Rôle |
|-------|-----------|------|
| `points.calculated` | consommé | Déclenche l'envoi de l'email de résultat |
| `notification.sent` | produit | Confirmation d'envoi |

## Démarrage local

```bash
./mvnw quarkus:dev

# Consulter les emails envoyés
open http://localhost:8025
```
