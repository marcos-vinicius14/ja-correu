# AGENTS.md - Ja Correu Development Guide

## Project Overview

Ja Correu is a monorepo containing:
- **Backend**: Spring Boot 4.0.3 (Java 21) with hexagonal architecture
- **Frontend**: Angular 21 with TypeScript

## Build Commands

### Root (pnpm workspace)

```bash
pnpm run dev              # Start frontend + backend simultaneously
pnpm run start            # Alias for dev
pnpm run build            # Build both frontend and backend
pnpm run test             # Run frontend tests
```

### Backend (Spring Boot / Maven)

```bash
# Build
cd apps/backend && ./mvnw clean package -DskipTests

# Run
cd apps/backend && ./mvnw spring-boot:run

# Run single test class
cd apps/backend && ./mvnw test -Dtest=LoginUseCaseTest

# Run single test method
cd apps/backend && ./mvnw test -Dtest=LoginUseCaseTest#execute_success_returnsTokens

# Run all tests
cd apps/backend && ./mvnw test
```

### Frontend (Angular / npm)

```bash
cd apps/frontend

# Development
npm run start             # ng serve (http://localhost:4200)
npm run watch             # ng build --watch --configuration development

# Build
npm run build             # Production build

# Tests (Vitest)
npm run test              # Run all tests with watch mode
npm run test -- --run     # Run tests once (CI mode)
npm run test -- --run --coverage  # With coverage
```

## TDD Cycle (Obrigatório)

Todo desenvolvimento Java DEVE seguir o ciclo TDD rigorosamente:

### 1. Red (Escreva o teste que falha)
- Escreva o teste ANTES de implementar qualquer funcionalidade
- O teste deve compilar e falhar com mensagem clara
- Use nomes descritivos: `<method>_<scenario>_<expected>`

```java
@Test
void execute_validCredentials_returnsAccessToken() {
    // Arrange
    LoginRequest request = new LoginRequest("user@test.com", "password123");
    
    // Act
    Result<TokenResponse> result = loginUseCase.execute(request);
    
    // Assert
    assertTrue(result.isSuccess());
    assertNotNull(result.getData().accessToken());
}
```

### 2. Green (Faça o teste passar)
- Implemente o mínimo necessário para o teste passar
- Não otimizações prematuras
- Ignore CodeStyle temporariamente se necessário

### 3. Refactor (Melhore o código)
- Aplique boas práticas e Java moderno
- Execute testes para garantir que nada quebrou
- Execute lint e formatação

### Workflow TDD

```bash
# 1. Crie o teste (Red)
# 2. Execute: ./mvnw test -Dtest=NomeClasseTest#nomeMetodo
# 3. Implemente ate passar (Green)
# 4. Refatore (Refactor)
# 5. Execute testes novamente
# 6. Rode: ./mvnw spotless:apply format
```

## Code Style Guidelines

### General

- **No comments** unless explicitly requested
- Use meaningful variable and method names
- Keep functions small and focused
- Follow existing patterns in each module

### Java (Backend) - Modern Java 21+

#### Architecture: Hexagonal (Ports & Adapters)

```
src/main/java/org/jacorreu/
├── identity/          # Authentication module
│   ├── application/   # Use cases, DTOs
│   ├── core/          # Domain logic, interfaces (ports)
│   ├── infra/         # Adapters (controllers, persistence, security)
│   └── shared/        # Shared utilities (validation)
├── user/              # User module (same structure)
└── shared/            # Cross-module shared code
```

#### Modern Java 21+ Features (OBRIGATÓRIO)

**Records para DTOs e Value Objects:**
```java
public record TokenResponse(String accessToken, String refreshToken) { }

public record LoginRequest(String email, String password) { }
```

**Sealed Classes para Herança Controlada:**
```java
public sealed interface Result<T> permits Result.Success, Result.Failure {
    boolean isSuccess();
    T getData();
    Notification getNotification();
    
    static <T> Result<T> success(T data) { return new Success<>(data); }
    static <T> Result<T> failure(Notification notification) { return new Failure<>(notification); }
    
    record Success<T>(T data) implements Result<T> {
        @Override public boolean isSuccess() { return true; }
        @Override public T getData() { return data; }
        @Override public Notification getNotification() { return Notification.EMPTY; }
    }
    
    record Failure<T>(Notification notification) implements Result<T> {
        @Override public boolean isSuccess() { return false; }
        @Override public T getData() { throw new IllegalStateException("No data on failure"); }
        @Override public Notification getNotification() { return notification; }
    }
}
```

