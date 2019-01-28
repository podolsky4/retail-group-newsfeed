package com.impltech.service;

import com.impltech.Main;
import com.impltech.exception.TokenExpiredException;
import com.impltech.model.Article;
import com.impltech.model.Category;
import com.impltech.model.CommentOutcome;
import com.impltech.model.Credentials;
import com.impltech.shared.Template;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * REST client fetches resources from specified endpoints.
 *
 * <p>Created: 20.09.18</p>
 * <p>Released: 07.11.18</p>
 *
 * @author Yuri Podolsky
 * @version 1.1
 */
public class RestClient {

  private static final Logger logger = Logger.getLogger(RestClient.class.getName());

  // REST API
  public static final String HOST_URL = "http://srv-news-00.welcash.kiev.ua:3000";
//  public static final String HOST_URL = "http://localhost:3000";
  private static final String ARTICLES = "/articles";
  private static final String ARTICLE = "/article";
  private static final String CATEGORIES = "/categories";
  private static final String CATEGORY = "/category";
  private static final String TEMPLATES = "/templates";
  private static final String CONFIRMATION = "/confirmation/true";
  private static final String COMMENT = "/comment";
  private static final String LIKE = "/like";
  private static final String LOGIN = "/login";

  // Instance of the rest client (i.e Singleton)
  private static RestClient instance;

  // Logged user instance
  private JSONObject loggedUser;
  // Jersey client
  private final Client client;

  private RestClient() {
    client = ClientBuilder.newClient();
  }
  // Get single instance of rest client
  public static RestClient getInstance() {
    if (instance == null) {
      instance = new RestClient();
    }
    return instance;
  }

  // Get json array of any resources
  private JsonArray getJsonArray(String url) throws JSONException, TokenExpiredException {
    Response response = client.target(HOST_URL)
            .path(url)
            .queryParam("username", loggedUser.getString("login"))
            .queryParam("password", loggedUser.getString("token"))
            .request(MediaType.APPLICATION_JSON)
            .get();

    if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
      throw new TokenExpiredException("User token is expired");
    } else if (response.getStatus() != Response.Status.OK.getStatusCode()) {
      throw new RuntimeException("Failed : HTTP error code : "
              + response.getStatus());
    }

      String resp = response.readEntity(String.class);

