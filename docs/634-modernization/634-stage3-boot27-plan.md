# ReCiter #634 — Stage 3: Spring Boot 2.5.0 → 2.7.18 + Spring Security 5.7

**Status:** executed — deps + security rewrite committed & locally green (see §0 for corrections) · **Owner:** lead author, #634 · **Worktree:** `/Users/paulalbert/worktrees/reciter-remediation`

This document is an execution plan, not research. An engineer should be able to run it top‑to‑bottom without re‑investigating. All paths are repo‑relative to the worktree root above unless absolute.

---

## 0. Execution corrections (applied during Steps 7–10)

Two claims in the plan below were wrong and were corrected when the code was actually compiled and run. The committed code reflects the corrections; the original sections are left as-written for traceability.

1. **`HttpSecurity.securityMatcher(String)` does NOT exist at Spring Security 5.7.11** — it was introduced at 5.8/6.0. `mvn compile` failed with `cannot find symbol: method securityMatcher`. The code keeps `httpSecurity.antMatcher("/reciter/**")` (the original call; `antMatcher` is not deprecated at 5.7). Consequence for §2/§3a: the stated "order is forced because `securityMatcher` needs 5.7" justification is **inaccurate** — deps-first is still the right order (the adapter must be replaced and `WebSecurityConfigurerAdapter` is deprecated, so bumping the pom first still isolates failure classes), but it is **not** literally forced by `securityMatcher`. The inner `antMatchers(...)`/`web.ignoring().antMatchers(...)` calls are unchanged and correct at 5.7.