**Result com métodos funcionais (map, flatMap, onFailure):**
```java
public final class Result<T> {
    // ...existing methods...
    
    public void onFailure(Consumer<Error> action) {
        if (isFailure() && notification != null) {
            notification.getErrors().forEach(action);
        }
    }
    
    public <R> Result<R> map(Function<T, R> mapper) {
        if (isSuccess()) return Result.success(mapper.apply(data));
        return Result.failure(notification);
    }
    
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (isSuccess()) return mapper.apply(data);
        return Result.failure(notification);
    }
}
```

**Notification com fluent API:**
```java
public final class Notification {
    private final List<Error> errors = new ArrayList<>();
    
    public Notification addError(String field, String message) {
        errors.add(Error.of(field, message));
        return this;
    }

    public <T> Notification merge(Result<T> result) {
        if (result != null && !result.isSuccess()) {
            result.getNotification().getErrors().forEach(e -> addError(e.field(), e.message()));
        }
        return this;
    }
}
```

Exemplo de uso para propagar erros de Value Objects:
```java
notification.merge(AvailableDaysPerWeek.create(command.availableDaysPerWeek()));
notification.merge(CurrentPacePerKm.create(command.currentPacePerKm()));
```

**Nota:** Java 21 NÃO suporta switch expressions com boolean. Use operador ternário para booleanos:
```java
// Errado - não compila em Java 21
return switch (condition) {
    case true -> ok;
    case false -> fail;
};

// Correto - use operador ternário
return condition ? ok : fail;
```

**Pattern Matching para instanceof e switch:**
```java
// Pattern matching instanceof
if (obj instanceof TokenResponse(String access, String refresh)) {
    return access + refresh;
}

// Switch expressions
String status = switch (result) {
    case Success s -> "ok";
    case Failure f -> "error";
};
```

**Text Blocks para strings multilinha:**
```java
String json = """
    {
        "name": "%s",
        "email": "%s"
    }
    """.formatted(name, email);
```

**Virtual Threads (Thread.Builder):**
```java
// Para operacoes I/O-bound
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> repository.save(entity));
}
```

**Local-Variable Type Inference (var):**
```java
var user = userRepository.findByEmail(email);
var notification = new Notification();
```

#### Naming Conventions

- **Packages**: lowercase, e.g., `org.jacorreu.identity.infra.web.controllers`
- **Classes**: PascalCase, e.g., `LoginUseCase`, `UserDomain`
- **Methods**: camelCase, e.g., `findByEmail`, `execute`
- **Constants**: UPPER_SNAKE_CASE, e.g., `REFRESH_TOKEN_TTL`
- **Tests**: `<ClassName>Test`, e.g., `LoginUseCaseTest`
- **Records**: PascalCase, same as classes
- **Sealed interfaces**: PascalCase with `Result`, `Either` naming
- **Use Cases**: suffixed with `UseCase`, e.g., `LoginUseCase`, `OutboxEventUseCase`

#### UUID v7 (Time-Based)

Use UUID v7 (time-based) para IDs que precisam de ordenação temporal. Não use `UUID.randomUUID()` (v4 aleatório).

```java
import com.fasterxml.uuid.Generators;

// Gera UUID v7 (time-based)
UUID id = Generators.timeBasedEpochGenerator().generate();
```

**Exemplo em factory methods:**
```java
public record OutboxEvent(...) {
    public static OutboxEvent create(UUID aggregateId, String eventType, String payload) {
        return new OutboxEvent(
                Generators.timeBasedEpochGenerator().generate(),  // UUID v7
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.PENDING,  // gerado internamente
                Instant.now(),               // gerado internamente
                null
        );
    }
}
```

**Benefícios do UUID v7:**
- Ordenável por tempo (útil para logs, debug, outbox events)
- Não expõe informação sensível (como UUID v1 com MAC address)
- Suportado nativamente pelo PostgreSQL (uuid-ossp)

#### Imports

- Order: static imports first, then external, then internal
- Group by package, blank line between groups

```java
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.stereotype.Controller;
import org.jacorreu.identity.application.usecase.LoginUseCase;
```

#### Error Handling

- Use `Result<T>` from `org.jacorreu.shared.validation`
- Return `Result.failure(notification)` on errors
- Controllers return `ResponseEntity<?>` with proper HTTP status codes

```java
if (!result.isSuccess()) {
    return errorResponse(HttpStatus.BAD_REQUEST, "Erro ao criar usuário", result, "Hint message");
}
```

#### Functions Small and Focused

Use cases devem ser quebrados em funções pequenas e focadas. Cada função deve fazer apenas uma coisa:

