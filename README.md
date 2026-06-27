# Module Wallet — Portefeuille Virtuel (Francs CFA - XOF)

## Présentation du Projet

Ce projet implémente un **module de portefeuille virtuel** pour une plateforme e-commerce, avec des devises en **Francs CFA (XOF)**. Il a été construit en suivant une démarche pédagogique progressive en **6 niveaux**, chacun introduisant un concept fondamental du développement d'API REST modernes avec **Spring Boot 4.1.0** et **Java 21**.

### Les 6 Niveaux d'Apprentissage

| Niveau | Concept | Objectif |
|--------|---------|----------|
| **1** | IoC & DI (Inversion of Control / Dependency Injection) | Laisser Spring gérer le cycle de vie des objets |
| **2** | DTO & REST Niveau 2 | Exposer une API sans fuite de données, avec validation |
| **3** | Exceptions Métier & Codes HTTP | Traduire les règles métier en codes HTTP standardisés |
| **4** | HATEOAS (Niveau 3 de Richardson) | Rendre l'API découvrable via l'hypermedia |
| **5** | Persistance JPA / PostgreSQL | Connecter l'application à une base de données réelle |
| **6** | Événements Domaine & Découplage | Réagir à des événements sans créer de dépendances directes |

---

## Arborescence du Projet

```
wallet/
├── pom.xml                                           # Configuration Maven (dépendances, plugins)
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/
│   │   │   ├── wallet/                               # Package principal (feature "wallet")
│   │   │   │   ├── WalletApplication.java            # Point d'entrée Spring Boot
│   │   │   │   ├── entity/
│   │   │   │   │   └── Wallet.java                   # Entité métier JPA
│   │   │   │   ├── service/
│   │   │   │   │   ├── WalletFundingService.java      # Interface publique du service
│   │   │   │   │   └── WalletFundingServiceImpl.java  # Implémentation (package-private)
│   │   │   │   ├── repository/
│   │   │   │   │   └── WalletRepository.java          # Accès aux données JPA
│   │   │   │   ├── controller/
│   │   │   │   │   ├── WalletController.java          # Contrôleur REST
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── FundWalletRequestDTO.java  # DTO d'entrée (record)
│   │   │   │   │       └── WalletResponseDTO.java     # DTO de sortie (HATEOAS)
│   │   │   │   ├── exception/
│   │   │   │   │   ├── InsufficientBalanceException.java  # Solde insuffisant
│   │   │   │   │   ├── WalletNotFoundException.java       # Client introuvable
│   │   │   │   │   └── WalletExceptionHandler.java        # Handler global (@ControllerAdvice)
│   │   │   │   └── event/
│   │   │   │       └── LowBalanceEvent.java           # Événement domaine (solde < 5000 XOF)
│   │   │   └── alerts/                               # Package indépendant (feature "alerts")
│   │   │       ├── entity/
│   │   │       │   └── Alert.java                    # Entité JPA (table "alerts")
│   │   │       ├── repository/
│   │   │       │   └── AlertRepository.java          # Accès aux données JPA
│   │   │       ├── service/
│   │   │       │   └── AlertService.java             # Service métier des alertes
│   │   │       └── listener/
│   │   │           └── LowBalanceEventListener.java  # Écouteur d'événements Spring
│   │   └── resources/
│   │       └── application.properties                # Configuration PostgreSQL + JPA
│   └── test/java/com/ecommerce/wallet/
│       └── WalletApplicationTests.java               # Test de contexte Spring Boot
```

---

## Explication Détaillée des Fichiers

### 1. `pom.xml` — Configuration Maven

Le fichier déclare les dépendances nécessaires au projet :

| Dépendance | Rôle |
|------------|------|
| `spring-boot-starter-data-jpa` | Intégration JPA / Hibernate pour la persistance |
| `spring-boot-starter-webmvc` | Contrôleurs REST (Spring MVC) |
| `spring-boot-starter-validation` | Validation automatique (`@Valid`, `@Positive`, etc.) |
| `spring-boot-starter-hateoas` | Hypermedia / HATEOAS pour les réponses REST |
| `postgresql` (runtime) | Pilote JDBC pour PostgreSQL |
| `lombok` (optional) | Réduction du code boilerplate (non utilisé dans ce projet) |

