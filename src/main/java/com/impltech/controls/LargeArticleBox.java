package com.impltech.controls;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * This is custom control that stores article in a large box and show it to the feed.
 *
 * @author Yuri Podolsky, 2018-10-17.
 *
 */
public class LargeArticleBox extends VBox {
  @FXML
  public Text groupName;
  @FXML
  public ImageView image;
  @FXML
  public Text title;
  @FXML
  public Text time;

  public LargeArticleBox() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LargeArticleBox.fxml"));
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

  public void setGroupName(String groupName) {
    groupNameTextProperty().set(groupName);
  }

  public StringProperty groupNameTextProperty() {
    return groupName.textProperty();
  }

  public ImageView getImage() {
    return image;
  }

  public void setImage(Image value) {
    getImage().imageProperty().set(value);
  }

  public Text getTitle() {
    return title;
  }

  public void setTitle(String title) {
    titleTextProperty().set(title);
  }

  public StringProperty titleTextProperty() {
    return title.textProperty();
  }

  public Text getTime() {
    return time;
  }

  public void setTime(Text time) {
    this.time = time;
  }

  public StringProperty timeTextProperty() {
    return time.textProperty();
  }
}
