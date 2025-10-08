package cbtis239.front.api;

import cbtis239.front.api.dto.RoleDto;
import cbtis239.front.api.dto.UserCreateRequest;
import cbtis239.front.api.dto.UserDto;
import cbtis239.front.api.dto.UserUpdateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class UserApiClient {
    private static final TypeReference<List<UserDto>> USER_LIST_REF = new TypeReference<>() {};
    private static final TypeReference<List<RoleDto>> ROLE_LIST_REF = new TypeReference<>() {};

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public UserApiClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build(),
                defaultBaseUrl());
    }

    public UserApiClient(HttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.mapper = new ObjectMapper().findAndRegisterModules();
    }

    private static String defaultBaseUrl() {
        return Optional.ofNullable(System.getenv("API_BASE_URL"))
                .orElseGet(() -> System.getProperty("api.base-url", "http://localhost:8080/api"));
    }

    public List<UserDto> listUsers() {
        HttpRequest request = baseRequest("/usuarios").GET().build();
        String body = send(request, 200);
        try {
            return mapper.readValue(body, USER_LIST_REF);
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "Error al parsear usuarios: " + e.getMessage());
        }
    }

    public UserDto createUser(UserCreateRequest requestBody) {
        HttpRequest request = baseRequest("/usuarios")
                .POST(HttpRequest.BodyPublishers.ofString(writeBody(requestBody), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .build();
        String body = send(request, 201);
        try {
            return mapper.readValue(body, UserDto.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "Error al parsear la respuesta de creación: " + e.getMessage());
        }
    }

    public UserDto updateUser(long id, UserUpdateRequest requestBody) {
        HttpRequest request = baseRequest("/usuarios/" + id)
                .PUT(HttpRequest.BodyPublishers.ofString(writeBody(requestBody), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .build();
        String body = send(request, 200);
        try {
            return mapper.readValue(body, UserDto.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "Error al parsear la respuesta de actualización: " + e.getMessage());
        }
    }

    public void deleteUser(long id) {
        HttpRequest request = baseRequest("/usuarios/" + id)
                .DELETE()
                .build();
        send(request, 204);
    }

    public List<RoleDto> listRoles() {
        HttpRequest request = baseRequest("/usuarios/roles").GET().build();
        String body = send(request, 200);
        try {
            return mapper.readValue(body, ROLE_LIST_REF);
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "Error al parsear roles: " + e.getMessage());
        }
    }

    private HttpRequest.Builder baseRequest(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        return HttpRequest.newBuilder(URI.create(baseUrl + normalized))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("Accept-Language", Locale.getDefault().toLanguageTag());
    }

    private String send(HttpRequest request, int expectedStatus) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != expectedStatus) {
                throw new ApiException(response.statusCode(), response.body());
            }
            return response.body();
        } catch (IOException e) {
            throw new ApiException(500, "Error de comunicación con el backend: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(500, "Solicitud interrumpida");
        }
    }

    private String writeBody(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ApiException(500, "No se pudo serializar la petición: " + e.getMessage());
        }
    }
}