      JsonReader jsonReader = Json.createReader(new StringReader(resp));
      return jsonReader.readArray();

  }

  /**
   * Fetch template from server.
   *
   * @return
   * Instance of template from the list
   */
  public Template fetchTemplate() throws TokenExpiredException {

    JsonArray jsonArray = null;
    try {
      jsonArray = getJsonArray(TEMPLATES);
      logger.info("Fetching list of templates: " + jsonArray);
    } catch (JSONException e) {
      logger.warning("Cannot fetch template: " + e.getMessage());
    }

    return Objects.requireNonNull(jsonArray).stream().map(Template::new).findFirst().get();
  }

  /**
   * Fetch list of categories.
   *
   * @return Instance of category from the list
   */
  public List<Category> fetchCategories() throws TokenExpiredException {

    JsonArray jsonArray = null;
    try {
      jsonArray = getJsonArray(CATEGORIES);
      logger.info("Fetching list of categories: " + jsonArray);
    } catch (JSONException e) {
      logger.warning("Cannot fetch categories: " + e.getMessage());
    }

    return Objects.requireNonNull(jsonArray)
            .stream()
            .map(Category::new)
            .collect(toList());
  }

  /**
   * Send commentOutcome for specified article.
   *
   * @param commentOutcome
   * CommentOutcome entity
   * @param articleId
   * Article ID
   */
  public void sendComment(CommentOutcome commentOutcome, String articleId) throws JSONException {
    Response response = client.target(HOST_URL)
            .path(COMMENT)
            .path("{articleId}")
            .resolveTemplate("articleId", articleId)
            .path("{userId}")
            .resolveTemplate("userId", loggedUser.getString("_id"))
            .queryParam("username", loggedUser.getString("login"))
            .queryParam("password", loggedUser.getString("token"))
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(commentOutcome));

    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
      throw new RuntimeException("Failed : HTTP error code : "
              + response.getStatus());
    }
  }

  /**
   * Set like to the article from particular user.
   *
   * @param articleId
   * Article id
   *
   * @param entity
   * Empty entity
   *
   * @return
   * If request succeeded then return {true}, {false} in other way
   */
  public JsonObject likeArticle(final String articleId, Entity<?> entity) throws JSONException {

    Response response = client.target(HOST_URL)
            .path(ARTICLE)
            .path(LIKE)
            .path("/{articleId}")
            .resolveTemplate("articleId", articleId)
            .queryParam("username", loggedUser.getString("login"))
            .queryParam("password", loggedUser.getString("token"))
            .request(MediaType.APPLICATION_JSON)
            .put(entity);

    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
      throw new RuntimeException("Failed : HTTP error code : "
              + response.getStatus());
    }
    String resp = response.readEntity(String.class);
    JsonReader reader = Json.createReader(new StringReader(resp));
    return reader.readObject();
  }

  /**
   * Authorize new user or return logged one and save to {@link RestClient#loggedUser}.
   *
   * @param credentials
   * User credentials (username and password).
   * @see Credentials
   *
   * @return
   * HTTP Status
   *
   * @throws IOException
   * May throws {@link IOException}
   */
  public int authorize(Credentials credentials) throws IOException, JSONException {
    Response response = client.target(HOST_URL)
            .path(LOGIN)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

    int responseStatus = response.getStatus();
    if (responseStatus == Response.Status.OK.getStatusCode()) {
      String user = response.readEntity(String.class);
      loggedUser = new JSONObject(user);
      Main.getSocket().emit("login", loggedUser);
      logger.info("Socket id: " + Main.getSocket().id() + " emitting event LOGIN");
      serializeUser();
    }
    return responseStatus;
  }

  // Login emit event
  public void emit(Socket socket) {
    socket.emit("login", user());
    logger.info("Socket id: " + Main.getSocket().id() + " emitting event LOGIN");
  }

  /**
   * Serialize user object in file.
   *
   * @throws IOException
   * May throws {@link IOException} if writing error occurs
   */
  private void serializeUser() throws IOException {
    try (FileWriter file = new FileWriter("user.json")) {
      file.write(loggedUser.toString());
      logger.info("Successfully Copied JSON Object to File...");
      logger.info("JSON Object: " + user());
    }
  }

  /**
   * Deserialize user object from file.
   *
   * @throws IOException
   * May throws {@link IOException}, if specified file do not exist
   */
  public void deserializeUser() throws IOException, JSONException {
    File file = new File("user.json");
    if (file.exists()) {
      try(FileReader fr = new FileReader(file)) {
        Scanner scanner = new Scanner(fr);
        String jsonString = scanner.nextLine();
        loggedUser = new JSONObject(jsonString);
      }
      logger.info("Deserialized JSON Object from File...");
      logger.info("JSON Object: " + user());
    }
  }

  public JSONObject user() {
    return loggedUser;
  }

  /**
   * Get list of articles
   *
   * @return Observable list of articles
   */
  public Collection<Article> getArticleList(int pageIndex) {
    JsonArray jsonArray = null;
    try {
      Response response = client.target(HOST_URL)
              .path(ARTICLES)
              .path(CONFIRMATION)
              .path("{page}")
              .resolveTemplate("page", pageIndex)
              .queryParam("username", loggedUser.getString("login"))
              .queryParam("password", loggedUser.getString("token"))
              .request(MediaType.APPLICATION_JSON)
              .get();

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        throw new RuntimeException("Failed : HTTP error code : "
                + response.getStatus());
      }

      String resp = response.readEntity(String.class);

      JsonReader jsonReader = Json.createReader(new StringReader(resp));
      jsonArray = jsonReader.readArray();
    } catch (JSONException e) {
      logger.severe("Cannot get articles list: " + e.getMessage());
    }

    return Objects.requireNonNull(jsonArray)
            .stream()
            .map(Article::new)
            .collect(toList());
  }

    public Article fetchArticle(String articleId) {
        JsonObject jsonObject = null;
        try {
            Response response = client.target(HOST_URL)
                    .path(ARTICLE)
                    .path("{id}")
                    .path("{confirmation}")
                    .resolveTemplate("id", articleId)
                    .resolveTemplate("confirmation", true)
                    .queryParam("username", loggedUser.getString("login"))
                    .queryParam("password", loggedUser.getString("token"))
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            String resp = response.readEntity(String.class);

            JsonReader jsonReader = Json.createReader(new StringReader(resp));
            jsonObject = jsonReader.readObject();
        } catch (JSONException e) {
            logger.severe("Cannot get article by id: " + e.getMessage());
        }

        return new Article(jsonObject);
    }

  public Collection<Article> getArticlesByCategory(String categoryId, int pageIndex) {

    JsonArray jsonArray = null;
    try {
      Response response = client.target(HOST_URL)
              .path(ARTICLES)
              .path(CATEGORY)
              .path("{category}")
              .resolveTemplate("category", categoryId)
              .path("/true")
              .path("{page}")
              .resolveTemplate("page", pageIndex)
              .queryParam("username", loggedUser.getString("login"))
              .queryParam("password", loggedUser.getString("token"))
              .request(MediaType.APPLICATION_JSON)
              .get();

      if (response.getStatus() != Response.Status.OK.getStatusCode()) {
        throw new RuntimeException("Failed : HTTP error code : "
                + response.getStatus());
      }

      String resp = response.readEntity(String.class);

      JsonReader jsonReader = Json.createReader(new StringReader(resp));
      jsonArray = jsonReader.readArray();
    } catch (JSONException e) {
      logger.severe("Cannot get articles list: " + e.getMessage());
    }

    return Objects.requireNonNull(jsonArray)
            .stream()
            .map(Article::new)
            .collect(toList());
  }
}