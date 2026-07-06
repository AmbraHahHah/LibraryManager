# PLAN — LibraryManager

## 1. Stock par Livre et par Édition

- [X] `GET /books/{bookId}/stock` — agrège les stocks de toutes les copies d'un livre
- [X] `GET /books/{bookId}/stock-by-edition` — stock groupé par format (PAPERBACK, HARDCOVER, etc.)

## 2. Argent gagné par genre

- [X] Créer `RevenueService.getRevenueByGenre()`
- [X] `GET /revenue/by-genre` → `[{genre: "Romance", revenue: 730000}]`
- [X] Chemin : `OrderLine.unitPrice × quantity → Copy → Book → BookCategory → Category`

## 3. Validation de vente avec le stock

- [ ] Créer `OrderService.create()` avec validation stock
- [ ] Vérifier `Stock.availableStock >= OrderLine.quantity` pour chaque ligne
- [ ] Réserver/déduire le stock à la confirmation
- [ ] Libérer le stock à l'annulation
- [ ] Créer `OrderController` (CRUD)
- [ ] Créer `OrderLineService` + `OrderLineController`
- [ ] DTOs : `OrderRequest`, `OrderResponse`, `OrderLineRequest`, `OrderLineResponse`
- [ ] Créer `OrderLineRepository`

## 4. Endpoints requis + doc API

### CRUD manquants
- [ ] Author — Repository → Service → Controller → DTOs
- [ ] Category — Repository → Service → Controller → DTOs (hiérarchie parent/enfants)
- [ ] Client — Repository → Service → Controller → DTOs
- [ ] Review — Repository → Service → Controller → DTOs
- [ ] Order — Service + Controller (Repository existe)
- [ ] OrderLine — Repository → Service → Controller → DTOs

### Endpoints de recherche / filtres
- [ ] `GET /books/search?title=&author=&category=`
- [ ] `GET /books/{id}/copies`
- [ ] `GET /books/{id}/reviews`
- [ ] `GET /authors/{id}/books`
- [ ] `GET /categories/{id}/books`
- [ ] `GET /clients/{id}/orders`
- [ ] `GET /clients/{id}/reviews`
- [ ] `GET /copies/{id}/stock`
- [ ] `GET /copies/{id}/movements`

### Exception handling global
- [ ] `@ControllerAdvice` : EntityNotFoundException → 404, MethodArgumentNotValidException → 400, etc.

### API Documentation
- [ ] Remplir `docs/api.yml` (OpenAPI 3.0)
- [ ] Ou intégrer SpringDoc OpenAPI (swagger-ui)

### Préprod + Prod (données différentes)
- [ ] Vérifier que preprod et prod utilisent des bases de données différentes
- [ ] Configurer le déploiement prod Poja
- [ ] Renseigner le Google Form : STD, URL preprod, URL prod, URL GitHub

## 5. JaCoCo 80% + Tests

### Configuration
- [ ] Passer `minimum = 0` → `minimum = 0.8` dans `jacocoTestCoverageVerification`

### Tests unitaires (Mockito)
- [ ] `StockServiceTest`
- [ ] `StockMovementServiceTest`
- [ ] `OrderServiceTest`
- [ ] `RevenueServiceTest`
- [ ] `AuthorServiceTest`
- [ ] `CategoryServiceTest`
- [ ] `ClientServiceTest`
- [ ] `ReviewServiceTest`

### Tests d'intégration (Testcontainers)
- [ ] Contrôleurs existants (Book, Copy, Editor — déjà existants)
- [ ] Nouveaux contrôleurs (Stock, StockMovement, Order, etc.)

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
