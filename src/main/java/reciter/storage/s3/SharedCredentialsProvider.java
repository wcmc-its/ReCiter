package reciter.storage.s3;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

/**
 * One JVM-wide {@link DefaultCredentialsProvider} shared by every AWS client in the application.
 *
 * <p>Replaces the deprecated {@code DefaultCredentialsProvider.create()}, which also returned a
 * shared instance. The provider is {@code SdkAutoCloseable}; every client built with it shares its
 * single IRSA STS ({@code AssumeRoleWithWebIdentity}) connection pool. Closing any such client
 * shuts that pool down, so once the ~1h IRSA session expires every other client (including the
 * long-lived DynamoDB client) fails credential refresh with "Connection pool shut down".
 * Clients built with this provider must therefore never be closed, and the provider itself must
 * never be closed.
 */
public final class SharedCredentialsProvider {

    public static final DefaultCredentialsProvider INSTANCE = DefaultCredentialsProvider.builder().build();

    private SharedCredentialsProvider() {}
}
