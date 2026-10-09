# Extractable native patterns

## AppHeader
- Source: app/DaleelakApp.kt
- Category: layout
- Description: text wordmark, demo caption and optional home-return link
- Extractable props: showHome (boolean)
- Hardcoded: text-only DALEELAK identity, colors, typography and home icon

## OperationEntry
- Source: features/home/HomeScreen.kt
- Category: basic
- Description: outlined row with sky icon surface, title/subtitle and count
- Extractable props: count (number)
- Hardcoded: layout, spacing, outline and typography; each entry's label/icon comes from native source
