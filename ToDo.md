# TODO — LibraryManager

## 1. CRUD Manquants

Chaque entité nécessite : Repository → DTOs (Request/Response) → Service → Controller

- [X] **Book** — CRUD complet
- [ ] **Author** — CRUD complet
- [ ] **Category** — CRUD complet (gestion hiérarchique parent/enfants)
- [ ] **Client** — CRUD complet
- [X] **Editor** — CRUD complet
- [X] **Copy** — CRUD complet (lié à Book + Editor)
- [ ] **Order** — CRUD complet (lié à Client + OrderLine)
- [ ] **OrderLine** — CRUD (lié à Order + Copy)
- [ ] **Review** — CRUD (lié à Book + Client)
- [ ] **Stock** — CRUD (lié à Copy)
- [ ] **StockMovement** — CRUD (lié à Copy + Order)

## 2. Endpoints Spécifiques

- [ ] `GET /books/{id}/copies` — copies d'un livre
- [ ] `GET /books/{id}/reviews` — avis d'un livre
- [ ] `GET /books/search?title=&author=&category=` — recherche/filtres
- [ ] `GET /authors/{id}/books` — livres d'un auteur
- [ ] `GET /categories/{id}/books` — livres d'une catégorie
- [ ] `GET /clients/{id}/orders` — commandes d'un client
- [ ] `GET /clients/{id}/reviews` — avis d'un client
- [ ] `GET /copies/{id}/stock` — stock d'un exemplaire
- [ ] `GET /copies/{id}/movements` — mouvements de stock

## 3. Pagination

- [ ] Paginer `GET /books` (actuellement retourne tout en une fois)
- [ ] Paginer tous les `GET` de listes

## 4. Exception Handling

- [ ] Créer un `@ControllerAdvice` global pour gérer :
  - `EntityNotFoundException` → 404
  - `MethodArgumentNotValidException` → 400
  - `ConstraintViolationException` → 400
  - `HttpMessageNotReadableException` → 400
  - toute `Exception` non gérée → 500

## 5. Validation

- [ ] Ajouter `@Email`, `@Pattern`, `@Positive`, `@NotNull` sur les DTOs restants
- [ ] Valider les contraintes métier (ex: stock > 0 avant commande)

## 6. API Documentation

- [ ] Intégrer **SpringDoc OpenAPI** (swagger)
- [ ] Documenter les endpoints

## 7. Tests

- [X] Tests unitaires (JUnit + Mockito) pour `BookService`
- [ ] Tests d'intégration pour `BookController` (Testcontainers)
- [ ] Tests pour chaque nouveau service



## 8. Sécurité

- [ ] Ajouter un système d'authentification (Spring Security / JWT)
- [ ] Protéger les endpoints sensibles (création, modification, suppression)
- [ ] Vérifier les accès : un client ne voit que ses propres commandes

## 9. Améliorations

- [ ] Ajouter un "vrai" health endpoint (DB + services externes)
- [ ] Supprimer les entités `Dummy` / `DummyUuid` (utiles uniquement en dev POJA)
- [ ] Gestion des stocks automatique : créer un `StockMovement` + ajuster `Stock` à chaque commande
- [ ] Calcul automatique du `totalAmount` sur `Order` (somme des lignes + shipping)
- [ ] Rate limiting
- [ ] Cache (Redis) pour les lectures fréquentes
