# PLAN — LibraryManager

## 1. Stock par Livre et par Édition

- [X] `GET /books/{bookId}/stock` — agrège les stocks de toutes les copies d'un livre
- [X] `GET /books/{bookId}/stock-by-edition` — stock groupé par format (PAPERBACK, HARDCOVER, etc.)

## 2. Argent gagné par genre

- [X] Créer `RevenueService.getRevenueByGenre()`
- [X] `GET /revenue/by-genre` → `[{genre: "Romance", revenue: 730000}]`
- [X] Chemin : `OrderLine.unitPrice × quantity → Copy → Book → BookCategory → Category`

## 3. Validation de vente avec le stock

- [X] Créer `OrderService.create()` avec validation stock
- [X] Vérifier `Stock.availableStock >= OrderLine.quantity` pour chaque ligne
- [X] Réserver/déduire le stock à la confirmation
- [X] Libérer le stock à l'annulation
- [X] Créer `OrderController` (CRUD + confirm/cancel)
- [X] Créer `OrderLineService` + `OrderLineController`
- [X] DTOs : `OrderRequest`, `OrderResponse`, `OrderLineRequest`, `OrderLineResponse`
- [X] Créer `OrderLineRepository`

## 4. Endpoints requis + doc API

### CRUD manquants
- [X] Author — Repository → Service → Controller → DTOs
- [X] Category — Repository → Service → Controller → DTOs (hiérarchie parent/enfants)
- [X] Client — Repository → Service → Controller → DTOs
- [X] Review — Repository → Service → Controller → DTOs
- [X] Order — Service + Controller (Repository existe)
- [X] OrderLine — Repository → Service → Controller → DTOs

### Endpoints de recherche / filtres
- [X] `GET /books/search?title=&author=&category=`
- [X] `GET /books/{id}/copies`
- [X] `GET /books/{id}/reviews`
- [X] `GET /authors/{id}/books`
- [X] `GET /categories/{id}/books`
- [X] `GET /clients/{id}/orders`
- [X] `GET /clients/{id}/reviews`
- [X] `GET /copies/{id}/stock`
- [X] `GET /copies/{id}/movements`

### Exception handling global
- [X] `@ControllerAdvice` : EntityNotFoundException → 404, MethodArgumentNotValidException → 400, etc.

### API Documentation
- [X] Remplir `docs/api.yml` (OpenAPI 3.0)
- [ ] Ou intégrer SpringDoc OpenAPI (swagger-ui)

### Préprod + Prod (données différentes)
- [ ] Vérifier que preprod et prod utilisent des bases de données différentes
- [ ] Configurer le déploiement prod Poja
- [ ] Renseigner le Google Form : STD, URL preprod, URL prod, URL GitHub

## 5. JaCoCo 80% + Tests

### Configuration
- [X] Passer `minimum = 0` → `minimum = 0.8` dans `jacocoTestCoverageVerification`

### Tests unitaires (Mockito)
- [X] `StockServiceTest`
- [X] `StockMovementServiceTest`
- [X] `OrderServiceTest`
- [X] `RevenueServiceTest`
- [X] `AuthorServiceTest`
- [X] `CategoryServiceTest`
- [X] `ClientServiceTest`
- [X] `ReviewServiceTest`
- [X] `OrderLineServiceTest`
- [X] `BookServiceTest`
- [X] `CopyServiceTest`

### Tests d'intégration (Testcontainers)
- [X] BookControllerTest, CopyControllerTest, EditorControllerTest
- [X] AuthorControllerTest, CategoryControllerTest, ClientControllerTest, ReviewControllerTest, OrderLineControllerTest, OrderControllerTest
- [X] StockControllerTest, StockMovementControllerTest
- [X] ControllerMockMvcTest (tous les contrôleurs en WebMvcTest)

## Déjà fait
- [X] StockRepository + StockMovementRepository + OrderRepository
- [X] StockRequest, StockMovementRequest, StockAdjustRequest
- [X] StockResponse, StockMovementResponse
- [X] StockService + StockMovementService
- [X] StockController + StockMovementController
- [X] `docs/api.yml` créé (vide)
- [X] Book CRUD (complet + tests)
- [X] Copy CRUD (complet + tests)
- [X] Editor CRUD (complet + tests)
