# frontend

Interface utilisateur de BetAmis.

## Responsabilité

- Soumettre et consulter ses pronostics
- Suivre le classement général et par ligue en temps réel
- Recevoir les mises à jour de score via Server-Sent Events (`ranking.updated`)

## Stack technique

> À définir.

Options envisagées :

- React / Next.js
- Vue.js / Nuxt
- Angular

## Connexion aux services

| Service | Protocole |
|---------|-----------|
| prediction-service | REST |
| league-service | REST |
| scoring-service | REST + SSE (`ranking.updated`) |
