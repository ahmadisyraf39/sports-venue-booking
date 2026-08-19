package com.ahmadisyraf39.sportsbooking.gateway;

import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;

/**
 * Starts one lightweight stub HTTP server per downstream service on an OS-assigned
 * ephemeral port, then drives real requests through the gateway's actual Netty HTTP
 * client via WebTestClient. Each stub echoes its own service name back, so a passing
 * assertion proves the full predicate-match -> forward-to-URI chain for that route,
 * not just that a route definition parsed.
 *
 * Route id/predicate/uri are all re-declared here via @DynamicPropertySource rather
 * than only overriding "uri". Two things force that:
 *   1. Binding stubs to the literal ports in application.yaml (8081-8085) would
 *      collide with the real sibling services a developer may already have running
 *      locally - observed firsthand while writing this test.
 *   2. Spring Boot does not merge a List<T> property (like "routes") across
 *      property sources by index/field - whichever source defines any part of the
 *      list becomes authoritative for the whole list. Overriding only "uri" here
 *      caused "predicates" and "id" to bind as empty/missing for every route,
 *      failing @NotEmpty validation on GatewayProperties. So each route must be
 *      fully re-specified in this single highest-priority source.
 *
 * That means ROUTE_SPECS below duplicates the path predicates from
 * application.yaml - if a predicate changes there, update it here too, or this
 * test will keep passing against a pattern the app no longer uses.
 *
 * A JDK HttpServer is used instead of WireMock to avoid pulling in a new test
 * dependency for such a simple stub.
 *
 * Spring Boot 4 dropped @AutoConfigureWebTestClient in favor of the
 * servlet-oriented RestTestClient, which doesn't drive a WebFlux app's real Netty
 * routing filter, so WebTestClient is built manually against the random server port.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingIntegrationTest {

    @Value("${jwt.secret}")
    private String jwtSecret;


    private record RouteSpec(String id, String pathPredicate) {
    }

    private static final List<RouteSpec> ROUTE_SPECS = List.of(
            new RouteSpec("user-service", "/api/auth/**"),
            new RouteSpec("venue-service", "/api/venues/**"),
            new RouteSpec("booking-service", "/api/bookings/**"),
            new RouteSpec("payment-service", "/api/payments/**"),
            new RouteSpec("notification-service", "/api/notifications/**")
    );

    private static List<HttpServer> stubServers;

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureRoutesAgainstStubs(DynamicPropertyRegistry registry) {
        stubServers = ROUTE_SPECS.stream()
                .map(spec -> startStub(spec.id()))
                .toList();

        for (int i = 0; i < ROUTE_SPECS.size(); i++) {
            RouteSpec spec = ROUTE_SPECS.get(i);
            int stubPort = stubServers.get(i).getAddress().getPort();
            String prefix = "spring.cloud.gateway.server.webflux.routes[" + i + "]";
            registry.add(prefix + ".id", spec::id);
            registry.add(prefix + ".uri", () -> "http://localhost:" + stubPort);
            registry.add(prefix + ".predicates[0]", () -> "Path=" + spec.pathPredicate());
        }
    }

    @AfterAll
    static void stopStubServers() {
        stubServers.forEach(server -> server.stop(0));
    }

    private static HttpServer startStub(String serviceName) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                byte[] body = serviceName.getBytes();
                exchange.getResponseHeaders().add("X-Stub-Service", serviceName);
                exchange.sendResponseHeaders(200, body.length);
                try (var responseBody = exchange.getResponseBody()) {
                    responseBody.write(body);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start stub server for " + serviceName, e);
        }
    }

    @BeforeEach
    void createWebTestClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void routesUsersPathToUserService() {
        assertRoutesTo("/api/auth/123", "user-service");
    }

    @Test
    void routesVenuesPathToVenueService() {
        assertRoutesTo("/api/venues/456", "venue-service");
    }

    @Test
    void routesBookingsPathToBookingService() {
        assertRoutesTo("/api/bookings/789", "booking-service");
    }

    @Test
    void routesPaymentsPathToPaymentService() {
        assertRoutesTo("/api/payments/1", "payment-service");
    }

    @Test
    void routesNotificationsPathToNotificationService() {
        assertRoutesTo("/api/notifications/1", "notification-service");
    }

    @Test
    void unmatchedPathIsNotRouted() {
        webTestClient.get().uri("/api/unknown/1")
                .exchange()
                .expectStatus().isNotFound();
    }

    private String generateTestToken() {
        return Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();
    }

    private void assertRoutesTo(String path, String expectedService) {
        webTestClient.get().uri(path)
                .header("Authorization", "Bearer " + generateTestToken())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Stub-Service", expectedService)
                .expectBody(String.class).isEqualTo(expectedService);
    }

    @Test
    void rejectsUnauthenticatedRequestToProtectedRoute() {
        webTestClient.get().uri("/api/venues/456")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