### 2. `application.properties` — Configuration Base de Données

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wallet_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update    # Hibernate crée/met à jour les tables automatiquement
spring.jpa.show-sql=true                # Affiche les requêtes SQL dans la console
```

### 3. `WalletApplication.java` — Point d'Entrée

```java
@SpringBootApplication
public class WalletApplication { ... }
```

L'annotation `@SpringBootApplication` active automatiquement :
- La **configuration automatique** Spring Boot
- Le **scan des composants** (détection des `@Service`, `@RestController`, etc.)
- La **configuration JPA** (détection des `@Entity`, `JpaRepository`)

### 4. `Wallet.java` — L'Entité Métier

C'est le cœur du domaine. Elle respecte plusieurs contraintes fortes :

```java
@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    protected Wallet() { }  // Constructeur PROTECTED pour JPA uniquement
}
```

**Règles métier protégées (invariants) :**
- Le solde `balance` ne peut jamais devenir négatif
- Les méthodes `addFunds()` et `deductFunds()` valident leurs arguments
- `deductFunds()` lève `InsufficientBalanceException` si le solde est insuffisant
- **Aucun `@Setter`** : le solde ne peut être modifié que par les méthodes métier
- **Constructeur vide `protected`** (pas `public` !) pour satisfaire JPA sans exposer de risque

#### Piège JPA Évitée

JPA exige un constructeur sans argument. Beaucoup de développeurs le mettent en `public`, ce qui permet d'instancier un `Wallet` vide et incohérent. Ici, il est **`protected`** : accessible par Hibernate (via reflection) mais pas par le code applicatif.

### 5. `WalletFundingService.java` — Interface du Service

```java
public interface WalletFundingService {
    void addFunds(String clientId, BigDecimal amount);
    void deductFunds(String clientId, BigDecimal amount);
    WalletResponseDTO getBalance(String clientId);
}
```

L'interface est **publique** car elle définit le contrat que les autres couches (contrôleur) doivent utiliser.

### 6. `WalletFundingServiceImpl.java` — Implémentation (Package-Private)

```java
@Service
class WalletFundingServiceImpl implements WalletFundingService {
    // ...
}
```

**Pourquoi `package-private` ?** Pour que seules les classes du même package puissent instancier ou référencer cette implémentation. Le reste du projet (et le monde extérieur) doit passer par l'interface `WalletFundingService`. C'est le principe de **programmation par interface**.

**Injection par constructeur :** Spring injecte automatiquement `WalletRepository` et `ApplicationEventPublisher` via le constructeur.

**Logique métier :**
- `addFunds()` : cherche le portefeuille ou le crée s'il n'existe pas, puis ajoute les fonds
- `deductFunds()` : cherche le portefeuille (ou lève `WalletNotFoundException`), débite, et publie un événement si le solde passe sous 5 000 XOF
- `getBalance()` : retourne un DTO avec les informations du portefeuille

### 7. `WalletRepository.java` — Accès aux Données

```java
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByClientId(String clientId);
}
```

Hérite de `JpaRepository` qui fournit déjà les opérations CRUD de base. La méthode `findByClientId()` est dérivée du nom : Spring Data JPA génère automatiquement la requête `SELECT * FROM wallets WHERE client_id = ?`.

### 8. `WalletController.java` — Contrôleur REST

```java
@RestController
@RequestMapping("/api/wallets")
public class WalletController { ... }
```

Expose 3 endpoints :

| Méthode | Endpoint | Corps | Description |
|---------|----------|-------|-------------|
| **POST** | `/api/wallets/{clientId}/fund` | `{"amount": 5000}` | Recharger le portefeuille |
| **POST** | `/api/wallets/{clientId}/pay` | `{"amount": 5000}` | Effectuer un paiement |
| **GET** | `/api/wallets/{clientId}` | — | Consulter le solde (avec liens HATEOAS) |

### 9. `FundWalletRequestDTO.java` — DTO d'Entrée (Record)

```java
public record FundWalletRequestDTO(@Positive BigDecimal amount) { }
```

Un **record Java** (nouveauté Java 16+) qui sert de conteneur de données immuable. L'annotation `@Positive` de Jakarta Validation garantit que le montant est strictement positif — une requête avec `amount = 0` ou `amount = -100` sera rejetée avec une erreur 400.

### 10. `WalletResponseDTO.java` — DTO de Sortie avec HATEOAS

```java
public class WalletResponseDTO extends RepresentationModel<WalletResponseDTO> {
    private final String clientId;
    private final BigDecimal balance;
}
```

En étendant `RepresentationModel` de Spring HATEOAS, ce DTO peut transporter des **liens hypermedia**. La réponse JSON inclut automatiquement un tableau `links` qui indique au client les actions possibles :

```json
{
  "clientId": "client123",
  "balance": 15000,
  "links": [
    { "rel": "self", "href": "/api/wallets/client123" },
    { "rel": "fund", "href": "/api/wallets/client123/fund" },
    { "rel": "history", "href": "/api/wallets/client123/history" }
  ]
}
```

**Pourquoi HATEOAS ?** Au niveau 3 du modèle de maturité de Richardson, l'API devient "découvrable" : le client n'a pas besoin de documentation externe pour naviguer — les liens sont dans la réponse elle-même.

### 11. `WalletExceptionHandler.java` — Gestion Centralisée des Erreurs

```java
@ControllerAdvice
public class WalletExceptionHandler { ... }
```

Traduit les exceptions métier en codes HTTP standardisés :

| Exception | Code HTTP | Signification |
|-----------|-----------|---------------|
| `IllegalArgumentException` | **400 Bad Request** | Requête invalide (montant négatif, etc.) |
| `WalletNotFoundException` | **404 Not Found** | Portefeuille inexistant |
| `InsufficientBalanceException` | **409 Conflict** | Conflit métier (solde insuffisant) |

### 12. `LowBalanceEvent.java` — Événement Domaine

```java
public class LowBalanceEvent {
    private final String clientId;
    private final BigDecimal currentBalance;
}
```

Un simple POJO qui transporte les informations nécessaires. Il est publié par `WalletFundingServiceImpl` via `ApplicationEventPublisher` lorsque le solde passe sous 5 000 XOF après un paiement.

### 13. Package `com.ecommerce.alerts` — Système d'Alerte Découplé

Ce package implémente une **nouvelle fonctionnalité** sans créer de dépendance vers `Wallet`. Tout le couplage se fait via l'événement `LowBalanceEvent`.

#### `Alert.java` — Entité JPA

```java
@Entity
@Table(name = "alerts")
public class Alert {
    private Long id;
    private String clientId;
    private String message;
    private LocalDateTime createdAt;
}
```

Enregistre les alertes de sécurité dans la table `alerts`.

#### `AlertService.java` — Service

```java
@Service
public class AlertService {
    public void createAlert(String clientId, String message) { ... }
}
```

#### `LowBalanceEventListener.java` — Écouteur d'Événements

```java
@Component
public class LowBalanceEventListener {
    @EventListener
    public void handleLowBalance(LowBalanceEvent event) { ... }
}
```

**Le point clé :** cet écouteur importe `LowBalanceEvent` (un simple POJO) mais **jamais** la classe `Wallet`. Le package `alerts` est totalement indépendant du package `wallet` — le couplage se fait uniquement via des événements. C'est le principe d'**Inversion de Dépendance** et de **couplage faible**.

---

## Flux d'Exécution

### Rechargement (`POST /api/wallets/{clientId}/fund`)

```
Client HTTP
    │
    ▼
