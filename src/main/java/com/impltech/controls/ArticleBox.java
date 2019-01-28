package com.impltech.controls;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * This is custom control that stores article and show it to the feed.
 *
 * @author Yuri Podolsky, 2018-10-17.
 *
 */
public class ArticleBox extends VBox {

  @FXML private Text groupName;
  @FXML private ImageView image;
  @FXML private Text title;
  @FXML private Text author;
  @FXML private Text shortBody;

  public ArticleBox() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ArticleBox.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public Text getGroupName() {
    return groupName;
  }

  public void setGroupName(String value) {
    groupNameTextProperty().set(value);
  }

  private StringProperty groupNameTextProperty() {
    return groupName.textProperty();
  }

  public ImageView getImage() {
    return image;
  }

  public void setImage(ImageView articleImage) {
    this.image = articleImage;
  }

  public Text getTitle() {
    return title;
  }

  public void setTitle(Text title) {
    this.title = title;
  }

  public StringProperty titleTextProperty() {
    return title.textProperty();
  }

  public Text getAuthor() {
    return author;
  }

  public void setAuthor(Text author) {
    this.author = author;
  }

  public StringProperty authorTextProperty() {
    return author.textProperty();
  }

  public Text getShortBody() {
    return shortBody;
  }

  public void setShortBody(Text shortBody) {
    this.shortBody = shortBody;
  }

  public StringProperty shortBodyTextProperty() {
    return shortBody.textProperty();
  }

  public void bindTitleStyle (StringProperty style) {
    title.styleProperty().bind(style);
  }

  public void bindShortBodyStyle (StringProperty style) {
    shortBody.styleProperty().bind(style);
  }

}
