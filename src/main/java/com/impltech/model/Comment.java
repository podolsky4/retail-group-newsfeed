package com.impltech.model;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.converter.LocalDateTimeStringConverter;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a comment in the collection of comments from an article.
 *
 * @author Yuri Podolsky, 2018-10-02
 */
public class Comment {
  private final Property<String> body;
  private final Property<Boolean> confirmation;
  private final Property<LocalDateTime> time;
  private final Property<String> author;

  public Comment(JsonValue jsonValue) {
    JsonObject jsonObject = (JsonObject) jsonValue;

    this.body = new ReadOnlyObjectWrapper<>(this, "body", jsonObject.getString("body"));
    this.confirmation = new ReadOnlyObjectWrapper<>(this, "confirmation", jsonObject.getBoolean("confirmation"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    LocalDateTimeStringConverter dateConverter = new LocalDateTimeStringConverter(formatter, DateTimeFormatter.ISO_DATE_TIME);
    this.time = new ReadOnlyObjectWrapper<>(this, "time", dateConverter.fromString(jsonObject.getString("time")));
    this.author = new ReadOnlyObjectWrapper<>(this, "author", String.format("%s %s",
            jsonObject.getJsonObject("user").getString("firstName"),
            jsonObject.getJsonObject("user").getString("lastName")));
  }

  public Property<String> bodyProperty() {
    return body;
  }

  public final Boolean getConfirmation() {
    return confirmation.getValue();
  }

  public final LocalDateTime getTime() {
    return time.getValue();
  }

  public void setTime(LocalDateTime time) {
    this.time.setValue(time);
  }

  public final String getAuthor() {
    return author.getValue();
  }

  public Property<String> authorProperty() {
    return author;
  }

  public void setAuthor(String author) {
    this.author.setValue(author);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Comment that = (Comment) o;

    if (body != null ? !body.equals(that.body) : that.body != null) return false;
    if (confirmation != null ? !confirmation.equals(that.confirmation) : that.confirmation != null) return false;
    if (time != null ? !time.equals(that.time) : that.time != null) return false;
    return author != null ? author.equals(that.author) : that.author == null;
  }

  @Override
  public int hashCode() {
    int result = body != null ? body.hashCode() : 0;
    result = 31 * result + (confirmation != null ? confirmation.hashCode() : 0);
    result = 31 * result + (time != null ? time.hashCode() : 0);
    result = 31 * result + (author != null ? author.hashCode() : 0);
    return result;
  }
}