WalletController.fundWallet()
    │ @Valid FundWalletRequestDTO (contient amount)
    ▼
WalletFundingService.addFunds()
    │
    ├─► WalletRepository.findByClientId()
    │      └─► Existe ? → Wallet existant
    │      └─► Pas trouvé ? → Nouveau Wallet(balance=0)
    │
    ├─► Wallet.addFunds(amount)
    │      └─► Valide que amount > 0
    │      └─► balance += amount
    │
    └─► WalletRepository.save(wallet)
```

### Paiement (`POST /api/wallets/{clientId}/pay`)

```
Client HTTP
    │
    ▼
WalletController.payFromWallet()
    │
    ▼
WalletFundingService.deductFunds()
    │
    ├─► WalletRepository.findByClientId()
    │      └─► Pas trouvé ? → WalletNotFoundException → 404
    │
    ├─► Wallet.deductFunds(amount)
    │      └─► Solde insuffisant ? → InsufficientBalanceException → 409
    │      └─► OK → balance -= amount
    │
    ├─► WalletRepository.save(wallet)
    │
    └─► Balance < 5 000 XOF ?
           └─► Oui → ApplicationEventPublisher.publishEvent(LowBalanceEvent)
                       │
                       ▼
              LowBalanceEventListener.handleLowBalance()
                       │
                       ▼
              AlertService.createAlert()
                       │
                       ▼
              AlertRepository.save(new Alert(...))