2. **The entire `SecurityFilterChainIntegrationTest` is env-gated, not just the valid-key assertion.** With `spring.security.enabled=true`, `Application.apiKeySetter()` requires `ADMIN_API_KEY` **and** `CONSUMER_API_KEY` in the JVM env at context-load time (that is exactly why the `test` profile sets security off). Without them the context cannot load — every test in the class errors, not just the positive path (the plan's §3e under-specified this). The class now gates on both keys via `@BeforeClass Assume` and **skips cleanly** when they are absent; it runs all four assertions when present. The positive-path assertions were relaxed to "security did not reject (not 401/403)" because the downstream controllers run against mocked AWS and may return 200/400/500.

**Verified (Java 17, local), Steps 5 + 10:** dependency bump — `ApplicationContextSmokeTest` green, full suite **147/147**, `dependency:tree` single Security 5.7.11 + swagger 2.2.9. Security rewrite — full suite **148 run / 1 skipped / 0 failures** without the api-key env vars, **151 / 0 skipped / 0 failures** with them. *Not yet run through CI.*

---

## 1. Goal & Success Gate

**Goal.** Move the ReCiter Spring Boot app from `spring-boot-starter-parent` 2.5.0 to 2.7.18, which brings Spring Framework 5.3.31, **Spring Security 5.7.11**, Jackson 2.13.5, Tomcat 9.0.83, SnakeYAML 1.30, Logback 1.2.12, Hibernate Validator 6.2.5, spring‑data‑commons 2.7.18. Replace the single live `WebSecurityConfigurerAdapter` (`APISecurityConfig`) with the component‑based `SecurityFilterChain` + `WebSecurityCustomizer` model, and remove the OAuth2 5.6.0 version pins so the whole Spring Security stack resolves to one minor (5.7.11).

**Success gate (all must be true before the stage is "done"):**

1. **`ApplicationContextSmokeTest` is green** — the existing per‑stage gate. Full context boots under the `test` profile and `springdocApiDocsAreGenerated()` still renders `/v3/api-docs/reciter`. Command: `mvn test -Dtest=ApplicationContextSmokeTest`. **Scope caveat:** this test runs `@AutoConfigureMockMvc(addFilters = false)` (smoke test line 56) under `@ActiveProfiles("test")` with `spring.security.enabled=false` (`application-test.properties` line 11). With the Spring Security filters off **and** security disabled, this test exercises **neither the migrated `SecurityFilterChain` nor `MultiApiKeyFilter`**. It proves the BOM resolves and the context wires — nothing about live security behavior. Security behavior is gated separately by item 3.
2. **App boots** — `mvn clean package` succeeds and the resulting `target/reciter-2.1.3.jar` starts (locally with `--spring.security.enabled=false`, or context‑loads via the smoke test). No `BeanCurrentlyInCreationException`, no `PatternParseException` at startup.
3. **api‑key auth still works (as far as CI can prove it)** — `MultiApiKeyFilter` runs inside the chain. The migrated chain is **first exercised** by the new `SecurityFilterChainIntegrationTest` (Step 9), because the smoke test in item 1 runs with filters off and security disabled. Concretely verified by that test: ping is public, a protected `/reciter/**` path returns **401** without a key, and `/reciter/generate-access-token` is reachable without an api‑key. **Honest limit:** the positive "valid key → 200" path **cannot be exercised in CI as the filter is currently written**, because `MultiApiKeyFilter` reads `ADMIN_API_KEY`/`CONSUMER_API_KEY` from `System.getenv()` at field‑init time (lines 34, 36) and `System.getenv` cannot be overridden by Spring properties / `@DynamicPropertySource`. So this item is satisfied as: **no‑key → 401 verified; valid‑key → 200 verified only when `ADMIN_API_KEY` is present in the env** (otherwise that assertion is skipped via `Assume`). See §3e for the two options to close this gap.
4. **oauth2 resource‑server still works** — the `oauth2ResourceServer().jwt()` wiring loads, `jwtDecoder()` is injected (not self‑invoked), and a single 5.7.11 Spring Security classpath resolves. Verified by `mvn dependency:tree -Dincludes=org.springframework.security` showing **only** 5.7.11 lines — this must include `spring-security-core`, `spring-security-web`, `spring-security-config` (the three version‑less declared deps at pom lines 191‑203) **and** `spring-security-oauth2-jose` / `spring-security-oauth2-resource-server`, all reporting 5.7.11 — plus context load.

**Non‑goals (explicitly deferred):** Spring Boot 3 / Spring Security 6, the `javax → jakarta` switch, the matcher renames being *mandatory* (`antMatchers`/`authorizeRequests` still compile on 5.7 — we adopt the new lambda DSL opportunistically but it is not required), AWS SDK v1 internal version skew (1.11.925 BOM vs 1.12.742 jars), and the Caffeine 2→3 jump.

---

## 2. Ordered, Dependency‑Aware Steps

### Decision: bump dependencies FIRST, rewrite security SECOND.

**Justification.** `WebSecurityConfigurerAdapter` is only **deprecated** at Spring Security 5.7, not removed — so the parent bump compiles and runs `APISecurityConfig` as‑is. That means the pom bump is independently *buildable* and *context‑loadable* before we touch any security code, which isolates two distinct failure classes:

- A failure after the **pom bump** is a dependency/wiring/2.6‑default problem (circular refs, PathPatternParser, swagger mediation, oauth2 split‑version).
- A failure after the **security rewrite** is a SecurityFilterChain/filter‑ordering problem.

**Important scope limit on what Step 5 proves.** Step 5 runs only `ApplicationContextSmokeTest`, which (item 1 above) runs with security disabled and the Spring Security filter chain off. So Step 5 verifies **BOM resolution + context wiring only — NOT security behavior.** It will catch a split‑version classpath, a circular‑reference fatal, a PathPatternParser rejection, or a swagger/springdoc `NoSuchMethod` at context load. It will **not** catch a regression in the live security configuration (adapter or rewritten chain), because that path is never executed at Step 5. The migrated chain is first exercised by the new `SecurityFilterChainIntegrationTest` at Step 10. Do **not** treat a green Step 5 as evidence that security still works — that is Step 10's job.

If we rewrote security first against the old 2.5 BOM we'd be writing 5.7 idioms (`securityMatcher`, `WebSecurityCustomizer`) against Spring Security 5.5 — `WebSecurityCustomizer` exists in 5.4+ so it would compile, but `HttpSecurity.securityMatcher(String)` does **not** exist until 5.7, so the rewrite literally cannot compile until the BOM is bumped. The dependency bump is a hard prerequisite. Order is forced.

> Sub‑ordering note: the oauth2 pin drop (Step 2) and the swagger pin bump (Step 3) are bundled **into the same commit** as the parent bump (Step 1). The parent bump alone produces a *broken* classpath (5.6 oauth2 jars against 5.7 core, and a possible stale swagger‑models), so they are not separable verification points — verify the trio together at Step 5.

| # | Change | Files | Verify |
|---|--------|-------|--------|
| 1 | Bump `spring-boot-starter-parent` 2.5.0 → 2.7.18 | `pom.xml` (line 143) | part of Step 5 |
| 2 | Drop the two `<version>5.6.0</version>` oauth2 pins | `pom.xml` (lines 356, 361) | part of Step 5 |
| 3 | Bump springdoc 1.6.15 → 1.7.0; bump swagger‑models/core/annotations 2.2.8 → 2.2.9 (keep the pins) | `pom.xml` (lines 161/166/171, 265) | part of Step 5 |
| 4 | Delete `log4j2.version=2.16.0` property; rename `logging.file` → `logging.file.name` | `pom.xml` (line 58), `src/main/resources/application.properties` (line 5) | part of Step 5 |
| 5 | **VERIFY BOM + WIRING ONLY (not security):** `mvn clean test -Dtest=ApplicationContextSmokeTest` then `mvn dependency:tree -Dincludes=org.springframework.security,io.swagger.core.v3` | — | both green; security **all** 5.7.11 (core/web/config/oauth2‑jose/oauth2‑resource‑server), swagger all 2.2.9. **NB: security behavior is not tested here — see Step 10.** |
| 6 | Add `spring.main.allow-circular-references=false` to `application-test.properties` (documentation/parity guard so a cycle fails identically in test and prod; Boot 2.6+ already defaults it to `false` everywhere — this is not a behavior change, see §5a) | `src/test/resources/application-test.properties` | smoke test still green |
| 7 | **Rewrite `APISecurityConfig`**: drop `extends WebSecurityConfigurerAdapter`; add `SecurityFilterChain` @Bean (inject `JwtDecoder`) + `WebSecurityCustomizer` @Bean | `src/main/java/reciter/security/APISecurityConfig.java` | compiles + Step 10 |
| 8 | (Optional, recommended) delete the fully commented‑out dead file `WebSecurity.java` | `src/main/java/reciter/security/WebSecurity.java` | compiles |
| 9 | Add filter‑chain integration test (`addFilters = true`, `spring.security.enabled=true`) — the chain is otherwise untested | `src/test/java/reciter/SecurityFilterChainIntegrationTest.java` (new) | Step 10 |
| 10 | **VERIFY THE REWRITE (first real security exercise):** `mvn clean test` (full suite) + boot the jar | — | smoke + new test + 21 vintage tests green; app boots |

After Step 10, the §1 success gate is met, subject to the item‑3 CI caveat on the valid‑key path.

---

## 3. Exact Before/After Code

### 3a. `src/main/java/reciter/security/APISecurityConfig.java`

Only the import block and the two `configure(...)` methods change. The `@Value` fields, `MultiApiKeyAuthenticationFilter()` bean, `propertyScanner()` bean, and the entire `jwtDecoder()` bean are **unchanged**. The one behavioral subtlety: the old code called `jwtDecoder()` directly inside `configure()`; in the new bean we **inject** the `JwtDecoder` bean as a method parameter so Spring hands us the singleton instead of creating a second un‑proxied decoder.

**Imports — BEFORE (lines 13–16, 27):**

```java
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
...
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
```

**Imports — AFTER (drop the adapter AND the now‑unused `WebSecurity` import; add `WebSecurityCustomizer` and `SecurityFilterChain`):**

```java
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
...
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
```

> Why drop `import ...builders.WebSecurity;`: the `WebSecurityCustomizer` functional interface is `void customize(WebSecurity web)`. The lambda `(web) -> { ... }` has its parameter type **inferred** from the SAM interface, so `WebSecurity` does not need to be imported for the lambda to compile. Keeping the import is harmless on the default build (no unused‑import failure), but the import is genuinely unused and would warn under checkstyle/unused‑import enforcement. Drop it. (If you prefer to keep it, the lambda still compiles either way — but do not justify it as "needed for the lambda param.")

**Class declaration — BEFORE (line 39):**

```java
public class APISecurityConfig extends WebSecurityConfigurerAdapter {
```

**Class declaration — AFTER:**

```java
public class APISecurityConfig {
```

**The two `configure(...)` methods — BEFORE (lines 134–178):**

```java
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {

		log.info("Inside the Configure method of the APiSecurity");
		httpSecurity.antMatcher("/reciter/**")
	    .csrf().disable()
	    .sessionManagement()
	        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	    .and()
	    .exceptionHandling()
	        .authenticationEntryPoint(customEntryPoint)
	    .and()

	    // 1. Setup JWT Decoding (Fires ONLY if Bearer header is present)
	    .oauth2ResourceServer()
	        .jwt()
	        .decoder(jwtDecoder()) // Uses your Caffeine-backed bean
	    .and()
	    .and()

	    // 2. Add API Key Filter
	    .addFilterBefore(multiApiKeyFilter, UsernamePasswordAuthenticationFilter.class)

	    // 3. Asynchronous S3 Logging Filter
	    .addFilterAfter(new S3LoggingFilter(s3UserLogHandler),
	                    org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter.class)

	    .authorizeRequests()
	        // this path /reciter/generate-access-token is allowed to generate a JWT token
	    	.antMatchers(HttpMethod.POST, "/reciter/generate-access-token").permitAll()

	        // Everything else requires a valid JWT or API Key
	        .anyRequest().authenticated();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		log.info("Inside the Configure method taking WebSecurity param of the APiSecurity");
		if (!securityEnabled) {
			web.ignoring().antMatchers("/reciter/**");
		}
		// Added to whitelist ping controller and Access Token
		web.ignoring().antMatchers("/reciter/ping");

	}
}
```

**The two `configure(...)` methods — AFTER (replace lines 134–178 with the two beans below):**

```java
	@Bean
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity httpSecurity, JwtDecoder jwtDecoder) throws Exception {

		log.info("Inside apiSecurityFilterChain of the APISecurity");
		httpSecurity
		    // antMatcher("/reciter/**") -> securityMatcher (HttpSecurity-level matcher renamed at 5.7)
		    .securityMatcher("/reciter/**")
		    .csrf().disable()
		    .sessionManagement()
		        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		    .and()
		    .exceptionHandling()
		        .authenticationEntryPoint(customEntryPoint)
		    .and()

		    // 1. Setup JWT Decoding (Fires ONLY if Bearer header is present).
		    //    jwtDecoder is INJECTED (the singleton bean), not self-invoked, so we
		    //    never build a second un-proxied NimbusJwtDecoder.
		    .oauth2ResourceServer()
		        .jwt()
		        .decoder(jwtDecoder)
		    .and()
		    .and()

		    // 2. Add API Key Filter (ORDER PRESERVED: before UsernamePassword)
		    .addFilterBefore(multiApiKeyFilter, UsernamePasswordAuthenticationFilter.class)

		    // 3. Asynchronous S3 Logging Filter (ORDER PRESERVED: after Bearer token filter)
		    .addFilterAfter(new S3LoggingFilter(s3UserLogHandler),
		                    org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter.class)

		    .authorizeRequests()
		        // this path /reciter/generate-access-token is allowed to generate a JWT token
		    	.antMatchers(HttpMethod.POST, "/reciter/generate-access-token").permitAll()

		        // Everything else requires a valid JWT or API Key
		        .anyRequest().authenticated();

		return httpSecurity.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> {
			// /reciter/ping always bypassed (whitelisted ping controller)
			web.ignoring().antMatchers("/reciter/ping");
			// entire /reciter/** bypassed only when security is disabled (local/dev/test)
			if (!securityEnabled) {
				web.ignoring().antMatchers("/reciter/**");
			}
		};
	}
}
```

**Notes that are load‑bearing:**

- Keep `@EnableWebSecurity` and `@Configuration` on the class. They are required for the bean‑style configuration to be picked up.
- `antMatcher("/reciter/**")` on `HttpSecurity` is **deprecated and renamed** at 5.7 → `securityMatcher("/reciter/**")`. This is the *only* matcher that *must* change (the old `HttpSecurity.antMatcher` name is removed on the 5.7→6.0 path; `securityMatcher(String)` is the supported form, and it exists at 5.7). The inner `antMatchers(HttpMethod.POST, ...)` and `web.ignoring().antMatchers(...)` calls **still compile on 5.7** and are kept verbatim — do not churn them this stage.
- The fluent `.csrf().disable()...` chain is kept (compiles on 5.7). Migrating to the lambda DSL (`.csrf(csrf -> csrf.disable())`) is optional and explicitly **out of scope** to keep the diff reviewable.
- No `AuthenticationManager` @Bean is needed. `MultiApiKeyFilter` authenticates by calling `SecurityContextHolder.getContext().setAuthentication(...)` directly (confirmed at lines 73 and 87 of `MultiApiKeyFilter.java`) and never invokes an `AuthenticationManager`; the old adapter's `authenticationManager()` helper was never used. If a future filter needs one, expose it once via the pattern shown in §3b.

### 3b. `src/main/java/reciter/security/WebSecurity.java`

`WebSecurity.java` is **100% commented‑out dead code** (verified: zero non‑comment, non‑blank lines in the file). It declares no active bean and never compiles. It requires **no migration**.

- **Recommended action:** delete the file to remove confusion (Step 8). It is a pure no‑op for the upgrade either way.
- **If kept:** leave it exactly as is. Its commented `extends WebSecurityConfigurerAdapter` and `authenticationManager()` references are inert.

For completeness, the AUTHENTICATIONMANAGER‑WITHOUT‑ADAPTER pattern the task asks about — *if this file were ever resurrected* — is the following. This is reference only; **do not add it** this stage (nothing needs an `AuthenticationManager`):

```java
// 5.7 equivalent of the dead WebSecurity.configure(HttpSecurity) — reference only, NOT added in Stage 3
@Bean
SecurityFilterChain webFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
    http.cors().and().csrf().disable()
        .authorizeRequests()
            .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
            .anyRequest().authenticated()
        .and()
        .addFilter(new JWTAuthenticationFilter(authManager))
        .addFilter(new JWTAuthorizationFilter(authManager))
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    return http.build();
}

// AuthenticationManager exposed WITHOUT the adapter (replaces configure(AuthenticationManagerBuilder)):
@Bean
AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
    return cfg.getAuthenticationManager();
}
```

### 3c. `pom.xml` — exact edits

**Parent (lines 140–144):**

```xml
<!-- BEFORE -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.5.0</version>
</parent>

<!-- AFTER -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
</parent>
```

**OAuth2 pins (lines 353–362) — drop only the `<version>` lines:**

```xml
<!-- BEFORE -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
    <version>5.6.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
    <version>5.6.0</version>
</dependency>

<!-- AFTER (inherit 5.7.11 from the Boot 2.7.18 BOM) -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
```

> We use **option B** (keep the two explicit artifacts, drop the version pins) rather than collapsing to `spring-boot-starter-oauth2-resource-server`, to minimize the Stage‑3 diff. Both resolve to the same 5.7.11 jars.

> **No pom edit is needed for the three already‑declared, version‑less Spring Security deps** at lines 191–203 (`spring-security-config`, `spring-security-web`, `spring-security-core`). They are BOM‑managed and will float to 5.7.11 automatically with the parent bump — this is correct as‑is. They are called out here only so the Step 5 `dependency:tree` assertion explicitly confirms **all five** Spring Security artifacts (these three + the two oauth2 ones) report a single 5.7.11, fully satisfying §1 item 4.

**springdoc (lines 262–266):**

```xml
<!-- BEFORE -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.6.15</version>
</dependency>

<!-- AFTER (1.7.0 is built on Boot 2.7.10; avoid 1.8.0 which drags slf4j 2.0) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

**Swagger dependencyManagement pins (lines 158–172) — bump 2.2.8 → 2.2.9, KEEP the pins** (the Boot BOM does not manage `io.swagger.core.v3`, so dropping them re‑exposes the Stage‑2 mediation hazard). Also refresh the stale comment (current comment at lines 154–157 blames a `swagger-codegen-maven-plugin` that does **not** exist anywhere in this pom — see §7 Open Question 6):

```xml
<!-- AFTER -->
<!-- Pin the OpenAPI 3 model/core/annotation stack to the version springdoc 1.7.0
     ships with (swagger-api.version=2.2.9). The Boot 2.7.18 BOM does NOT manage
     io.swagger.core.v3, so this pin is still load-bearing: it keeps overriding any
     stale transitive swagger-models and stays aligned with springdoc 1.7.0. #634 Stage 3.
     (The prior comment cited a swagger-codegen-maven-plugin; no such plugin is in this
     pom — confirm the true conflicting source via dependency:tree, see Open Question 6.) -->
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-models</artifactId>
    <version>2.2.9</version>
</dependency>
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-core</artifactId>
    <version>2.2.9</version>
</dependency>
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-annotations</artifactId>
    <version>2.2.9</version>
</dependency>
```

**log4j2 property (line 58) — delete the override to inherit Boot 2.7.18's managed 2.17.2:**

```xml
<!-- BEFORE -->
<properties>
    <java.version>11</java.version>
    <sqlite4java.version>1.0.392</sqlite4java.version>
    <log4j2.version>2.16.0</log4j2.version>
    <lombok.version>1.18.44</lombok.version>
</properties>

<!-- AFTER (drop log4j2.version: inherit 2.17.2, closes CVE-2021-45105 / CVE-2021-44832;
     app uses Logback at runtime so this is dependency hygiene) -->
<properties>
    <java.version>11</java.version>
    <sqlite4java.version>1.0.392</sqlite4java.version>
    <lombok.version>1.18.44</lombok.version>
</properties>
```

**Keep unchanged (recorded so the reviewer doesn't "fix" them):** `lombok.version=1.18.44` (needed for Java‑17 local dev; BOM offers 1.18.30), `caffeine 2.9.3` (BOM value is identical; pin is redundant but leaving it avoids touching `CognitoClientRegistry` — optional drop, low value), `jaxb-api 2.3.0`, `nimbus-jose-jwt 9.37.4`, `java-jwt 3.19.4`, all AWS SDK v1/v2 jars, all `reciter-*-model` jars, `junit-vintage-engine` (still in the 2.7.18 BOM), and the three version‑less `spring-security-config/web/core` deps (BOM‑managed, float to 5.7.11).

### 3d. `src/main/resources/application.properties` (line 5)

```properties
# BEFORE
# Log file path (note: logging.file is deprecated in newer Spring Boot; prefer logging.file.name)
logging.file=logs/reciter.log

# AFTER
# Log file path
logging.file.name=logs/reciter.log
```

### 3e. New test — `src/test/java/reciter/SecurityFilterChainIntegrationTest.java`

The existing smoke test uses `@AutoConfigureMockMvc(addFilters = false)` (line 56) and runs under `spring.security.enabled=false`, so it does **not** exercise the migrated chain. This new test runs with `addFilters = true` **and** `spring.security.enabled=true`, mirroring the smoke test's `@MockBean` set so the context loads without AWS.

> **Profile / env caveats — read before writing this test (all verified against the source):**
>
> 1. **Security must be turned ON.** The `test` profile sets `spring.security.enabled=false` (`application-test.properties` line 11), which makes the `WebSecurityCustomizer` ignore **all** of `/reciter/**` and makes `MultiApiKeyFilter` short‑circuit (`MultiApiKeyFilter` lines 50–54). To exercise auth at all, override it on the test class with `properties = "spring.security.enabled=true"`.
> 2. **`jwtDecoder()` stays on its fail‑fast branch.** `application-test.properties` line 24 sets `aws.cognito.user-pool-id=NONE`, which forces `jwtDecoder()` down the no‑network branch that returns a decoder throwing `JwtException` on any token (`APISecurityConfig` lines 90–102). That is correct for no‑token / api‑key requests — we never send a Bearer token in this test.
> 3. **`/reciter/generate-access-token` requires a JSON body.** The controller maps it with `@PostMapping(value="/generate-access-token", consumes = MediaType.APPLICATION_JSON_VALUE)` and `@RequestBody Map<String,String>` (`CognitoAccessTokenController` lines 73–74). A POST with **no** `Content-Type` returns **415 Unsupported Media Type** — a dispatch error raised by Spring MVC *after* the security chain. Asserting only "not 401/403" against a bare POST would pass on a 415 and prove nothing about `permitAll()`. So the test **must** send `contentType(MediaType.APPLICATION_JSON)` and a minimal body (`"{}"`) so the request actually reaches the mapping. Note also that `MultiApiKeyFilter.shouldNotFilter` (line 106) already returns `true` for this exact path, so the endpoint is reachable independently of `permitAll()` — the assertion proves reachability‑without‑a‑key, not specifically that `permitAll()` did the work. State that honestly; do not over‑claim.
> 4. **The valid‑key path cannot be set via Spring properties.** `MultiApiKeyFilter` reads `ADMIN_API_KEY`/`CONSUMER_API_KEY` from `System.getenv()` at **field‑init time** (lines 34, 36). `System.getenv` is **not** overridable by Spring `@Value`/`@DynamicPropertySource`/`@SpringBootTest(properties=...)`, so the positive "valid key → 200" assertion can only run when the env var is actually set in the JVM's environment. The skeleton gates it with `Assume.assumeNotNull(adminKey)` so it **skips** when absent. **To make the positive path run deterministically in CI you must do one of:**
>    - **(a)** Set `ADMIN_API_KEY` in the CI job environment before the JVM starts (surefire inherits it), so the `Assume` passes; **or**
>    - **(b)** Refactor `MultiApiKeyFilter` to read the keys via `@Value("${admin.api.key:}")` instead of `System.getenv()`. Only then does `@DynamicPropertySource`/`@SpringBootTest(properties=...)` work, and only then can the test inject a known key without touching the OS environment. *(This is a small, contained filter change; it is the cleaner fix but it is a behavior‑adjacent edit, so flag it for review rather than slipping it in silently.)*
>    Until one of those is done, treat the §1 item‑3 valid‑key claim as "verified only when `ADMIN_API_KEY` is present."
> 5. **The 401 (not 403) outcome depends on `customEntryPoint` being wired.** `APISecurityConfig` injects it `@Autowired(required=false)` (line 44). With the `@Component` present, a no‑key anonymous request hits `.anyRequest().authenticated()` → `ExceptionTranslationFilter` invokes `CustomAuthenticationEntryPoint.commence`, which sets `SC_UNAUTHORIZED` (line 29) → **401**. If that component were ever not scanned, the entry point would be null and the default could yield 403. The assertion below uses a 401‑or‑403 tolerance so it does not silently break if entry‑point wiring changes, while still proving the request was rejected.

```java
package reciter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.s3.AmazonS3;

import reciter.consumer.service.CognitoAuthService;
import reciter.security.CognitoClientRegistry;
import reciter.service.GenderService;
import reciter.service.NameFrequencyService;
import reciter.service.ScienceMetrixDepartmentCategoryService;
import reciter.service.ScienceMetrixService;
import reciter.service.dynamo.DynamoDbInstitutionAfidService;
import reciter.service.dynamo.DynamoDbMeshTermService;

/**
 * Stage 3 (#634): exercises the migrated SecurityFilterChain + WebSecurityCustomizer
 * with the Spring Security filters ENABLED (addFilters = true) and security turned ON
 * (spring.security.enabled=true), which the Stage-0 smoke test deliberately does not do.
 *
 * Coverage limits (see plan §3e caveats):
 *  - jwtDecoder() stays on its fail-fast branch (aws.cognito.user-pool-id=NONE) — we send
 *    no Bearer token, so that is fine.
 *  - The positive "valid api-key -> 200" path runs only when ADMIN_API_KEY is set in the
 *    JVM env, because MultiApiKeyFilter reads it from System.getenv() at field init and
 *    Spring property overrides cannot reach it. validKeyAllowed() is Assume-gated to skip
 *    when the env var is absent.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.security.enabled=true")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
public class SecurityFilterChainIntegrationTest {

    @MockBean private AmazonDynamoDB amazonDynamoDB;
    @MockBean private AmazonS3 amazonS3;
    @MockBean private CognitoAuthService cognitoAuthService;
    @MockBean private CognitoClientRegistry cognitoClientRegistry;
    @MockBean private ScienceMetrixService scienceMetrixService;
    @MockBean private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService;
    @MockBean private DynamoDbMeshTermService dynamoDbMeshTermService;
    @MockBean private GenderService genderService;
    @MockBean private NameFrequencyService nameFrequencyService;
    @MockBean private DynamoDbInstitutionAfidService dynamoDbInstitutionAfidService;

    @Autowired private MockMvc mockMvc;

    /** WebSecurityCustomizer always ignores /reciter/ping -> reachable with no api-key. */
    @Test
    public void pingIsPublic() throws Exception {
        mockMvc.perform(get("/reciter/ping")).andExpect(status().isOk());
    }

    /**
     * A protected /reciter/** path with no api-key must be REJECTED (MultiApiKeyFilter is in
     * the chain but sets no authentication when the header is absent; .anyRequest().authenticated()
     * then triggers the entry point). Expect 401 via CustomAuthenticationEntryPoint, tolerating
     * 403 in case the entry-point bean is ever not wired (see §3e caveat 5).
     */
    @Test
    public void protectedPathRequiresKey() throws Exception {
        int sc = mockMvc.perform(get("/reciter/find/all/identity"))
                        .andReturn().getResponse().getStatus();
        org.junit.Assert.assertTrue("expected 401 or 403, got " + sc, sc == 401 || sc == 403);
    }

    /**
     * generate-access-token is reachable without an api-key (it is permitAll() in the chain AND
     * MultiApiKeyFilter.shouldNotFilter exempts it). Send a valid JSON content-type + minimal body
     * so the request actually reaches the @PostMapping (which requires consumes=APPLICATION_JSON);
     * otherwise a bare POST returns 415 and proves nothing. The security outcome we assert is:
     * NOT rejected by security (not 401, not 403). Downstream may 400 (missing fields) / 401-in-body
     * from the controller, but the HTTP status here will not be a security 401/403.
     */
    @Test
    public void tokenEndpointIsReachableWithoutKey() throws Exception {
        int sc = mockMvc.perform(post("/reciter/generate-access-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                        .andReturn().getResponse().getStatus();
        // Missing userName/password -> controller returns 400 (see CognitoAccessTokenController),
        // which is the expected "reached the controller, was not blocked by security" signal.
        org.junit.Assert.assertNotEquals("security must not reject this permitAll path", 401, sc);
        org.junit.Assert.assertNotEquals("security must not reject this permitAll path", 403, sc);
    }

    /**
     * Valid api-key is accepted — only runs where ADMIN_API_KEY is set in the JVM env, because
     * MultiApiKeyFilter reads it via System.getenv() at field init (not overridable by Spring).
     * Skips otherwise. To make this run deterministically in CI, set ADMIN_API_KEY in the job env
     * (option a) or refactor the filter to read keys via @Value (option b) — see §3e caveat 4.
     */
    @Test
    public void validKeyAllowed() throws Exception {
        String adminKey = System.getenv("ADMIN_API_KEY");
        Assume.assumeNotNull(adminKey);
        mockMvc.perform(get("/reciter/find/all/identity").header("api-key", adminKey))
               .andExpect(status().isOk());
    }
}
```

---

## 4. Dependency Version Table

| Artifact | Current | Target | Drop pin? | Reason |
|---|---|---|---|---|
| `org.springframework.boot:spring-boot-starter-parent` | 2.5.0 | **2.7.18** | no | Stage‑3 target; single source of the whole BOM shift. Crosses 2.6 (circular‑ref ban, PathPatternParser) and 2.7 (Security 5.7, adapter deprecation). |
| `org.springframework.security:spring-security-oauth2-jose` | 5.6.0 (pinned) | 5.7.11 (BOM) | **yes** | Mixing 5.6 oauth2 with 5.7 core/web/config is a Linkage/`NoSuchMethod` hazard in the exact JWT resource‑server path `APISecurityConfig` uses. |
| `org.springframework.security:spring-security-oauth2-resource-server` | 5.6.0 (pinned) | 5.7.11 (BOM) | **yes** | `BearerTokenAuthenticationFilter` (named in `addFilterAfter`) and the `oauth2ResourceServer` DSL must match the core minor. |
| `org.springframework.security:spring-security-config` | BOM (5.5.x via 2.5.0) | **5.7.11** (BOM) | n/a (already version‑less) | Declared with no `<version>` (pom line 191); floats with the parent bump. Confirm in Step 5 `dependency:tree`. |
| `org.springframework.security:spring-security-web` | BOM (5.5.x via 2.5.0) | **5.7.11** (BOM) | n/a (already version‑less) | Declared with no `<version>` (pom line 196); floats with the parent bump. Confirm in Step 5. |
| `org.springframework.security:spring-security-core` | BOM (5.5.x via 2.5.0) | **5.7.11** (BOM) | n/a (already version‑less) | Declared with no `<version>` (pom line 201); floats with the parent bump. Confirm in Step 5. |
| `org.springdoc:springdoc-openapi-ui` | 1.6.15 | **1.7.0** | no (not BOM‑managed) | 1.7.0 is built on Boot 2.7.10 — the correct version for the 2.7 line. Avoid 1.8.0 (pulls slf4j 2.0). |
| `io.swagger.core.v3:swagger-models` | 2.2.8 (pinned) | **2.2.9** (pinned) | no | Boot 2.7.18 BOM does NOT manage `io.swagger.core.v3`; dropping re‑exposes Stage‑2 hazard. Align to springdoc 1.7.0's `swagger-api.version=2.2.9`. |
| `io.swagger.core.v3:swagger-core` | 2.2.8 (pinned) | **2.2.9** (pinned) | no | Same — keep the pin, align to 2.2.9. |
| `io.swagger.core.v3:swagger-annotations` | 2.2.8 (pinned) | **2.2.9** (pinned) | no | Same — keep the pin, align to 2.2.9. |
| `log4j2.version` (pom property override) | 2.16.0 | **delete → 2.17.2 (BOM)** | yes | 2.16.0 is below 2.17.1/2.17.2; inheriting the managed 2.17.2 closes CVE‑2021‑45105 / CVE‑2021‑44832. App uses Logback at runtime (hygiene only). |
| `com.github.ben-manes.caffeine:caffeine` | 2.9.3 (pinned) | 2.9.3 | no (optional yes) | BOM value is identical 2.9.3; pin is redundant. Keep to avoid touching `CognitoClientRegistry`'s Caffeine‑2 builder calls; do **not** let it drift to BOM‑era 3.x. |
| `org.projectlombok:lombok` (property) | 1.18.44 | 1.18.44 | no | Needed for Java‑17 local toolchain; forward‑compatible. BOM would offer 1.18.30. |
| `javax.xml.bind:jaxb-api` | 2.3.0 (pinned) | 2.3.0 | no | API‑only, behavior‑neutral; leaving it minimizes surface. (BOM offers 2.3.1.) |
| `com.amazonaws:aws-java-sdk-bom` + v1 jars (1.12.742) | 1.11.925 / 1.12.742 | unchanged | no | Not BOM‑managed; unaffected by Boot bump. Internal v1 skew is out of scope for #634 Stage 3. |
| `software.amazon.awssdk:secretsmanager` | 2.20.34 | unchanged | no | Not BOM‑managed; unaffected. |
| `org.junit.vintage:junit-vintage-engine` | BOM (2.5.0) | BOM (2.7.18) | no | Still in the 2.7.x BOM; the 21 JUnit‑4 tests need no migration this stage (vintage is dropped only in Boot 3). |
| `edu.cornell.weill.reciter:reciter-*-model` | 2.0.x | unchanged | no | Plain POJO/Lombok/Jackson jars; not Boot‑coupled. Stale transitive deps stay overridden by the BOM. |

---

## 5. Intermediate Boot 2.6 Gotchas (and how to handle them HERE)

The parent bump silently crosses **2.6**, which changed two defaults. Neither is expected to bite this app, but both are *audited explicitly* because the smoke test boots the full context and will surface them immediately.

### 5a. Circular‑reference ban (`spring.main.allow-circular-references` default `true → false`)

- **What changed:** Boot 2.6 flips the default to `false`; any A↔B bean cycle that loaded on 2.5 now fails startup with `BeanCurrentlyInCreationException`.
- **This app's exposure:** Low. Wiring is plain `@Bean` factories + field `@Autowired`. The tests area independently characterized the security graph as **acyclic**. Watch points, all in `reciter.security`: `APISecurityConfig` `@Autowired(required=false)` `MultiApiKeyFilter` (a `@Component`) *and* declares it again as `@Bean MultiApiKeyAuthenticationFilter()`; `MultiApiKeyFilter` `@Autowired`s `CustomAuthenticationEntryPoint`; `S3UserLogHandler` is `@Async`‑adjacent (`@EnableAsync` is on `Application`, confirmed line 83) and injected into `APISecurityConfig`. None of these close a cycle.
- **Handling for THIS app:**
  1. **Step 6 records the ban explicitly in the test profile:** add `spring.main.allow-circular-references=false` to `src/test/resources/application-test.properties`. **Correct framing:** this is **not** a guard that test enjoys but prod lacks. `src/main/resources/application.properties` does **not** set this property (verified), so **production inherits the same Boot 2.6+ `false` default** — a cycle would fail identically at prod startup (`BeanCurrentlyInCreationException`) and at smoke‑test time. The explicit test‑profile line is **documentation/parity only**; it makes the default visible to a reader and pins it against future profile drift. **No separate prod guard is needed** — prod is not "silently relying on a default" in a way that differs from test; both get `false`.
  2. **If a cycle surfaces:** prefer to break it (constructor/setter ordering, or `@Lazy` on one side of the offending injection — most likely a `@Lazy` on `APISecurityConfig`'s `MultiApiKeyFilter` field). **Do NOT** re‑enable the flag as a first move.
  3. **Escape hatch (documented, last resort only):** add `spring.main.allow-circular-references=true` to `src/main/resources/application.properties` with an inline comment explaining the specific cycle it papers over. Do this only if untangling is genuinely impractical within the stage.

### 5b. Spring MVC default path matching → `PathPatternParser` (from `AntPathMatcher`)

- **What changed:** Boot 2.6 switches the MVC default to `PathPatternParser`. `PathPatternParser` rejects mappings with a mid‑pattern `**` (e.g. `/a/**/b`) or regex path variables at startup.
- **This app's exposure (VERIFIED, not assumed):** A scan of `src/main/java/reciter/controller/` for `@*Mapping` patterns found **zero** mid‑pattern `**`, zero regex path variables, and only simple paths plus trailing‑slash variants (`/reciter/identity/`, `/reciter/save/identities/`). Trailing‑slash mappings are still accepted (Boot 2.7 keeps trailing‑slash matching on by default). So **no controller change is expected**, and the `SwaggerConfig.pathsToMatch("/reciter/**")` (verified at `SwaggerConfig.java` line 25) uses a trailing `**`, which `PathPatternParser` accepts.
- **Two interplay surfaces, both handled:**
  - **springdoc:** 1.7.0 is `PathPatternParser`‑compatible; the smoke test's `springdocApiDocsAreGenerated()` (`GET /v3/api-docs/reciter`) is the exact re‑run assertion (Step 5).
  - **Spring Security matchers:** `antMatcher`/`antMatchers`/`securityMatcher` are URL matching on the **security** side, still Ant‑based in 5.7, independent of MVC's `PathPatternParser`. They keep working.
- **Handling for THIS app:** Do **nothing pre‑emptively.** Only if a concrete controller mapping is rejected at startup, add `spring.mvc.pathmatch.matching-strategy=ant-path-matcher` to `application.properties`. Given the verified scan, this fallback is not expected to be needed.

---

## 6. Risk Register + Rollback

| Risk | Likelihood | Mitigation |
|---|---|---|
| OAuth2 5.6/5.7 split‑version classpath (LinkageError / NoSuchMethodError in the JWT resource‑server path) | Medium → Low after fix | Drop both 5.6.0 pins (Step 2). **Gate:** `mvn dependency:tree -Dincludes=org.springframework.security` shows a single 5.7.11 across core/web/config/oauth2‑jose/oauth2‑resource‑server (all five). |
| SecurityFilterChain rewrite changes filter ordering or auth semantics | Medium | Preserve exact order (`addFilterBefore` UsernamePassword, `addFilterAfter` Bearer). The new `SecurityFilterChainIntegrationTest` (Step 9) is the **only** test that exercises the live chain — the smoke test runs `addFilters=false` + security off and proves nothing here. It asserts: ping public, protected‑needs‑key (401/403), token endpoint reachable‑without‑key (JSON body sent so the request reaches the mapping, not a 415). The "valid key → 200" path is Assume‑gated and only runs when `ADMIN_API_KEY` is in the env (see §3e caveat 4). |
| Bean cycle now fatal (2.6 ban) | Low | Acyclic graph (tests area); same `false` default in test and prod (§5a). Break with `@Lazy`/ordering before any flag. |
| `PathPatternParser` rejects a controller mapping | Low | Verified scan found none. Fallback: `spring.mvc.pathmatch.matching-strategy=ant-path-matcher` only if a concrete mapping fails. |
| springdoc 1.7.0 / swagger 2.2.9 NoSuchMethod against 1.6.x call sites | Low | Bump springdoc and swagger pins together (Step 3); smoke test `springdocApiDocsAreGenerated()` is the assertion. |
| `jwtDecoder()` self‑invocation → two decoder instances post‑rewrite | Low (eliminated) | Inject `JwtDecoder` as a `SecurityFilterChain` method param instead of calling `jwtDecoder()` (done in §3a AFTER). |
| Valid‑key positive auth path goes unverified in CI | Medium | `MultiApiKeyFilter` reads keys from `System.getenv()` at field init, which Spring cannot override. To actually verify "valid key → 200": (a) set `ADMIN_API_KEY` in the CI job env, or (b) refactor the filter to `@Value` the keys (§3e caveat 4). Until then the success gate (§1 item 3) is downgraded: no‑key → 401 verified; valid‑key verified only when the env var is present. |
| 21 JUnit‑4 vintage tests fail to compile/run under the bump | Low | Vintage engine stays in the 2.7.18 BOM; Step 10 runs the full `mvn clean test`. Mockito 4.5.1 / `@MockBean` / `MockMvc` are stable 5.3.7→5.3.31. |
| `MultiApiKeyFilter` double‑registration (it is `@Component` + explicit `@Bean`; an `OncePerRequestFilter` `@Component` is auto‑registered as a servlet filter too) | Low (pre‑existing, not introduced here) | Pre‑existing latent issue, **out of scope** for the strict 5.7 change. If observed, add `@Bean FilterRegistrationBean<MultiApiKeyFilter>` with `setEnabled(false)`. Flagged, not fixed, to keep the stage focused. |
| log4j2 pin removal pulls an unexpected transitive consumer | Very low | App runs Logback, no `spring-boot-starter-log4j2` declared; confirm with `mvn dependency:tree | grep log4j` after the bump. |

**Rollback note.** Every change in this stage is contained to `pom.xml`, three security/config source files, two property files, and one new test class — and lives on a dedicated branch in the `reciter-remediation` worktree. To roll back: `git revert` the Stage‑3 commit(s) (or reset the branch to the pre‑Stage‑3 SHA — *ask before any reset*; prefer `git revert`). The `pom.xml` parent line is the single switch that reverts the entire BOM. There are no DB migrations, no infra changes, and no deployment in this stage, so rollback is a pure code revert + rebuild. Because the adapter‑to‑bean rewrite (Steps 7–9) is committed *separately* from the pom bump (Steps 1–4), the security rewrite can be reverted independently while keeping the dependency upgrade, if the chain proves problematic in dev.

---

## 7. Open Questions

1. **Scope of the adapter migration this stage:** plan assumes we **do** migrate `APISecurityConfig` to `SecurityFilterChain`+`WebSecurityCustomizer` now (it is the whole point of Stage 3 and the only live adapter). The pom bump alone does *not* require it to compile/run. Confirm we are not deferring the rewrite to a later stage to shrink this diff. *(Recommendation: migrate now.)*
2. **Verifying the valid‑key path in CI (the item‑3 gap):** `MultiApiKeyFilter` reads `ADMIN_API_KEY`/`CONSUMER_API_KEY` from `System.getenv()` at field init, so Spring property overrides cannot inject a test key and `validKeyAllowed()` is `Assume`‑skipped when the env var is absent. Decide between: **(a)** inject `ADMIN_API_KEY` into the CI job environment so the positive assertion actually runs, or **(b)** refactor the filter to read the keys via `@Value("${admin.api.key:}")` (enabling `@DynamicPropertySource`/`@SpringBootTest(properties=...)` injection). Until one is chosen, the success gate is "no‑key → 401 verified; valid‑key verified only when the env var is present." *(Recommendation: (a) for this stage — zero code risk; consider (b) as a follow‑up hardening.)*
3. **CI test execution:** does the pipeline run the full surefire suite, or build the JAR with `-DskipTests` (the project guide notes `buildspec.yml` skips tests for the artifact)? If CI skips tests, the smoke + integration gates must be run manually/locally for this stage, and the 21 vintage tests' *compilation* under 2.7.18 is the only thing CI proves. Confirm.
4. **Delete dead `WebSecurity.java`?** Recommended (Step 8) but optional — zero compile/runtime impact either way (verified: zero non‑comment, non‑blank lines). Confirm preference to delete vs. leave commented.
5. **`MultiApiKeyFilter` double‑registration:** is the filter currently running twice (once in‑chain via `addFilterBefore`, once as an auto‑registered servlet filter)? Pre‑existing and out of scope, but the rewrite is the natural moment to add a `FilterRegistrationBean{setEnabled(false)}` guard. Confirm whether to fold that hardening in.
6. **Stale swagger comment cause:** the existing pom comment (lines 154–157) blames a `swagger-codegen-maven-plugin` that does **not** exist anywhere in the pom (verified — the string appears only inside that comment). The pin is still load‑bearing (BOM doesn't manage `io.swagger.core.v3`), but the stated cause is inaccurate — confirm via `mvn dependency:tree -Dincludes=io.swagger.core.v3` what actually pulls a conflicting `swagger-models`, and correct the comment accordingly (§3c already provides a corrected, hedged comment).
7. **`log4j2.version=2.16.0` intentional?** Confirm the pin was purely CVE‑response and no downstream consumer breaks on 2.17.x before deleting it to inherit the managed 2.17.2.
