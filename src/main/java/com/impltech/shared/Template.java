package com.impltech.shared;

import com.impltech.model.TextStyle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents settings template that is used to set the display settings, fonts.
 *
 * @author Yuri Podolsky, 18-10-12
 */
public class Template {

  private ObservableMap<String, String> generalStyles;
  private ObservableMap<String, TextStyle> articleStyles;

  /**
   * Map json into template model
   * @param jsonValue incoming JSON object
   */
  public Template(JsonValue jsonValue) {
    JsonObject jsonObject = (JsonObject) jsonValue;
    Map<String, String> styles = new HashMap<>();
    styles.put("fontSizeMetric", jsonObject.getJsonObject("generalStyles").getString("fontSizeMetric"));
    styles.put("backgroundColor", jsonObject.getJsonObject("generalStyles").getString("backgroundColor"));
    this.generalStyles = FXCollections.observableMap(styles);
    Map<String, TextStyle> articleStylesMap = new HashMap<>();
    articleStylesMap.put("shortBody", new TextStyle(jsonObject.getJsonObject("articleStyles").getJsonObject("shortBody")));
    articleStylesMap.put("body", new TextStyle(jsonObject.getJsonObject("articleStyles").getJsonObject("body")));
    articleStylesMap.put("title", new TextStyle(jsonObject.getJsonObject("articleStyles").getJsonObject("title")));
    this.articleStyles = FXCollections.observableMap(articleStylesMap);
  }

  public ObservableMap<String, String> getGeneralStyles() {
    return generalStyles;
  }

  ObservableMap<String, TextStyle> getArticleStyles() {
    return articleStyles;
  }

}
