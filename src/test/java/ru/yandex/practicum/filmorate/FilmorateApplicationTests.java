package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.filmorate.adapters.LocalDateAdapter;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FilmorateApplicationTests {
	private ConfigurableApplicationContext context;
	private final static String BASE_URL = "http://localhost:8080/";
	private final static String FILMS = BASE_URL + "films";
	private final static String USERS = BASE_URL + "users";
	private final HttpClient client = HttpClient.newHttpClient();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@BeforeAll
	public static void beforeAll() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
		gson = builder.create();
	}

	@BeforeEach
	public void beforeEach() {
		context = SpringApplication.run(FilmorateApplication.class);
	}

	@Test
	public void films_emptyRequest_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void films_emptyName_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"name\": \"\",\n" +
						"  \"description\": \"description\",\n" +
						"  \"releaseDate\": \"1900-03-25\",\n" +
						"  \"duration\": 200\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void films_wrongDescription_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"name\": \"Film name\",\n" +
						"  \"description\": \"Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги," +
						" а именно 20 миллионов. о Куглов, который за время «своего отсутствия», стал кандидатом Коломбани.\",\n" +
						"  \"releaseDate\": \"1900-03-25\",\n" +
						"  \"duration\": 200\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void films_wrongReleaseDate_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"name\": \"Name\",\n" +
						"  \"description\": \"Description\",\n" +
						"  \"releaseDate\": \"1890-03-25\",\n" +
						"  \"duration\": 200\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void films_failDuration_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"name\": \"Name\",\n" +
						"  \"description\": \"Description\",\n" +
						"  \"releaseDate\": \"1980-03-25\",\n" +
						"  \"duration\": -200\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void films_updateFilm() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"name\": \"nisi eiusmod\",\n" +
						"  \"description\": \"adipisicing\",\n" +
						"  \"releaseDate\": \"1967-03-25\",\n" +
						"  \"duration\": 100\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());

		request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"id\": 1,\n" +
						"  \"name\": \"Anna\"," +
						"  \"description\": \"adipisicing\",\n" +
						"  \"releaseDate\": \"1967-03-25\"" +
						"}")).build();
		response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		String responseText = response.body();
		Film film = gson.fromJson(responseText, Film.class);
		assertEquals(1, film.getId());
		assertEquals("Anna", film.getName());
		assertEquals("adipisicing", film.getDescription());
		assertEquals(LocalDate.parse("1967-03-25", DateTimeFormatter.ISO_DATE), film.getReleaseDate());
	}

	@Test
	public void films_updateUnknownFilm() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"name\": \"nisi eiusmod\",\n" +
						"  \"description\": \"adipisicing\",\n" +
						"  \"releaseDate\": \"1967-03-25\",\n" +
						"  \"duration\": 100\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());

		request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"id\": 1,\n" +
						"  \"name\": \"Anna\"" +
						"}")).build();
		response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void films_normalRequest_shouldReturnNewFilm() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"name\": \"nisi eiusmod\",\n" +
						"  \"description\": \"adipisicing\",\n" +
						"  \"releaseDate\": \"1967-03-25\",\n" +
						"  \"duration\": 100\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		String responseText = response.body();
		Film film = gson.fromJson(responseText, Film.class);
		assertEquals(1, film.getId());
		assertEquals("nisi eiusmod", film.getName());
		assertEquals("adipisicing", film.getDescription());
		assertEquals(LocalDate.parse("1967-03-25", DateTimeFormatter.ISO_DATE), film.getReleaseDate());
		assertEquals(100, film.getDuration());
	}

	@Test
	public void films_getAll() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FILMS))
				.header("Content-type", "application/json")
				.GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
	}

	@Test
	public void users_emptyRequest_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void users_normalRequest_shouldReturnNewUser() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"dolore\",\n" +
						"  \"name\": \"Nick Name\",\n" +
						"  \"email\": \"mail@mail.ru\",\n" +
						"  \"birthday\": \"1946-08-20\"\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		String responseText = response.body();
		User user = gson.fromJson(responseText, User.class);
		assertEquals(1, user.getId());
		assertEquals("dolore", user.getLogin());
		assertEquals("Nick Name", user.getName());
		assertEquals("mail@mail.ru", user.getEmail());
		assertEquals(LocalDate.parse("1946-08-20", DateTimeFormatter.ISO_DATE), user.getBirthday());
	}

	@Test
	public void users_failLogin_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"dolore ullamco\",\n" +
						"  \"email\": \"yandex@mail.ru\",\n" +
						"  \"birthday\": \"2446-08-20\"\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void users_failEmail_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"dolore ullamco\",\n" +
						"  \"name\": \"\",\n" +
						"  \"email\": \"mail.ru\",\n" +
						"  \"birthday\": \"1980-08-20\"\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void users_failBirthday_shouldBeInternalError() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"dolore\",\n" +
						"  \"name\": \"\",\n" +
						"  \"email\": \"test@mail.ru\",\n" +
						"  \"birthday\": \"2446-08-20\"\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void users_emptyName_shouldReturnNewUser() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"common\",\n" +
						"  \"email\": \"friend@common.ru\",\n" +
						"  \"birthday\": \"2000-08-20\"\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		String responseText = response.body();
		User user = gson.fromJson(responseText, User.class);
		assertEquals(1, user.getId());
		assertEquals("common", user.getLogin());
		assertEquals("common", user.getName());
		assertEquals("friend@common.ru", user.getEmail());
		assertEquals(LocalDate.parse("2000-08-20", DateTimeFormatter.ISO_DATE), user.getBirthday());
	}

	@Test
	public void users_updateUser() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"dolore\",\n" +
						"  \"name\": \"Nick Name\",\n" +
						"  \"email\": \"mail@mail.ru\",\n" +
						"  \"birthday\": \"1946-08-20\"\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());

		request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"id\": 1,\n" +
						"  \"email\": \"mail@mail.ru\",\n" +
						"  \"birthday\": \"1946-08-20\",\n" +
						"  \"login\": \"dolore\",\n" +
						"  \"name\": \"Anna\"" +
						"}")).build();
		response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		String responseText = response.body();
		User user = gson.fromJson(responseText, User.class);
		assertEquals(1, user.getId());
		assertEquals("Anna", user.getName());
		assertEquals("mail@mail.ru", user.getEmail());
		assertEquals(LocalDate.parse("1946-08-20", DateTimeFormatter.ISO_DATE), user.getBirthday());
	}

	@Test
	public void users_updateUnknownUser() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"dolore\",\n" +
						"  \"name\": \"Nick Name\",\n" +
						"  \"email\": \"mail@mail.ru\",\n" +
						"  \"birthday\": \"1946-09-20\"\n" +
						"}")).build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());

		request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString("{\n" +
						"  \"login\": \"doloreUpdate\",\n" +
						"  \"name\": \"est adipisicinge\",\n" +
						"  \"email\": \"mail@yandex.ru\",\n" +
						"  \"id\": \"9999\",\n" +
						"  \"birthday\": \"1976-09-20\"\n" +
						"}")).build();
		response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(500, response.statusCode());
	}

	@Test
	public void users_getAll() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(USERS))
				.header("Content-type", "application/json")
				.GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
	}

	@AfterEach
	public void afterEach() {
		SpringApplication.exit(context);
	}
}