```

### Consultation (`GET /api/wallets/{clientId}`)

```
Client HTTP
    │
    ▼
WalletController.getWallet()
    │
    ├─► WalletFundingService.getBalance()
    │      └─► WalletRepository.findByClientId() → WalletNotFoundException → 404
    │
    └─► Retourne EntityModel<WalletResponseDTO>
           ├─► clientId
           ├─► balance
           └─► _links (self, fund, history)
```

---

## Endpoints de l'API

```bash
# Recharger un portefeuille (crée le portefeuille s'il n'existe pas)
curl -X POST http://localhost:8080/api/wallets/client123/fund \
  -H "Content-Type: application/json" \
  -d '{"amount": 10000}'

# Effectuer un paiement
curl -X POST http://localhost:8080/api/wallets/client123/pay \
  -H "Content-Type: application/json" \
  -d '{"amount": 3000}'

# Consulter le solde (avec liens HATEOAS)
curl http://localhost:8080/api/wallets/client123

# Exemple de réponse :
# {
#   "clientId": "client123",
#   "balance": 7000,
#   "links": [
#     { "rel": "self", "href": "/api/wallets/client123" },
#     { "rel": "fund", "href": "/api/wallets/client123/fund" },
#     { "rel": "history", "href": "/api/wallets/client123/history" }
#   ]
# }
```

---

## Concepts Clés Illustrés

### 1. Inversion de Contrôle (IoC)
Spring instancie et gère le cycle de vie des objets (`@Service`, `@RestController`, `JpaRepository`). Le développeur se concentre sur la logique métier.

### 2. Injection de Dépendances (DI)
Les dépendances sont fournies via le constructeur, pas créées manuellement :
```java
WalletFundingServiceImpl(WalletRepository repo, ApplicationEventPublisher publisher) { ... }
```

### 3. Encapsulation (Séance 7)
- `WalletFundingServiceImpl` est `package-private` → invisible depuis l'extérieur du package
- `Wallet` n'a pas de `@Setter` → l'état ne change que via les méthodes métier
- Constructeur `protected` pour JPA → pas d'instanciation vide par le code applicatif

### 4. DTO vs Entité
Le contrôleur reçoit un `FundWalletRequestDTO` (record) et non l'entité `Wallet`. Cela évite de exposer la structure interne de la base de données.

### 5. HATEOAS — Niveau 3 de Richardson
L'API ne se contente pas de retourner des données : elle retourne aussi des **liens** qui guident le client vers les actions disponibles.

### 6. Événements Domaine
Au lieu d'importer `AlertService` directement dans `WalletFundingServiceImpl` (ce qui créerait une dépendance circulaire), on publie un événement. Le système d'alerte écoute et réagit de manière totalement découplée.

### 7. Package by Feature
Chaque fonctionnalité métier est regroupée dans son propre package :
- `com.ecommerce.wallet` → tout ce qui concerne le portefeuille
- `com.ecommerce.alerts` → tout ce qui concerne les alertes

---

## Prérequis pour Exécuter

1. **Java 21+** installé
2. **PostgreSQL** en cours d'exécution sur `localhost:5432`
3. Base de données `wallet_db` créée :
   ```sql
   CREATE DATABASE wallet_db;
   ```
4. Lancer l'application :
   ```bash
   ./mvnw spring-boot:run
   ```

Les tables `wallets` et `alerts` sont créées automatiquement par Hibernate (`ddl-auto=update`).