```java
public Result<AthleteProfileResult> execute(CompleteOnboardingCommand command) {
    var notification = new Notification();

    var user = validateUser(command.userId(), notification);
    if (notification.hasErrors()) {
        return Result.failure(notification);
    }

    validateInput(command, notification);
    if (notification.hasErrors()) {
        return Result.failure(notification);
    }

    var profile = createAndSaveProfile(command);
    activateUser(user);

    return Result.success(buildResult(profile));
}

private UserDomain validateUser(UUID userId, Notification notification) { }
private void validateInput(CompleteOnboardingCommand command, Notification notification) { }
private AthleteProfileDomain createAndSaveProfile(CompleteOnboardingCommand command) { }
private void activateUser(UserDomain user) { }
private AthleteProfileResult buildResult(AthleteProfileDomain profile) { }
```

**Regras:**
- O método `execute` orquestra o fluxo
- Validações retornam cedo (`return Result.failure()`)
- Operações de persistência e transformação em métodos separados
- Instanciar objetos uma única vez (ex: `Notification`)

#### Functional Approach (Optional + Result)

Prefira código funcional com Optional e Result ao invés de lançar exceptions. Minimize o uso de ifs usando composição:

```java
public Result<AthleteProfileResult> execute(CompleteOnboardingCommand command) {
    var notification = new Notification();

    return userRepository.findById(command.userId())
            .<Result<AthleteProfileResult>>map(user -> validateUserStatus(user, command, notification))
            .orElseGet(() -> {
                notification.addError("userId", "Usuário não encontrado");
                return Result.failure(notification);
            });
}

private Result<AthleteProfileResult> validateUserStatus(UserDomain user,
        CompleteOnboardingCommand command, Notification notification) {

    return switch (user.getStatus()) {
        case UserStatus.Active active -> {
            notification.addError("already_active", "Onboarding já foi concluído");
            yield Result.failure(notification);
        }
        default -> validateInput(command, notification);
    };
}

private Result<AthleteProfileResult> validateInput(CompleteOnboardingCommand command,
        Notification notification) {

    AvailableDaysPerWeek.create(command.availableDaysPerWeek())
            .onFailure(e -> notification.addError(e.field(), e.message()));

    CurrentPacePerKm.create(command.currentPacePerKm())
            .onFailure(e -> notification.addError(e.field(), e.message()));

    return notification.hasErrors()
            ? Result.failure(notification)
            : createProfile(command);
}

private Result<AthleteProfileResult> createProfile(CompleteOnboardingCommand command) {
    var days = AvailableDaysPerWeek.create(command.availableDaysPerWeek()).getData();
    var pace = CurrentPacePerKm.create(command.currentPacePerKm()).getData();

    var profile = AthleteProfileDomain.create(command.userId(), command.goal(),
            command.level(), days, pace, command.injuriesNotes());

    athleteProfileRepository.save(profile);
    embeddingGateway.generateOnboardingEmbedding(profile);

    return Result.success(buildResult(profile));
}
```

**Benefícios:**
- Pipeline de operações sem múltiplos ifs
- Optional como container de falha (não lança exceptions)
- Cada método retorna `Result` - composição funcional
- Use operador ternário para booleanos (Java 21 não suporta switch com boolean)
- Use `.orElseGet()` ao invés de `.orElse(null)` para evitar NullPointerException

#### Domain Objects

- Use factory methods: `UserDomain.restore()`, `Email.restore()`
- Value objects are immutable (use records)
- Keep domain logic in core layer, not in controllers
- Use `assert` statements for invariants

```java
public record Email(String value) {
    public static Email restore(String email) {
        assert email != null && !email.isBlank();
        return new Email(email.toLowerCase());
    }
}
```

