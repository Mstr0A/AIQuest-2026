# Native page dependencies

All pages render within MainActivity -> DaleelakTheme -> DaleelakApp.
Theme depends on ui/theme/Color.kt and Type.kt.
App depends on DaleelakViewModel, domain/Models.kt and ui/components/DaleelakIcons.kt.
ViewModel depends on domain/Models.kt, data/DemoCatalog.kt and data/LocalOperationStore.kt.
LocalOperationStore and DemoCatalog depend on domain/Models.kt.

## Home
Entry: features/home/HomeScreen.kt
- ui/components/DaleelakIcons.kt
Counts/callbacks are supplied by app/DaleelakApp.kt, which owns UI-local home routing.

## Assistant
Entry: features/assistant/AssistantScreen.kt
- app/DaleelakViewModel.kt
  - domain/Models.kt
  - data/DemoCatalog.kt
  - data/LocalOperationStore.kt
- data/DemoCatalog.kt
- ui/components/DaleelakIcons.kt

## Operations
Entry: features/operations/OperationsScreen.kt
- app/DaleelakViewModel.kt
- domain/Models.kt
- features/journey/JourneyScreen.kt

## Journey
Entry: features/journey/JourneyScreen.kt
- app/DaleelakViewModel.kt
- domain/Models.kt
Overview page + one card per step + final actions page, vertically snapping.

## Locations
Entry: features/locations/LocationsScreen.kt
No local imports beyond framework. Verified inventory is empty.
