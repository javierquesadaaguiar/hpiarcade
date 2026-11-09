package es.ulpgc.hpi.arcadeservice.infrastructure;

import es.ulpgc.hpi.arcadeservice.architecture.ports.PlayerRepositoryImpl;
import es.ulpgc.hpi.arcadeservice.architecture.usecases.PlayerUseCaseImpl;
import es.ulpgc.hpi.arcadeservice.infrastructure.web.PlayerHttpAdapter;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpApiTest {
    private final HttpClient client = HttpClient.newHttpClient();
    private Javalin server;
    private String base;

    @BeforeEach
    void start() {
        server = PlayerHttpAdapter.create(new PlayerUseCaseImpl(new PlayerRepositoryImpl()));
        server.start(0);
        base = "http://localhost:" + server.port();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private void player(String nickname) throws Exception {
        assertEquals(201, post("/api/players", "{\"nickname\":\"" + nickname + "\"}").statusCode());
    }

    @Test
    void duplicateNicknameIsRejectedWith400() throws Exception {
        player("Ada");
        assertEquals(400, post("/api/players", "{\"nickname\":\"ada\"}").statusCode());
    }

    @Test
    void scoresEndpoint() throws Exception {
        player("Ada");
        var ok = post("/api/players/1/scores", "{\"points\":10}");
        assertEquals(200, ok.statusCode());
        assertTrue(ok.body().contains("\"score\":10"));
        assertEquals(404, post("/api/players/99/scores", "{\"points\":10}").statusCode());
        assertEquals(400, post("/api/players/1/scores", "{\"points\":0}").statusCode());
        assertEquals(400, post("/api/players/1/scores", "{\"points\":-5}").statusCode());
        assertEquals(400, post("/api/players/abc/scores", "{\"points\":5}").statusCode());
    }

    @Test
    void leaderboardEndpoint() throws Exception {
        player("Ada");
        player("Bea");
        post("/api/players/2/scores", "{\"points\":7}");
        var all = get("/api/leaderboard");
        assertEquals(200, all.statusCode());
        assertTrue(all.body().indexOf("Bea") < all.body().indexOf("Ada"));
        var limited = get("/api/leaderboard?limit=1");
        assertEquals(200, limited.statusCode());
        assertTrue(limited.body().contains("Bea") && !limited.body().contains("Ada"));
        assertEquals(400, get("/api/leaderboard?limit=0").statusCode());
        assertEquals(400, get("/api/leaderboard?limit=-2").statusCode());
        assertEquals(400, get("/api/leaderboard?limit=abc").statusCode());
    }
}
