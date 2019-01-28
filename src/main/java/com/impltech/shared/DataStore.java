package com.impltech.shared;

import com.impltech.exception.TokenExpiredException;
import com.impltech.model.Article;
import com.impltech.model.Category;
import com.impltech.service.RestClient;
import com.sun.javafx.collections.ObservableSequentialListWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents data storage for application.
 *
 * @author Yuri Podolsky, 18-10-12
 */
public class DataStore {

  private ObservableSequentialListWrapper<Article> articles;
  private final ObservableList<Category> categories;
  private Template template;
  private StringProperty titleStyle;
  private StringProperty shortBodyStyle;
  private StringProperty backgroundColor;
  private int fontSizeBody;

  private int pageIndex = 0;
  private boolean scrollFinished = false;

  private DataStore() throws TokenExpiredException {
    RestClient client = RestClient.getInstance();
    this.articles = new ObservableSequentialListWrapper<>(new ArrayList<>());
    this.categories = FXCollections.observableArrayList();
    Category category = new Category("", "Все статьи");
    categories.add(category);
    Collection<Category> categoryCollection = client.fetchCategories();
    categories.addAll(categoryCollection);
    this.template = client.fetchTemplate();
    String fontMetric = template.getGeneralStyles().get("fontSizeMetric");
    int fontSizeTitle = template.getArticleStyles().get("title").getFontSize();
    int fontSizeShortBody = template.getArticleStyles().get("shortBody").getFontSize();
    this.fontSizeBody = template.getArticleStyles().get("body").getFontSize();
    this.titleStyle = new ReadOnlyStringWrapper("-fx-font-size: " + fontSizeTitle + " " + fontMetric + ";");
    this.shortBodyStyle = new ReadOnlyStringWrapper("-fx-font-size: " + fontSizeShortBody + " " + fontMetric + ";");
    String backgroundColor = template.getGeneralStyles().get("backgroundColor");
    this.backgroundColor = new ReadOnlyStringWrapper("-fx-background-color: " + backgroundColor + ";");
  }

  public static DataStore initialize() throws TokenExpiredException {
    return new DataStore();
  }

  public Template getTemplate() {
    return template;
  }

  public StringProperty titleStyleProperty() {
    return titleStyle;
  }

  public StringProperty shortBodyStyleProperty() {
    return shortBodyStyle;
  }

  public ObservableList<Article> getArticles() {
    return articles;
  }

  public String getBackgroundColor() {
    return backgroundColor.get();
  }

  public StringProperty backgroundColorProperty() {
    return backgroundColor;
  }

  public int getFontSizeBody() {
    return fontSizeBody;
  }

  public int getPageIndex() {
    return pageIndex;
  }

  public void setPageIndex(int pageIndex) {
    this.pageIndex = pageIndex;
  }

  public boolean isScrollFinished() {
    return scrollFinished;
  }

  public void setScrollFinished(boolean scrollFinished) {
    this.scrollFinished = scrollFinished;
  }

  public ObservableList<Category> getCategories() {
    return categories;
  }

}
