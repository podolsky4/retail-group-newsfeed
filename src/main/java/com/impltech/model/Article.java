package com.impltech.model;

import com.impltech.service.RestClient;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.util.converter.LocalDateTimeStringConverter;
import org.json.JSONException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static javax.json.JsonValue.ValueType.NULL;

/**
 * Represents an updatable Article in the collection of the articles.
 *
 * @author Yuri Podolsky, 2018-10-03
 */
public class Article {
  /**
   * Need to store id
   */
  private final StringProperty id;
  private final BooleanProperty confirmation;
  private final Property<JsonArray> likes;
  private final Property<JsonArray> comments;
  private final Property<LocalDateTime> timeOfPublication;
  private final StringProperty title;
  private final StringProperty shortBody;
  private final StringProperty body;
  private final StringProperty status;
  private final Property<JsonObject> category;
  private final StringProperty categoryName;
  private final Property<JsonObject> file;
  private final StringProperty user;
  private final Type type;
  private Property<String> imgUrl;
  private Property<String> imgSmallUrl;
  private Property<String> screenshot;
  private Property<String> videoMP4Url;
  private final BooleanProperty liked;
  private final ObjectProperty<Image> imageSmallProperty;
  public enum Type {
    imageType, videoType
  }

  /**
   * Article constructor
   * @param jsonValue incoming article in JSON object
   *
   */
  public Article(JsonValue jsonValue) {

    final JsonObject jsonObject = (JsonObject) jsonValue;
    this.id = new ReadOnlyStringWrapper(this, "id", jsonObject.getString("_id"));
    this.confirmation = new ReadOnlyBooleanWrapper(this, "confirmation", jsonObject.getBoolean("confirmation"));
    this.likes = new SimpleObjectProperty<>(this, "likes", jsonObject.getJsonArray("likes"));
    RestClient client = RestClient.getInstance();
    String userId = null;
    try {
      userId = client.user().getString("_id");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    boolean initValue = false;
    if (likes.getValue() != null) {
      if (!likes.getValue().isEmpty()) {
        for (int i = 0; i < likes.getValue().size(); i++) {
          if (likes.getValue().getString(i).equals(userId)) {
            initValue = true;
            break;
          }
        }
      }
    }
    this.liked  = new SimpleBooleanProperty(this, "isLiked", initValue);
    this.comments = new SimpleObjectProperty<>(this, "comments", jsonObject.getJsonArray("comments"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    LocalDateTimeStringConverter dateConverter = new LocalDateTimeStringConverter(formatter, DateTimeFormatter.ISO_DATE_TIME);
    this.timeOfPublication = new ReadOnlyObjectWrapper<>(this, "timeOfPublication", dateConverter.fromString(jsonObject.getString("timeOfPublication")));
    this.title = new ReadOnlyStringWrapper(this, "title", jsonObject.getString("title"));
    this.shortBody = new ReadOnlyStringWrapper(this, "shortBody", jsonObject.getString("shortBody"));
    final JsonValue bodyValue = jsonObject.get("body");
    this.body = new ReadOnlyStringWrapper(this, "body", bodyValue == null ? "" : ((JsonString)bodyValue).getString());
    this.status = new ReadOnlyStringWrapper(this, "status", jsonObject.getString("status"));
    this.category = new SimpleObjectProperty<>(this, "category", jsonObject.getJsonObject("category"));
    String output = capitalize(jsonObject.getJsonObject("category").getString("name"));
    this.categoryName = new ReadOnlyStringWrapper(this, "categoryName", output);
    this.file = new SimpleObjectProperty<>(this, "file", jsonObject.getJsonObject("file"));
    final JsonObject user = jsonObject.getJsonObject("user");
    String firstName = user.getString("firstName");
    String lastName = user.getString("lastName");
    final String authorName = String.format("%s %s", firstName, lastName);
    this.user = new ReadOnlyStringWrapper(this, "user", authorName);
    this.type = (jsonObject.getJsonObject("file").getString("contentType").matches("video(.*)"))? Type.videoType : Type.imageType;

    if (this.type == Type.videoType) {
      final JsonValue screenshot = jsonObject.get("screenshot");
      this.screenshot = new SimpleObjectProperty<>(this, "screenshot", screenshot.getValueType() == NULL ? "" : ((JsonString) screenshot).getString());
      final JsonValue videoMP4Url = jsonObject.get("videoMP4Url");
      this.videoMP4Url = new SimpleObjectProperty<>(this, "videoMP4Url", videoMP4Url.getValueType() == NULL ? "" : ((JsonString) videoMP4Url).getString());
    } else {
      final JsonValue imgUrl = jsonObject.get("imgUrl");
      this.imgUrl = new SimpleObjectProperty<>(this, "imgUrl", imgUrl.getValueType() == NULL ? "" : ((JsonString) imgUrl).getString());
      final JsonValue imgSmallUrl = jsonObject.get("imgSmallUrl");
      this.imgSmallUrl = new SimpleObjectProperty<>(this, "imgSmallUrl", imgUrl.getValueType() == NULL ? "" : ((JsonString) imgSmallUrl).getString());
    }
    Image image = new Image("img/preloader.gif");
    this.imageSmallProperty = new SimpleObjectProperty<>(this, "imageSmallProperty", image);
    }

  public final String getId() {
    return id.getValue();
  }

  public void setId(String id) {
    this.id.setValue(id);
  }

  public Property<JsonArray> likesProperty() {
    return likes;
  }

  public void setLikes(JsonArray likes) {
    this.likes.setValue(likes);
  }

  public JsonArray getComments() {
    return comments.getValue();
  }

  public LocalDateTime getTimeOfPublication() {
    return timeOfPublication.getValue();
  }

  public Property<LocalDateTime> timeOfPublicationProperty() {
    return timeOfPublication;
  }

  public String getTitle() {
    return title.getValue();
  }

  public Property<String> titleProperty() {
    return title;
  }

  public void setTitle(String title) {
    this.title.setValue(title);
  }

  public Property<String> shortBodyProperty() {
    return shortBody;
  }

  public String getBody() {
    return body.getValue();
  }

  public String getCategoryName() {
    return categoryName.get();
  }

  public StringProperty categoryNameProperty() {
    return categoryName;
  }

  public BooleanProperty likedProperty() {
    return liked;
  }

  public void setLiked(boolean liked) {
    this.liked.set(liked);
  }

  public ObjectProperty<Image> imageSmallPropertyProperty() {
    return imageSmallProperty;
  }

  public void setImageSmallProperty(Image imageSmallProperty) {
    this.imageSmallProperty.set(imageSmallProperty);
  }

  public StringProperty userProperty() {
    return user;
  }

  public Type getType() {
    return type;
  }

  public String getImgUrl() {
    return imgUrl.getValue();
  }

  public Property<String> imgUrlProperty() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl.setValue(imgUrl);
  }

  public String getImgSmallUrl() {
    return imgSmallUrl.getValue();
  }

  public Property<String> imgSmallUrlProperty() {
    return imgSmallUrl;
  }

  public void setImgSmallUrl(String imgSmallUrl) {
    this.imgSmallUrl.setValue(imgSmallUrl);
  }

  public String getScreenshot() {
    return screenshot.getValue();
  }

  public String getVideoMP4Url() {
    return videoMP4Url.getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Article article = (Article) o;

    if (id != null ? !id.equals(article.id) : article.id != null) return false;
    return title != null ? !title.equals(article.title) : article.title != null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (confirmation != null ? confirmation.hashCode() : 0);
    result = 31 * result + (likes != null ? likes.hashCode() : 0);
    result = 31 * result + (comments != null ? comments.hashCode() : 0);
    result = 31 * result + (timeOfPublication != null ? timeOfPublication.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (shortBody != null ? shortBody.hashCode() : 0);
    result = 31 * result + (body != null ? body.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (category != null ? category.hashCode() : 0);
    result = 31 * result + (file != null ? file.hashCode() : 0);
    result = 31 * result + (user != null ? user.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  private String capitalize(String original) {
    if (original == null || original.length() == 0) {
      return original;
    }
    return original.substring(0, 1).toUpperCase() + original.substring(1);
  }


}
