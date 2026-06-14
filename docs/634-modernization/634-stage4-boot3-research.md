# ReCiter #634 Stage 4 — Spring Boot 3 / Security 6 / Jakarta / AWS SDK v2: Research & Migration Plan

**Status:** research + plan, not executed · **Sibling of:** [`634-stage3-boot27-plan.md`](./634-stage3-boot27-plan.md) · **Baseline for all line numbers/deltas:** the post‑Stage‑3 tree on `fix/634-stage3-boot27` (PR #651), **not** `master`/`development` or the current `test/scoring-core-coverage` checkout (which is still springfox + Boot 2.5.0).

This document is research for an engineer deciding **how and when** to do the Boot 3 upgrade. It is decisive about the path and the sequencing, but it is not a line‑by‑line execution plan like Stage 3 — the executing engineer must re‑confirm a small number of items flagged here against live state at execution time (artifact versions move, EOL clocks advance).

---

## 0. Status & context (post‑Stage‑3 baseline; relationship to PR #651)

Stage 3 (PR **#651**, branch `fix/634-stage3-boot27`) is **still open / unmerged on `development`**. It moves the app `spring-boot-starter-parent` 2.5.0 → 2.7.18 and replaces the single `WebSecurityConfigurerAdapter` with the component‑based `SecurityFilterChain` + `WebSecurityCustomizer` model on Spring Security **5.7.11**. Critically, Stage 3 **deliberately did NOT** make the matcher renames mandatory: per the Stage‑3 execution corrections, `HttpSecurity.securityMatcher(String)` does not exist at 5.7.11 (it is a 5.8/6.0 API), so the committed code keeps `antMatcher("/reciter/**")` / `antMatchers(...)` / `web.ignoring().antMatchers(...)`. Those are the exact calls Stage 4 must finish converting.

**Everything in this plan is gated on #651 merging first.** The whole of Stage 4 is the parent‑version flip that Stage 3 was preparing for, and every code/pom delta below is expressed against the Stage‑3 tree:

- The verified Stage‑4 baseline pom is 442 lines: parent `2.7.18`, `springdoc-openapi-ui 1.7.0`, `javax.xml.bind:jaxb-api 2.3.0`, `DynamoDBLocal [1.12,2.0)`, `aws-java-sdk-bom 1.11.925` with `lambda/core/cognitoidp` forced to `1.12.742`, swagger‑core/models/annotations `2.2.9` pins, Lombok `1.18.44`, `maven-compiler-plugin 3.11.0`, the five `reciter-*-model` deps pinned (identity 2.0.10, article 2.0.36, dynamodb 2.0.15, scopus 2.0.3, pubmed 2.0.3). All of this only exists **after** Stages 1–3.
- Two structural facts from earlier stages that materially shrink Stage 4: **spring‑data‑dynamodb was already fully removed in Stage 1** (no `@EnableDynamoDBRepositories`, no `derjust`/`boostchicken`/`socialsignin` anywhere), and Stage 2 already swapped springfox → springdoc 1.x. So the Boot 3 critical path is **not** a Spring Data migration — it is a pure AWS SDK v1→v2 problem plus the jakarta flip.

**What this means for the executing engineer:** branch Stage 4 off the post‑#651 commit. Do not start it on `master`/`development`/`test/scoring-core-coverage`; the pom there does not match and the springdoc/security/jaxb deltas below will not apply.

---

## 1. Executive summary & recommendation

**Go path.** Do Stage 4 as **multiple decoupled sub‑stages** (sequenced in §5), not one mega‑PR. The two large bodies of work — the **javax→jakarta + Boot 3 framework flip** and the **AWS SDK v1→v2 migration** — are orthogonal and must not entangle. AWS SDK v2 runs fine on Boot 2.7 / Java 11, so the SDK work can (and should) land **first/independently**, de‑risking the framework flip.

**Target Boot/Java versions.** Land the framework flip on **Spring Boot 3.5.x (latest patch — 3.5.15 as of June 2026)** to keep Spring Framework 6.2 and avoid the Jackson 2→3 (`com.fasterxml.jackson` → `tools.jackson`) and Spring Framework 7 churn that Boot 4 forces — **but only with a committed, same‑quarter fast‑follow to Boot 4.0.x.** Boot 3.5 OSS support **ends 2026‑06‑30**, i.e. essentially the day this is written; 3.5 is a deliberate, short‑lived stepping stone, never the resting state. Adversarial verification (§8) weakened the original "3.5 as the destination" framing: as of 2026‑06‑14, 4.0.x (GA 2025‑11‑20, OSS support through 2026‑12‑31, same Java‑17 floor) is the safer **landing zone**, so the only defensible reason to touch 3.5 at all is the smaller diff — and that only holds if 4.0 is genuinely funded right behind it. **Java baseline: 11 → 17** (the hard floor for both 3.5 and 4.0). Java 21 LTS is supported and attractive while the Dockerfile is open, but **17 is the durable choice** because it survives the later 4.0 jump without a second base‑image change; pick 17 unless the team explicitly wants 21.

**The spring‑data‑dynamodb decision, in one paragraph.** There is no decision to make about *Spring Data* — it was removed in Stage 1, and no maintained spring‑data‑dynamodb fork supports Spring Data 3 / Boot 3 / jakarta anyway (derjust last released v5.1.0 on 2019‑01‑28; the boostchicken fork last released 5.2.5 on 2020‑06‑17, both still AWS‑SDK‑v1‑bound). The 15 repositories now extend a hand‑rolled `DynamoDbCrudRepository<T,ID>` over the v1 `DynamoDBMapper`, so the real decision is purely AWS‑SDK: **migrate to the AWS SDK v2 DynamoDB Enhanced Client** (`software.amazon.awssdk:dynamodb-enhanced`), rejecting fork‑and‑patch (dead upstream) and Spring Cloud AWS `DynamoDbTemplate` (a thin layer over the same Enhanced Client that adds an `io.awspring` BOM without removing any hard work). This is the single critical path and the dominant cost driver, mostly because the entity POJOs live in the **external `reciter-dynamodb-model` jar** and must be re‑annotated and re‑released in lockstep.

**Overall effort / risk verdict.** **Large overall, but lopsided.** The Boot 3 framework flip itself is small for ReCiter (≈20 javax import sites across ~11 files, application.properties already clean of renamed keys, security/springdoc groundwork done in Stages 2–3) — call it ~1–2 days for the jakarta+Boot‑3 envelope plus ~3–5 days to GA‑green including DynamoDB Local 2.x/3.x re‑validation and trailing‑slash/springdoc fixups. The **DynamoDB v1→v2 migration is the elephant**: roughly **3–5 developer‑weeks**, dominated by the cross‑repo entity re‑annotation and behavior‑preserving regression tests of the schema‑migration/skip‑null and on‑the‑wire item‑shape paths. The non‑Dynamo AWS services (S3/STS/Cognito/Lambda; SQS is dead code to delete) add ~1.5–3 days. Highest‑severity risks are all in the DynamoDB dimension: cross‑repo lockstep, `@DynamoDBDocument`/`@DynamoDBTyped` having no clean v2 analogue (silent item‑shape corruption), and `UPDATE_SKIP_NULL_ATTRIBUTES` data‑loss if `save→putItem` is mis‑ported.

---

## 2. Verified baseline (scan inventory)

The numbers below are from a scan of the Stage‑4 research worktree (`docs/634-stage4-boot3-research` / `fix/634-stage3-boot27` baseline). The "javax surface" and "model jars are javax‑free" claims were independently re‑verified by adversarial check #3 (constant‑pool decode of all 175 model `.class` files = 0 javax/jakarta refs).

### 2a. AWS SDK v1 usage (`com.amazonaws`) — 32 source files / 136 import sites (+ 8 test files)

| Service (v1 package) | Import sites | Where (key seams) | v2 target | Difficulty |
|---|---|---|---|---|
| DynamoDB (`dynamodbv2.*`) | ~72 (datamodeling 33, model 28, client 6, util 2) | `DynamoDbCrudRepository` (15 repos), `DynamoDbConfig`, `SchemaMigrationAspect`, 4 low‑level service impls | `DynamoDbEnhancedClient` + low‑level `DynamoDbClient` | **Large** (no DynamoDBMapper analogue; see §3) |
| S3 (`s3` + `s3.model`) | ~29 (model 19, client 10) | `AmazonS3Config`, `DynamoDbS3Operations`, `S3UserLogHandler`, 2 scorer call‑sites, 2 test mocks | `S3Client` | **Moderate** (no `doesObjectExist`/`doesBucketExistV2`; `ObjectMetadata` removed) |
| Cognito (`cognitoidp` + model) | 9 | `CognitoAuthService`, `CognitoClientRegistry` | `CognitoIdentityProviderClient` (pkg `cognitoidentityprovider`) | Low (mechanical builder rewrite + package rename) |
| Auth/credentials (`auth`) | 10 | shared across the above | `DefaultCredentialsProvider.create()` / `StaticCredentialsProvider` | Low (mechanical) |
| STS (`securitytoken` + model) | 4 | `AmazonS3Config.getAccountIDUsingAccessKey` (1 method) | `StsClient` | Trivial (1 method) |
| Lambda (`lambda` + model) | 4 | `NeuralNetworkModelArticlesScorer.callAwsLambda` (1 method) | `LambdaClient` (`SdkBytes` payload) | Low (1 method) |
| SQS (`sqs`) | 2 | `AmazonSQSExtendedClientConfig` — **entirely block‑commented** | — | **Negative** (delete dead code + deps) |
| regions / client.builder | 2 | misc | `Region.of(...)` | Mechanical |
| **AWS SDK v2 already present** | (secretsmanager 2.20.34) | — | bump to shared v2 BOM | — |

POM version skew to fix first: `aws-java-sdk-bom` is pinned `1.11.925` while `lambda/core/cognitoidp` are forced `1.12.742` and `DynamoDBLocal` floats `[1.12,2.0)`. Raise the v1 BOM to a single coherent final line before peeling off any service (see §3/§8 for the exact version).

### 2b. javax → jakarta surface — 13 source files / 25 import lines (0 test files)

| javax package | Sites | Files | Action |
|---|---|---|---|
| `javax.servlet.*` | 14 | `Application`, `MultiApiKeyFilter` (4), `S3LoggingFilter` (4), `CustomAuthenticationEntryPoint` (2), `APIKeyAuthFilter`, `ApiKeyRequestMatcher`, `BearerTokenRequestMatcher` | **→ jakarta.servlet** |
| `javax.validation.constraints` (`NotEmpty`/`NotNull`/`Positive`) | 3 | `StrategyParameters` (24–26) | **→ jakarta.validation** |
| `javax.annotation.PostConstruct` | 2 | `CognitoClientRegistry:14`, `S3UserLogHandler:9` | **→ jakarta.annotation** |
| `javax.inject.Inject` | 1 | `AmazonSQSExtendedClientConfig:5` (live import; class body is dead‑commented) | **→ delete the import** (simplest; the only user is commented out) |
| `javax.crypto` (`Mac`, `SecretKeySpec`) | 2 | `CognitoAuthService:9-10` | **STAYS javax** (JDK security) |
| `javax.xml.parsers` (`SAXParserFactory`, `ParserConfigurationException`) | 2 | `PubmedESearchHandler:27-28` | **STAYS javax** (JDK SAX — **NOT** JAXB; corrects the original baseline's "javax.xml = JAXB" note) |

**MUST flip = 20 sites across 11 files; MUST stay = 4 sites across 2 files; hand‑review surface = 13 files total.** There is **no** `javax.persistence`, `javax.ws.rs`, `javax.websocket`, `javax.ejb`, `javax.transaction`, or `javax.xml.bind`/JAXB anywhere in source.

### 2c. Dependencies needing Boot‑3 attention

| Dependency (baseline) | Action | Note |
|---|---|---|
| `spring-boot-starter-parent 2.7.18` | → `3.5.x` (then 4.0.x) | brings Spring Security 6.5.x, Spring Framework 6.2, Jackson 2.19 |
| `java.version 11` | → `17` | hard floor for Boot 3/4 |
| `Dockerfile FROM amazoncorretto:11-alpine` | → `17-alpine` | musl/native caveat in §6 |
| `k8-buildspec.yml runtime java: corretto11` | → `corretto17` | `buildspec.yml` is already DEPRECATED (openjdk11) — ignore/delete |
| `springdoc-openapi-ui 1.7.0` | → `springdoc-openapi-starter-webmvc-ui 2.8.x` (2.8.17) | **different artifactId**, not a version bump |
| swagger‑core/models/annotations `2.2.9` pins | drop (springdoc 2.8 manages its own io.swagger.v3) | re‑verify swagger‑codegen mediation (§6) |
| `javax.xml.bind:jaxb-api 2.3.0` | **drop** (unused by source) | replace with `jakarta.xml.bind-api 4.x` only if a runtime probe needs it |
| `DynamoDBLocal [1.12,2.0)` (com.amazonaws) | → jakarta‑clean 2.x/3.x (see §3/§8 for exact GAV) | 1.x throws `ClassNotFoundException: javax.servlet.ServletInputStream` on Boot 3 |
| `junit-vintage-engine` + 22 JUnit‑4 tests | **keep** | vintage runs unchanged on Boot 3.5; JUnit 5 migration is optional/deferred |
| five `reciter-*-model` jars | **no change** | verified javax/jakarta‑free; no re‑release needed for the jakarta flip |
| Lombok `1.18.44`, New Relic agent | **no change** | both Java‑17‑clean already |

---

## 3. The spring‑data‑dynamodb decision (critical path)

This is the load‑bearing decision of Stage 4. The framing "spring‑data‑dynamodb blocker" is **obsolete**: Stage 1 already removed it. What remains is an AWS SDK v1 `DynamoDBMapper` → v2 Enhanced Client migration, made expensive by a cross‑repo entity‑model coupling.

### 3a. Options

| Path | What it is | Trade‑offs | Verdict |
|---|---|---|---|
| **(a) AWS SDK v2 Enhanced Client** (`software.amazon.awssdk:dynamodb-enhanced`) | Rewrite `DynamoDbCrudRepository` + `SchemaMigrationAspect` onto `DynamoDbEnhancedClient`/`DynamoDbTable`/`TableSchema`; migrate 4 low‑level impls onto v2 `DynamoDbClient`; re‑annotate entities `@DynamoDbBean`/`@DynamoDbPartitionKey` | Officially the successor to `DynamoDBMapper`; lowest‑risk for this many mapper sites; works on Boot 2.7/Java 11 so it can land before the framework flip. Cost: cross‑repo entity re‑annotation; `count()`, `@DynamoDBDocument`, `@DynamoDBTyped`, `FailedBatch`, `UPDATE_SKIP_NULL_ATTRIBUTES` need behavior‑preserving reimplementation | **RECOMMENDED** |
| (b) Fork + patch a spring‑data‑dynamodb fork to Boot 3 | Port a community fork to Spring Data 3 / jakarta | Dead upstream (6 years stale), still v1‑bound — you'd solely maintain a Spring Data 3 port of an abandoned lib **and still own the v1→v2 problem underneath** | **REJECT** (non‑viable) |
| (c) Spring Cloud AWS 3.x `DynamoDbTemplate` (`io.awspring.cloud`) | High‑level template built **on top of** the v2 Enhanced Client | Boot‑3‑native and real, but it wraps exactly the Enhanced Client you'll wire by hand; adds an `io.awspring` BOM + auto‑config and does **not** remove the hard work (entity re‑annotation, `count()`, `@DynamoDBDocument`) | **REJECT as the primary path**; keep only as an optional ergonomics wrapper *after* the core migration |
| (d) Raw `DynamoDbClient` DAOs everywhere | Hand‑built `AttributeValue` maps for all 15 repos | Unnecessary verbosity for the 15 mapper‑backed repos | Use **only** for the 4 low‑level impls (which are already low‑level) |

### 3b. Recommended path & why (Path a)

Migrate to the v2 Enhanced Client. Concrete plan:

1. **Raise the v1 BOM** off `1.11.925` to the actual last v1 release (see §8 correction — it is **1.12.797**, not "1.12.788"), so remaining v1 services sit on one coherent line during the transition.
2. **Add the v2 BOM** `software.amazon.awssdk:bom` (current GA **2.46.x** — §8 corrects an earlier "2.31.x" to **2.46.x**) as a real `dependencyManagement` import; align the existing `secretsmanager` (2.20.34) to it and pull `dynamodb` + `dynamodb-enhanced`.
3. **Re‑annotate the external `reciter-dynamodb-model` entities** from `@DynamoDBTable`/`@DynamoDBHashKey`/`@DynamoDBAttribute`/`@DynamoDBRangeKey` to `@DynamoDbBean`/`@DynamoDbPartitionKey`/`@DynamoDbAttribute`/`@DynamoDbSortKey`; re‑model the **5** `@DynamoDBDocument` nested types as nested `@DynamoDbBean`, the **6** `@DynamoDBTyped` uses as custom `AttributeConverter`s; cut a new model release (e.g. `3.0.0`) and bump the pom dep in lockstep. **Per §8, the entity surface is ~13 `@DynamoDBTable` classes, not the "24 POJOs" of an earlier draft** — and the keys present are 13 `@DynamoDBHashKey`, **0** `@DynamoDBRangeKey`, 2 `@DynamoDBIndexHashKey` (→ `@DynamoDbSecondaryPartitionKey`), 1 `@DynamoDBAutoGeneratedKey` (no drop‑in v2 annotation — handle explicitly). The re‑annotation effort is meaningfully smaller than the original estimate, but the index‑hash‑key and auto‑generated‑key handling were missing from it and must be added.
4. **Rewrite `DynamoDbCrudRepository`**: `save`→`table.putItem`/`updateItem`; `load`→`getItem(Key)`; `scan`→`table.scan().items()` (PageIterable, stream lazily); `batchSave`→`batchWriteItem(WriteBatch.addPutItem)`; `batchLoad`→`batchGetItem(ReadBatch.addGetItem)`; `batchDelete`→`batchWriteItem(WriteBatch.addDeleteItem)`. **No `count()` in v2** — use `scan(ScanEnhancedRequest.builder().select(Select.COUNT).build())` and **sum `page.count()` across all pages**. **No `FailedBatch`** — inspect `unprocessedPutItemsForTable`/`unprocessedDeleteItemsForTable`, retry with backoff, throw if non‑empty (preserve the fail‑loud contract).
5. **Redesign `SchemaMigrationAspect`**: `DynamoDBMapperConfig.withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)` has no per‑mapper config in v2 — express it **per‑request** via `table.updateItem(UpdateItemEnhancedRequest.builder().ignoreNulls(true)...)`. The around‑advice's save‑interception contract changes; this is a redesign, not a line‑for‑line port. **Porting `save→putItem` here would clobber existing attributes — data loss.**
6. **Migrate the 4 low‑level impls** (`ArticleProvenanceServiceImpl`, `PmidProvenanceServiceImpl`, `FeedbackLogServiceImpl`, `FeedbackLogQueryService`) to v2 `DynamoDbClient` with the builder `AttributeValue` model and `software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException`; replace `ExpectedAttributeValue` with `Expression`/`conditionExpression`.
7. **Rework `DynamoDbConfig`** reflective table‑creation (it scans `@DynamoDBTable` via `ClassPathScanningCandidateComponentProvider` + `AnnotationTypeFilter`) to scan `@DynamoDbBean` and build v2 `CreateTableRequest`/`table.createTable`; bump **DynamoDB Local** to a jakarta‑clean line and re‑validate the embedded `ServerRunner`/`DynamoDBProxyServer` harness + sqlite4java natives.

**Sequencing:** do this v2 DynamoDB migration as its **own PR before or alongside** the jakarta/Boot‑3 flip. v2 works on Boot 2.7/Java 11, so it can land first and de‑risk the framework step. AWS's official OpenRewrite v1→v2 tool **does not** transform `DynamoDBMapper` — there is no automation for the core rewrite, only for the low‑level client/import churn.

---

## 4. Migration dimensions

### 4a. Boot 3 core (2.7.18 → 3.5.x, then 4.0.x)

- **Scope.** Parent + Java floor + the handful of Boot‑3 behavioral changes that bite ReCiter specifically.
- **Deltas.** Parent `2.7.18`→`3.5.x`; `java.version 11`→`17`. Add `spring-boot-properties-migrator` (runtime, test‑only) temporarily to surface property renames at startup, then remove it — ReCiter's `application.properties` contains **none** of the known renamed keys (`server.max-http-header-size`, `spring.redis.*`, `spring.data.cassandra.*`, `management.metrics.export.*`, `spring.resources.*`), so it should be quiet. Run the upgrade via OpenRewrite `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_5` (rewrite‑maven‑plugin 6.41.0 + rewrite‑spring 6.32.1) for the mechanical javax→jakarta + dependency bumps; from a 2.7 baseline the recipe traverses the 3.0 boundary and runs the Jakarta EE 9 migration (`JavaxMigrationToJakarta`), which by design preserves `javax.crypto` and `javax.xml.parsers`. Do the springdoc artifactId swap and DynamoDB Local change **by hand**.
- **Two behavioral traps (runtime, not compile):**
  - **Trailing‑slash 404s.** Spring 6 switched MVC default path matching to `PathPatternParser` with trailing‑slash matching defaulting to **false**. The **6** controller mappings ending in `/` (`/reciter/save/scopus/articles/`, `/reciter/find/scopus/articles/pmids/`, `/reciter/retrieve/articles/`, `/reciter/identity/`, `/reciter/save/identities/`, `/reciter/find/identity/by/uids/`) will 404 silently. Fix with a `UrlHandlerFilter` (Spring 6.2+, in Boot 3.5) `trimTrailingSlash` redirect (preferred), or dual `@RequestMapping` values, or the deprecated `setUseTrailingSlashMatch(true)`.
  - **DynamoDB Local incompatibility** — see §4f / §3 (the single most likely runtime regression in the core flip).
- **Risks.** Landing on a line whose OSS patches stop 2026‑06‑30 (**high** — mitigate by treating 3.5 as transient and funding the 4.0 follow‑on); Security 6.5 DSL deltas (**medium** — §4c); OpenRewrite over‑reach on the hand‑rolled DynamoDB/AWS/security code (**medium** — review the full diff, revert anything it touches in those files).
- **Effort.** Medium. The flip itself is mechanical; the time sinks are DynamoDB Local re‑validation and trailing‑slash/springdoc fixups (~0.5–1 day each with verification). ~3–5 engineer‑days for 3.5 GA‑green, plus a separate 4.0 follow‑on stage.

### 4b. Java 11 → 17 runtime / Docker

- **Scope.** `pom.xml` `java.version`, `Dockerfile`, `k8-buildspec.yml`, plus the deprecated `buildspec.yml`.
- **Deltas.** `java.version 11`→`17`; `Dockerfile FROM amazoncorretto:11-alpine`→`amazoncorretto:17-alpine`; `k8-buildspec.yml runtime-versions java: corretto11`→`corretto17`; delete/ignore the DEPRECATED `buildspec.yml` (openjdk11, references a stale `reciter-2.1.0.jar`). `maven-compiler-plugin 3.11.0` and Lombok `1.18.44` already support 17/21; New Relic agent supports Java 17 since v7.4.0 and the Dockerfile's `/current` download stays valid. Image‑tag/ECR/`kubectl set image` steps are JDK‑version‑agnostic.
- **Risk (low).** `amazoncorretto:17-alpine` uses musl libc; native libs (DynamoDB Local natives, any JNI) that worked under glibc can fail to load. DynamoDB Local is **test‑scope only** (not in the runtime image) and runs on the CodeBuild image, so the Alpine concern is confined there; New Relic ships a pure‑Java agent. Smoke‑test the built 17‑alpine image with `/reciter/ping` and confirm the agent attaches in a dev deploy before prod.
- **Pick 17, not 21**, so the same base image survives the later 4.0 jump (both floor at 17). Choose 21 only if the team explicitly wants the current LTS and confirms EKS node images + sqlite4java natives support it.
- **Effort.** Low (mechanical), ~0.5 day excluding verification.

### 4c. Spring Security 5.7 → 6 (Boot 3.5 brings Security 6.5.x)

- **Scope.** One file, `APISecurityConfig.java` (Stage 3 already did the structurally hard `WebSecurityConfigurerAdapter` → `SecurityFilterChain` + `WebSecurityCustomizer` work and produced a 6.x‑clean `JwtDecoder` bean). What remains is mechanical DSL modernization + one new bean.
- **Deltas (all in `APISecurityConfig.java` unless noted):**
  - `antMatcher("/reciter/**")` → `securityMatcher("/reciter/**")`.
  - Replace the whole fluent `.csrf().disable().sessionManagement()...and()...authorizeRequests().antMatchers(...)` chain with the **mandatory lambda DSL** (`authorizeRequests()`, `antMatchers()`, `.and()`‑chaining, and the no‑arg `oauth2ResourceServer()/jwt()` builders are **removed**, not just deprecated, in Security 6.0): `http.securityMatcher("/reciter/**").csrf(c -> c.disable()).sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS)).exceptionHandling(...).oauth2ResourceServer(o -> o.jwt(j -> j.decoder(jwtDecoder)))...authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, "/reciter/generate-access-token").permitAll().anyRequest().authenticated())`.
  - `web.ignoring().antMatchers(...)` → `web.ignoring().requestMatchers(...)` in `webSecurityCustomizer()`.
  - **NEW: close the #639 swagger gap.** The `/reciter/**`‑scoped chain does not cover `/v3/api-docs/**` and `/swagger-ui/**` (springdoc serves those at the servlet root), so under Boot 3 they are matched by **no** filter chain and served unauthenticated. Add a **second, higher‑`@Order(0)` `SecurityFilterChain`** that `securityMatcher`s the springdoc paths and explicitly `permitAll()` (or `denyAll()` if docs must be locked down); give the `/reciter/**` chain `@Order(1)`. Prefer this over `web.ignoring()` (discouraged in 6.x).
- **Safe by construction:** ReCiter runs a single `DispatcherServlet` at root (no `server.servlet.context-path`, SQS commented out), so String‑based `securityMatcher`/`requestMatchers` is safe — the CVE‑2023‑34035 ambiguity error only fires with >1 registered servlet. No explicit `MvcRequestMatcher`/`AntPathRequestMatcher` wrapping needed. `@Configuration` must stay on `APISecurityConfig` (Security 6.0 `@EnableWebSecurity` no longer meta‑includes it) — it already is.
- **Risks.** `MultiApiKeyFilter` path‑branching (`getServletPath()`/`getRequestURI()` → admin vs consumer key) under Tomcat 10 servlet‑path semantics is **high** (runtime‑test each branch; prefer `getRequestURI()` consistently; validate against the nginx sidecar prefix). New swagger chain ordering wrong → either swagger still open or `/reciter/**` breaks (**medium** — explicit `@Order` + assertions). The #639 end state (docs world‑readable vs locked) is an **open product question** (§7).
- **Effort.** Low‑to‑moderate (~0.5–1 day incl. integration tests). Real effort is runtime verification of `MultiApiKeyFilter` under Tomcat 10, not writing the config. `confidence: medium` on "prefer a second chain over `web.ignoring()`" being the canonical fix — it is the right call, but pin the exact extra springdoc paths (`/v3/api-docs.yaml`, `/swagger-ui/index.html`) at execution time.

### 4d. javax → jakarta

- **Scope.** §2b: 20 sites flip, 4 stay, across 13 files. Self‑contained source+pom edit; **no cross‑repo model release** (all five `reciter-*-model` jars verified javax/jakarta‑free).
- **Deltas.** Exact rewrites in §2b. Drop unused `jaxb-api 2.3.0`; `spring-boot-starter-validation`/`-websocket` stay (parent‑managed; their bundled `jakarta.validation-api 3.0.x`/`jakarta.websocket-api 2.1.x` auto‑flip when the parent moves). Boot 3.5 manages `jakarta.xml.bind-api 4.0.x` + `glassfish jaxb-runtime 4.0.x` if needed.
- **Risks.** Dropping `jaxb-api` could break a transitive runtime JAXB consumer (some AWS SDK v1 components historically needed JAXB) — **low/medium**, surfaces only at runtime. Mitigation: boot a full context + run the integration smoke test and exercise the AWS‑touching paths (S3, Cognito, Lambda invoke, STS `getCallerIdentity`) after removal; if a `NoClassDefFoundError` for a jakarta/javax JAXB class appears, add `jakarta.xml.bind-api 4.x` + `jaxb-runtime 4.x` — do **not** pre‑add. OpenRewrite over‑rewriting a JDK javax import — **low** (hand‑verify `CognitoAuthService` and `PubmedESearchHandler` still read javax; the 13‑file surface is eyeball‑able).
- **Effort.** Low in isolation (~1 hour with OpenRewrite + review); medium‑high for the inseparable Boot‑2.7→3.5 envelope it rides inside.

### 4e. AWS SDK v1 → v2 (non‑Dynamo services)

- **Scope.** S3, STS, Cognito, Lambda (SQS is dead code to delete). All have GA v2 equivalents; fully decoupled from the DynamoDB migration.
- **Deltas.** Add BOM‑managed `s3`, `sts`, `cognitoidentityprovider`, `lambda`, `apache-client`; **remove** `aws-java-sdk-sqs`, `amazon-sqs-java-extended-client-lib`, `aws-java-sdk-sts`, `aws-java-sdk-cognitoidp`, `aws-java-sdk-lambda`, `aws-java-sdk-core`, and the v1 BOM override (after DynamoDB v1 is also gone). Per‑service:
  - **S3 (highest friction).** No `doesObjectExist`/`doesBucketExistV2` — reimplement with `headObject`/`headBucket` in try/catch on `NoSuchKeyException`/`NoSuchBucketException` (treat `S3Exception.statusCode()==404` as not‑found). `ObjectMetadata` removed — content‑type/length move onto `PutObjectRequest.builder()` + `RequestBody.fromInputStream(is,len)`/`fromBytes`. `getObject` → `ResponseInputStream<GetObjectResponse>`; last‑modified via `headObject(...).lastModified()` (`Instant`, not `Date`). Affected: `S3UserLogHandler` (×2, incl. a `getStatusCode()==404` special‑case → `statusCode()==404`), `DynamoDbS3Operations`, `AmazonS3Config.createBucket`.
  - **STS.** One method → `StsClient...getCallerIdentity(GetCallerIdentityRequest.builder().build()).account()`.
  - **Cognito.** Package is `cognitoidentityprovider` (**not** `cognitoidp`); `AdminInitiateAuthRequest.builder()...authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)`, `response.authenticationResult().idToken()`, `ListUserPoolClientsRequest.builder()...`.
  - **Lambda.** `InvokeRequest.builder().payload(SdkBytes.fromUtf8String(json))`; read `InvokeResponse.payload().asUtf8String()` (**not** `.array()`), check `statusCode()` (int) + `functionError()`.
  - **SQS.** Delete `AmazonSQSExtendedClientConfig.java` + its deps; no migration.
  - **Credentials/region.** `DefaultAWSCredentialsProviderChain` → `DefaultCredentialsProvider.create()`; `withRegion(String)` → `.region(Region.of(String))`. Guard or omit `.region()` where `AWS_REGION` may be unset (avoid `Region.of(null)` NPE).
- **Process.** Run the AWS v2‑migration OpenRewrite recipe (`software.amazon.awssdk.v2migration.AwsSdkJavaV1ToV2`) in dryRun first (automates ~60–80% of client/request/response/exception churn), then hand‑fix the `doesObjectExist`/`doesBucketExistV2` stubs and the Lambda payload semantics; exclude the DynamoDB pieces.
- **Risks.** S3 existence checks (**high** — write `objectExists`/`bucketExists` helpers + tests for the 404 path); v2 secretsmanager version skew (**medium** — align `2.20.34` to the shared 2.46.x BOM); Lambda `payload().asUtf8String()` vs blind `.array()` (**medium**); Cognito package‑name trap and dead‑SQS deps lingering (**low**).
- **Effort.** Moderate, ~1.5–3 dev‑days combined. S3 moderate, STS trivial, Cognito/Lambda low, SQS negative. **Java/Boot‑independent:** v2 2.46.x supports Java 8+, so this can land on Boot 2.7/Java 11 before the framework flip.

### 4f. springdoc 1→2 & build / CI / infra

- **Scope.** springdoc artifactId swap, swagger pins, DynamoDB Local, CI runtime, trailing‑slash, jaxb drop.
- **Deltas.** `springdoc-openapi-ui 1.7.0` → `springdoc-openapi-starter-webmvc-ui 2.8.x` (group `org.springdoc` unchanged); **only source change** is `SwaggerConfig.java` `import org.springdoc.core.GroupedOpenApi` → `org.springdoc.core.models.GroupedOpenApi` (the `io.swagger.v3.oas.models.*` imports are unchanged). Drop the swagger‑core/models/annotations `2.2.9` pins. **DynamoDB Local:** replace `com.amazonaws:DynamoDBLocal [1.12,2.0)`, remove the `dynamodb-local-oregon` S3 `<repository>` (now on Maven Central), and **delete the 5 explicit sqlite4java `<dependency>` blocks + the `maven-dependency-plugin` `native-libs` copy execution** (2.x/3.x bundle their own natives). `k8-buildspec.yml` `corretto11`→`corretto17`. Keep `junit-vintage-engine`; note `@MockBean` is deprecated in Boot 3.4+ (`@MockitoBean`) — only relevant at 3.4+/4.0.
- **Risks.** Dropping the swagger pins could resurface `NoSuchMethodError Schema.getExampleSetFlag` if `swagger-codegen-maven-plugin` still drags in old swagger‑models (**medium** — re‑run `mvn dependency:tree`; keep a Boot‑3‑aligned pin if codegen remains). DynamoDB Local swap is entangled with the AWS‑SDK dimension (**medium** — do it **as part of**, not ahead of, the v2 dynamo migration; keep v1 Local + sqlite4java intact until then).
- **Confidence flag.** The **exact DynamoDB Local GAV is the one place where research and verification disagree** — resolve it at execution time, see §8 check #2. The intent is unambiguous (drop the version range, remove the Oregon repo, re‑validate `ServerRunner`/natives); the coordinates are not.
- **Effort.** Medium for the slice (~1–2 days of mechanical edits); real time is regression verification, and the DynamoDB Local swap is gated on the larger AWS‑SDK work.

### 4g. Internal `reciter-*` model deps

- **Scope.** Two distinct couplings, with **opposite** answers.
- **jakarta flip: NO change.** All five jars (identity 2.0.10, article 2.0.36, dynamodb 2.0.15, scopus 2.0.3, pubmed 2.0.3) are bytecode‑clean of javax **and** jakarta (verified by constant‑pool decode of 175 classes in §8 check #3). The originally‑suspected "models need jakarta‑baseline releases" blocker is **disproven** — their pinned versions stay.
- **DynamoDB v2: hard cross‑repo gate.** The `@DynamoDBTable`/`@DynamoDBHashKey` entity POJOs live **only** in `reciter-dynamodb-model 2.0.15`; v2 needs `@DynamoDbBean`/`@DynamoDbPartitionKey`. That jar must be migrated and re‑released (e.g. `3.0.0`) **in lockstep** with the ReCiter consumer (§3 step 3). This is the biggest cost driver and a release‑coordination dependency, not a code‑in‑this‑repo problem.
- **Risk (high, DynamoDB only).** If the model jar is owned/released separately, the work serializes. Mitigation: treat the model‑jar v2 migration as the **first deliverable**; publish a `3.0.0-SNAPSHOT` internally so ReCiter can compile/integration‑test before the final release; migrate model + consumer in one coordinated branch/PR pair.
- **Effort.** ~3–5 days for the model jar (incl. `@DynamoDBDocument`/`@DynamoDBTyped`/round‑trip fixtures); 0 for the jakarta flip.

---

## 5. Proposed sub‑stage sequencing

All of Stage 4 is gated on **#651 (Stage 3) merging**. Nothing below should land before it; every delta is expressed against the post‑#651 tree.

**Within‑2.7 quick win (can land NOW, before #651 even, since it is a no‑op rename):** the `jaxb-api 2.3.0 → 2.3.1` bump is the classic "within‑2.7 quick win," **but it is moot for Boot 3** — the dep is unused by source, so the correct action is to **drop `jaxb-api` entirely** rather than bump it. Do not spend a PR on `2.3.1`. (If a pre‑Boot‑3 hygiene PR is desired, dropping the unused dep is the move.)

**What can land before #651 merges:** the **AWS SDK v1→v2 migration is independent of Stage 3** and of Boot 3 (v2 2.46.x runs on Boot 2.5/2.7 + Java 11). In principle Stages 4a/4b could even branch off `development` rather than waiting for #651. **Recommended discipline:** still rebase them onto the post‑#651 tree to keep the #634 stack linear and avoid a three‑way merge with the security rewrite. The jakarta/Boot‑3 sub‑stages (4c onward) **cannot** start before #651 — they depend on the Stage‑3 `SecurityFilterChain`/pom baseline.

| Sub‑stage | Content | Boot/Java | Hard gates / notes |
|---|---|---|---|
| **4a — DynamoDB v1→v2** | Model jar re‑annotation + release; `DynamoDbCrudRepository` + `SchemaMigrationAspect` rewrite; 4 low‑level impls; `DynamoDbConfig` table‑creation; DynamoDB Local 2.x/3.x | Boot 2.7 / Java 11 | **Gate:** `reciter-dynamodb-model 3.0.0` must be published (or `-SNAPSHOT` internal) before ReCiter compiles. Largest effort. Land FIRST to de‑risk. Round‑trip on‑wire item‑shape tests are mandatory. |
| **4b — non‑Dynamo AWS v2** | S3/STS/Cognito/Lambda → v2; delete dead SQS; consolidate v2 BOM (align secretsmanager) | Boot 2.7 / Java 11 | Can land in parallel with 4a behind the shared v2 BOM. **Final removal of the v1 BOM only after 4a lands** (DynamoDB keeps v1 on the classpath until then). |
| **4c — jakarta + Boot 3.5 + Security 6.5** | javax→jakarta (20 sites); parent 2.7.18→3.5.x; Java 11→17; Dockerfile/k8‑buildspec; springdoc artifactId swap; finish `securityMatcher`/`requestMatchers`; new swagger `SecurityFilterChain` (#639); drop jaxb‑api; trailing‑slash filter | **Boot 3.5 / Java 17** | **Gate:** #651 merged. **Gate:** 4a/4b's DynamoDB Local + AWS v2 ideally done so Boot 3 doesn't run old v1/DynamoDB‑Local‑1.x under Java 17. Won't compile without the Security DSL rename — self‑enforcing. |
| **4d — Boot 4.0 fast‑follow** | parent 3.5.x→4.0.x; Jackson 2→3 (`com.fasterxml`→`tools.jackson`); Spring Framework 7 / Servlet 6.1 deltas; springdoc 2.8→3.0; OpenRewrite `boot4/UpgradeSpringBoot_4_0` | **Boot 4.0 / Java 17** | **Gate:** 4c green; springdoc 3.x GA; AWS SDK v2 already done (4a/4b). No base‑image churn (4.0 floors at 17). **Must be funded same quarter** — 3.5 OSS support ends 2026‑06‑30. |

**Why this order.** AWS SDK work first because it is Boot/Java‑independent, the single biggest risk pool (DynamoDB), and de‑couples cleanly. jakarta+Boot‑3 second because it is small once the SDK churn is out of the way and the Security 6 rename won't even compile otherwise. Boot 4 last because it adds the Jackson 2→3 package move on top — doing 3.5 first de‑risks it (official guidance: go to latest 3.5.x before 4.0). The one judgment call (§7): a team with appetite for a bigger single hop could collapse 4c+4d and go straight 2.7→4.0, accepting the Jackson migration in one PR to avoid a throwaway 3.5 landing.

---

## 6. Risk register (severity‑ranked)

| Risk | Severity | Mitigation |
|---|---|---|
| **Cross‑repo lockstep:** v2 entity annotations live in external `reciter-dynamodb-model`; ReCiter can't compile against the Enhanced Client until a new model jar is cut | **High** | Make the model‑jar v2 migration the first deliverable; publish `3.0.0-SNAPSHOT` internally; migrate model + consumer in one coordinated branch/PR pair |
| **`@DynamoDBDocument` (5) / `@DynamoDBTyped` (6) have no v2 annotation** — naive re‑annotation silently changes on‑the‑wire item shape, corrupting reads of existing prod items | **High** | Re‑model as nested `@DynamoDbBean` + custom `AttributeConverter` reproducing exact v1 serialization; round‑trip tests against DynamoDB Local that read v1‑written fixtures and assert byte‑for‑byte attribute equivalence before prod |
| **`UPDATE_SKIP_NULL_ATTRIBUTES` mis‑port** — `save→putItem` instead of `updateItem(ignoreNulls=true)` clobbers existing attributes (data loss in the schema‑migration path) | **High** | Map the skip‑null path explicitly to `updateItem(...ignoreNulls(true))`; regression test that updates a partially‑populated item and asserts unrelated attributes survive |
| **Landing on Boot 3.5 (OSS‑EOL 2026‑06‑30)** — unsupported framework on day one; CVE blind spot | **High** | Treat 3.5 as a transient stepping stone; fund the Boot 4.0 follow‑on (4d) in the same quarter; or skip straight to 4.0 |
| **`MultiApiKeyFilter` path‑branching under Tomcat 10** — `getServletPath()`/`getRequestURI()` semantics change can silently break admin‑vs‑consumer key selection | **High** | Integration‑test each branch (consumer key for `/reciter/article-retrieval/*` and `/reciter/dev/*`, admin for other `/reciter/*`, skip for `/generate-access-token`); prefer `getRequestURI()`; validate against the nginx sidecar prefix, not just `java -jar` |
| **S3 `doesObjectExist`/`doesBucketExistV2` removed** — naive port/OpenRewrite stub fails to compile or changes 404 semantics | **High** | `objectExists`/`bucketExists` helpers using `headObject`/`headBucket` + `NoSuchKey`/`NoSuchBucket` catch (and `statusCode()==404`); unit‑test `S3UserLogHandler`'s 404 path |
| **Trailing‑slash 404s** — the 6 slash endpoints silently 404 for Publication Manager / scripts / integrations | **Medium** | `UrlHandlerFilter` trimTrailingSlash redirect; integration test asserting 200 for both `/x` and `/x/`; verify the nginx sidecar doesn't strip/add slashes in a conflicting way |
| **DynamoDB Local 2.x/3.x harness re‑wiring** — `ServerRunner`/`DynamoDBProxyServer` + native‑lib packaging differ from 1.x; breaks local dev / embedded‑DB tests | **Medium** | Validate the new harness end‑to‑end on Java 17 before merge; confirm whether it backs CI or is dev‑only; run repository + smoke tests against the new embedded instance; do the swap **with** the v2 dynamo migration |
| **`FailedBatch` has no v2 analogue** — literal port ignoring unprocessed items silently drops writes | **Medium** | Inspect `unprocessed*ItemsForTable`, retry with backoff, throw if non‑empty after retries |
| **No `count()` / `PaginatedScanList` in v2** — non‑lazy rewrite could truncate at one page or change memory profile | **Medium** | `scan().items()` PageIterable streaming for `findAll`/`deleteAll`; `select(Select.COUNT)` summing **all** pages for `count()`; multi‑page tests |
| **Security 6 DSL deltas** (removed `authorizeRequests`/`antMatchers`/`.and()`/no‑arg oauth2 builders) + new swagger chain ordering | **Medium** | Lambda‑DSL rewrite per §4c; explicit `@Order(0)`/`@Order(1)`; re‑run `SecurityFilterChainIntegrationTest`; the OpenRewrite Security 6.5 sub‑recipe handles most of it |
| **Dropping swagger `2.2.9` pins** resurfaces `NoSuchMethodError Schema.getExampleSetFlag` via swagger‑codegen | **Medium** | `mvn dependency:tree` after the springdoc 2.x bump; keep a Boot‑3‑aligned pin if codegen still drags an old line |
| **v2 SDK version skew** (secretsmanager 2.20.34 vs new 2.46.x on one classpath) | **Medium** | Single shared `software.amazon.awssdk:bom 2.46.x` `dependencyManagement` import for all v2 artifacts incl. secretsmanager |
| **Lambda payload semantics** — v1 `getPayload().array()` (ByteBuffer) vs v2 `payload()` (`SdkBytes`) | **Medium** | Use `payload().asUtf8String()`; check `statusCode()` (int) + `functionError()` |
| **Un‑reviewable mega‑PR** if jakarta+Boot‑3 entangles with the 136‑site AWS v1→v2 churn | **Medium** | Sub‑stage per §5; AWS SDK first on Boot 2.7, then jakarta+Boot‑3 |
| **Dropping `jaxb-api 2.3.0`** breaks a transitive runtime JAXB consumer (e.g. AWS SDK v1) — runtime‑only | **Low** | Boot a full context + exercise S3/Cognito/Lambda/STS paths after removal; add `jakarta.xml.bind-api 4.x` + `jaxb-runtime 4.x` only if a `NoClassDefFoundError` appears |
| **`amazoncorretto:17-alpine` musl/native** lib load failures | **Low** | DynamoDB Local is test‑scope (CodeBuild image, not the alpine runtime); New Relic is pure‑Java; smoke‑test `/reciter/ping` + agent attach in dev |
| **String `securityMatcher` matcher auto‑selection** degrades to AntPath if MVC not detected | **Low** | Single‑servlet app makes it deterministic; add a test hitting `/reciter/feature-generator/by/uid` without a key expecting 401 |
| **OpenRewrite over‑reach** on hand‑rolled DynamoDB/AWS/security code | **Low/Medium** | Run on a branch, review the full diff, revert anything it touches in those files; use it for mechanical churn only |
| **`web.ignoring().requestMatchers("/reciter/**")` when `!securityEnabled`** opens everything if the flag is ever true in a deployed env | **Low** | Preserve the `!securityEnabled` guard exactly; confirm `spring.security.enabled=true` ships and K8s never overrides it |
| **Cognito package rename** (`cognitoidp`→`cognitoidentityprovider`) trips manual edits | **Low** | Use the exact v2 artifactId/package; rely on the recipe's `ChangeType`; verify with `mvn compile` |

---

## 7. Open questions (need a human / cross‑repo decision)

1. **Stage‑4 ceiling: 3.5 then 4.0, or straight to 4.0?** As of 2026‑06‑14, 3.5 OSS support ends in days. Recommend 3.5 first (smaller blast radius) **only if** 4.0 (4d) is funded same quarter; otherwise go straight to 4.0 and accept the Jackson 2→3 migration in one stage. Orchestrator/team call.
2. **Java 17 vs 21.** Recommend 17 (survives the 4.0 jump without re‑touching the base image). Confirm EKS node images + sqlite4java/native‑lib constraints if 21 is preferred.
3. **`reciter-dynamodb-model` ownership & release cadence.** The v2 entity re‑annotation must ship there first. Same team/pipeline as ReCiter, or external coordination? This determines whether 4a serializes.
4. **On‑wire item‑shape consumers.** Do ReciterDB ETL, Publication‑Manager, or the scoring Lambda also deserialize these DynamoDB entities? If so the v1→v2 item shape must stay byte‑compatible and those consumers need coordinated validation.
5. **#639 swagger end state.** Should the new swagger `SecurityFilterChain` `permitAll()` (docs world‑readable) or `denyAll()`/require the admin key? #639 only says they must stop being silently unauthenticated. Check `Projects/ReCiter/` for the ticket text.
6. **nginx sidecar path behavior.** Does the port‑80→5000 sidecar rewrite/strip a prefix before requests hit Spring? It affects both the `MultiApiKeyFilter` servletPath the filter sees post‑Boot‑3 and whether it masks/compounds the trailing‑slash 404 fix. Validate against the deployed proxy, not a local run.
7. **Is the embedded DynamoDB Local harness in CI or dev‑only?** Determines whether the 2.x/3.x re‑validation gates CI green or is a manual smoke check.
8. **Is the SQS large‑message feature genuinely abandoned?** If someone intends to revive `AmazonSQSExtendedClientConfig`, plan for `amazon-sqs-java-extended-client-lib 2.1.x` (v2) instead of deleting.
9. **Adopt Spring Cloud AWS `DynamoDbTemplate` as a post‑migration ergonomics layer, or keep the hand‑rolled repo on the raw Enhanced Client?** Trade‑off: less hand‑rolled code vs. an extra `io.awspring` BOM/auto‑config.
10. **Pin the New Relic agent?** The Dockerfile pulls `/current` (non‑deterministic). Consider pinning ≥ 7.4.0 at the same time — independent of the Boot 3 flip.
11. **Delete the deprecated `buildspec.yml` / `.ebextensions`?** They reference openjdk11 and a stale jar. Cleanup vs. keeping the Stage‑4 diff focused.

---

## 8. Verification notes (adversarial checks)

Four research claims were adversarially re‑checked. Verdicts and corrections are folded into the body above; the corrections are listed here for traceability. **Where verification weakened a claim, the body uses the verified position.**

### Check 1 — spring‑data‑dynamodb decision (Path a, reject b/c, raw DAOs only for the 4 impls) → **partially confirmed**

Core thesis confirmed: no Boot‑3/Spring‑Data‑3 spring‑data‑dynamodb release exists (derjust v5.1.0, 2019‑01‑28; boostchicken 5.2.5, 2020‑06‑17 — both v1‑bound); the Enhanced Client is the documented successor; the codebase structural claims (15 repos, `SchemaMigrationAspect` UPDATE_SKIP_NULL save interception, `DynamoDbConfig` `@DynamoDBTable` table‑creation scan, 4 low‑level impls, `@DynamoDBDocument`×5, `@DynamoDBTyped`×6) are exact. **Three factual corrections, all applied above:**
- **Entity count was overstated** — "24 entity POJOs" → actually **~13 `@DynamoDBTable` classes** (13 `@DynamoDBHashKey`, **0** `@DynamoDBRangeKey`, 2 `@DynamoDBIndexHashKey` → add `@DynamoDbSecondaryPartitionKey`, 1 `@DynamoDBAutoGeneratedKey` → handle explicitly). Re‑annotation is smaller than estimated but the index/auto‑gen handling was missing.
- **v1 BOM "1.12.788 is the final line" is wrong** → last v1 release is **1.12.797** (2025‑12‑29).
- **v2 BOM "2.31.x" is stale** → use **2.46.x** (2.46.7, 2026‑06‑09).
- *Unverified sub‑claim (flagged, not refuted):* "DynamoDB Local 1.x EOS Jan 2025" — the Boot‑3 `javax.servlet.ServletInputStream` incompatibility is confirmed; the specific date is plausible but uncorroborated. Don't cite it as fact.
- Sources: derjust/boostchicken release pages + Maven Central; aws‑sdk‑java‑v2 issue #3605; AWS Enhanced‑Client + mapping‑changes docs; Spring Cloud AWS `DynamoDbTemplate` apidocs; the ReCiter repo + `../ReCiter-Dynamodb-Model/src`.

### Check 2 — Boot 3 target version & Java baseline (3.5.x as immediate landing, then 4.0; Java 11→17/21; deltas A–I) → **partially confirmed**

Facts almost all correct; the **strategic framing is the weak part**. The EOL dates are exact (Boot 3.5 released 2025‑05‑22, OSS support ends 2026‑06‑30; Boot 3.4 EOL 2025‑12‑31; Boot 4.0 GA 2025‑11‑20, OSS through 2026‑12‑31, Java‑17 floor). All code/pom premises match the Stage‑3 baseline exactly (parent 2.7.18, springdoc 1.7.0, jaxb 2.3.0, DynamoDBLocal `[1.12,2.0)`, BOM 1.11.925, swagger 2.2.9 pins, Lombok 1.18.44, compiler 3.11.0). Deltas A–D, F–I verified accurate. **Corrections applied above:**
- **Strategy weakened:** as of 2026‑06‑14, recommending 3.5 as the "immediate landing" lands on a line whose OSS support expires in ~16 days. **4.0.x is the safer landing zone**; 3.5 is defensible only as a short‑lived stepping stone with the 4.0 follow‑on funded same quarter. The body reflects this.
- **Delta (E) factual error:** "software.amazon.dynamodb:DynamoDBLocal 2.x (e.g. 2.6.1)" is internally inconsistent — **2.6.1 is published under `com.amazonaws`**, and the `software.amazon.dynamodb` groupId rename is the **v3.x** change. Use **either** `com.amazonaws:DynamoDBLocal:2.6.1` (2.x, drop the Oregon repo) **or** `software.amazon.dynamodb:DynamoDBLocal:3.x` — not the stated pairing. This is the §4f confidence flag; resolve the exact GAV at execution time.
- **Staleness:** if picking 3.5, pin the latest **3.5.15**, not "3.5.11"; Boot 4.1.0 is now GA.
- Sources: spring.io support policy; `docs.spring.io/spring-boot/3.5/system-requirements`; OpenRewrite `boot3`/`boot4` recipe pages; Sonatype Central for DynamoDBLocal; local Stage‑3 `SwaggerConfig.java`/`APISecurityConfig.java`/pom/Dockerfile.

### Check 3 — internal model jars + jakarta tooling/POM deltas → **confirmed**

The strongest result. All five model jars (`javap -verbose` over 175 classes) carry **0** javax/jakarta refs and declare no javax/jakarta deps — **no cross‑repo model re‑release is needed for the jakarta flip** (the originally‑suspected gating risk is refuted). The 20‑flip/4‑stay split, every named file, the pom line refs, and the deferred `APISecurityConfig` 5.8/6.0 follow‑up comment all match the Stage‑4 baseline. OpenRewrite versions (rewrite‑maven‑plugin 6.41.0, rewrite‑spring 6.32.1) and the `UpgradeSpringBoot_3_5`/`JavaxMigrationToJakarta` mechanics are real; springdoc 2.8.17 is the current 2.8.x. **Minor flags applied above:**
- The 20 rewrite sites touch **11** files, not "9" (header undercount); the "13 files" hand‑review figure (11 flipped + 2 leave‑alone) is correct.
- `AmazonSQSExtendedClientConfig`: the **class body** is block‑commented but the `import javax.inject.Inject;` line is **live outside** the comment — deleting the import (as recommended) is the clean fix; don't expect OpenRewrite's auto‑flip to leave a working state without handling the orphan import.
- **Baseline‑mismatch warning:** executing on `test/scoring-core-coverage` will fail — that tree is still springfox 3.0.0 / Boot 2.5.0. Stage 4 must branch off the post‑#651 commit.
- Boot 4.0 also requires **Spring Framework 7.x / Servlet 6.1** (not just "Java 17+, Jakarta EE 11").
- Sources: local model jars in `~/.m2`; spring.io support policy; `endoflife.date/spring-boot`; Boot 4.0 migration guide; OpenRewrite recipe pages; springdoc releases.

### Check 4 — "AWS SDK v1 EOS Dec 2025 makes v1→v2 operationally urgent" → **partially confirmed**

The **date is exactly right** (v1 entered maintenance 2024‑07‑31, End‑of‑Support **2025‑12‑31**, confirmed by the AWS blog, Discussion #3078, and the repo README). The **"operationally urgent" framing is overstated and is corrected above:** End‑of‑Support does **not** mean the SDK stops working — AWS states existing v1 apps "continue to function as intended," and artifacts stay on Maven Central. There is **no Dec‑31‑2025 outage cliff**. The genuine, compounding risk is the **cessation of security patches/critical bug fixes** (an unpatched CVE in `aws-java-sdk-core` would have no upstream fix). Correct characterization: **an elevated, scheduled security/tech‑debt priority that grows over time, not a drop‑everything hotfix.** A phased, service‑by‑service migration (as sequenced in §5) is appropriate; treating it as a serial hotfix ahead of functional/availability work would be a misprioritization absent a concrete forcing function (an exploitable core CVE, or an AWS service making a breaking protocol change v1 can't handle).
- Sources: AWS "end‑of‑support" blog; aws‑sdk‑java Discussion #3078 / issue #3195 / README; AWS SDKs & Tools Maintenance Policy.