#### Test Implementation (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Test
    void execute_validCredentials_returnsTokens() {
        // Arrange
        var request = new LoginRequest(EMAIL, PASSWORD);
        var user = UserDomain.restore(UUID.randomUUID(), "test", EMAIL, "encoded", null, null, null);
        var expectedToken = new TokenResponse("access", "refresh");
        
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(issueTokenUseCase.execute(user)).thenReturn(Result.success(expectedToken));

        // Act
        var result = loginUseCase.execute(request);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("access", result.getData().accessToken());
        verify(issueTokenUseCase, times(1)).execute(user);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void execute_nullEmail_returnsFailure(String email) {
        var request = new LoginRequest(email, "password");
        var result = loginUseCase.execute(request);
        
        assertFalse(result.isSuccess());
    }
}
```

### TypeScript/Angular (Frontend)

#### Structure

```
src/app/
├── *.ts               # Standalone components
├── *.html             # Templates
└── *.scss             # Styles
```

#### Standalone Components

```typescript
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('Ja Correu');
}
```

#### Naming Conventions

- **Components**: PascalCase, e.g., `LoginComponent`
- **Files**: kebab-case, e.g., `login.component.ts`
- **Signals**: camelCase with `Signal<T>` type annotation
- **Methods**: camelCase

#### TypeScript Rules (strict mode enabled)

- Always define return types for methods
- Use `protected` for component properties accessed in templates
- Use `readonly` for immutable fields
- Prefer signals over BehaviorSubject

#### Prettier Formatting

- Print width: 100
- Single quotes: true
- Run before commit: `npx prettier --write .`

## Environment Variables

Create `.env` in project root:

```env
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=jacorreu_db
DATABASE_URL=jdbc:postgresql://localhost:5432/jacorreu_db
JWT_SECRET=your_jwt_secret
OPENAI_API_KEY=your_openai_key
```

## Database

```bash
# Start PostgreSQL
docker compose -f apps/backend/compose.yaml up -d db
```

## Common Issues

- Backend won't start: Check PostgreSQL is running
- Frontend won't start: Run `npm install` in `apps/frontend`
- Test failures: Ensure Docker is running for integration tests

## Outbox Pattern

O padrão Outbox resolve o problema de consistência em operações que envolvem múltiplos sistemas (ex: banco de dados + serviço externo).

### O Problema

Quando você faz duas operações que precisam ser atômicas mas pertencem a sistemas diferentes:
1. Salva `athlete_profile` → Postgres ✅ garantido pela transação
2. Gera embedding no PGVector → chamada externa ❌ pode falhar independentemente

Não existe transação distribuída entre as duas. Se a aplicação cair entre o passo 1 e 2, o perfil existe mas o embedding nunca é gerado — inconsistência silenciosa.

### A Solução — Outbox

Tratar o evento como dado, não como efeito colateral. Você persiste a intenção de executar a operação junto com a entidade principal, na mesma transação.

**Tabela outbox_event:**
```sql
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP
);
```

### Fluxo

**Fase 1 — Dentro da transação do use case:**
```
BEGIN TRANSACTION
  → INSERT INTO athlete_profile (...)
  → INSERT INTO outbox_event (aggregate_id, event_type, payload, status)
COMMIT
```

**Fase 2 — Scheduler independente (a cada 10 segundos):**
```
→ SELECT * FROM outbox_event WHERE status = 'PENDING'
→ Para cada evento: processar e atualizar status para PROCESSED/FAILED
```

### Quando Usar

- Operações que envolvem banco de dados + serviços externos
- Necessidade de garantir que ambas as operações ocorram ou nenhuma ocorrer
- Processamento assíncrono que pode ser retomado após falhas

### Estrutura (módulo shared)

```
org/jacorreu/outbox/
├── core/
│   ├── domain/
│   │   ├── OutboxEvent.java           # Record do evento
│   │   └── OutboxEventStatus.java     # Enum: PENDING, PROCESSED, FAILED
│   └── gateway/
│       └── OutboxEventRepository.java # Interface
├── application/
│   └── OutboxEventService.java       # Publish de eventos
└── infra/
    ├── persistence/
    │   ├── entity/OutboxEventEntity.java
    │   └── repository/SpringDataOutboxEventRepository.java
```

### Exemplo de Uso

```java
// No use case - em vez de chamar gateway diretamente:
eventPublisher.publish(profileId, "ONBOARDING_COMPLETED", profileId.toString());

// Scheduler processa eventos pendentes:
@Scheduled(fixedRate = 10000)
public void processOutboxEvents() {
    var events = repository.findAllByStatusOrderByCreatedAt(PENDING);
    for (var event : events) {
        try {
            handler.handle(event);
            repository.updateStatus(event.id(), PROCESSED);
        } catch (Exception e) {
            repository.updateStatus(event.id(), FAILED);
        }
    }
}
```

### Baixo Acoplamento (Ports & Adapters entre Módulos)

Para garantir baixo acoplamento entre módulos, siga o princípio de **Dependency Inversion** (SOLID):

- **Módulos de domínio definem interfaces (ports)**
- **Módulos de infraestrutura implementam (adapters)**

**Errado — Alto Acoplamento:**
```java
// Módulo onboarding depende diretamente da implementação do módulo outbox
import org.jacorreu.outbox.application.OutboxEventService;

private final OutboxEventService outboxEventService;
```

**Correto — Baixo Acoplamento:**
```java
// Módulo onboarding define a interface (port)
package org.jacorreu.onboarding.core.gateway;
public interface EventPublisher {
    void publish(UUID aggregateId, String eventType, String payload);
}

// Use case depende da abstração
private final EventPublisher eventPublisher;

// Módulo outbox fornece a implementação (adapter)
package org.jacorreu.outbox.infra.adapter;
@Component
public class OutboxEventPublisher implements EventPublisher { ... }
```

**Benefícios:**
- Módulo onboarding não conhece o módulo outbox
- Easy de trocar implementação (ex: Kafka, RabbitMQ, etc)
- Use cases são mais testáveis (mock fácil)
-遵守 Hexagonal Architecture entre módulos
