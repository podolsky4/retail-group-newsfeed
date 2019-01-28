package com.impltech.controller;

import com.impltech.Main;
import com.impltech.controls.ArticleBox;
import com.impltech.controls.LargeArticleBox;
import com.impltech.handler.TransitionHandler;
import com.impltech.model.Article;
import com.impltech.model.Category;
import com.impltech.service.RestClient;
import com.impltech.service.ViewManager;
import com.impltech.shared.DataStore;
import com.impltech.task.FetchImage;
import com.impltech.utils.ContentTypeChecker;
import com.impltech.utils.MyResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.converter.LocalDateTimeStringConverter;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static com.impltech.Main.executorService;

/**
 * Controller for filtered articles page
 *
 * @author Yuri Podolsky, 2018-08-26
 */
public class FilterArticleController implements Initializable {

  private static final Logger logger = Logger.getLogger(FilterArticleController.class.getName());
  private final MyResourceBundle bundle = Main.getBundle();

  private final String categoryId;

  private DataStore dataStore;

  private final ObservableList<Article> categorizedList = FXCollections.observableArrayList();

  @FXML
  private BorderPane window;
  @FXML
  private ListView<Article> viewCatArticles;
  @FXML
  private ListView<Category> sidebar;
  @FXML
  private Button hamburgerButton;

  private int pageIndex = 0;
  private boolean scrollFinished = false;

  public FilterArticleController(String categoryId) {
    this.categoryId = categoryId;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TransitionHandler tt = new TransitionHandler(hamburgerButton, sidebar);
    window.setOnMouseClicked(event -> hideSidebar(tt));
    viewCatArticles.setOnMouseClicked(event -> hideSidebar(tt));

    dataStore = Main.getDataStore();
    ViewManager manager = ViewManager.getInstance();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(bundle.getString("datetime.format"));
    LocalDateTimeStringConverter dateConverter = new LocalDateTimeStringConverter(formatter, null);

    viewCatArticles.setItems(categorizedList);
    viewCatArticles.setCellFactory(new Callback<ListView<Article>, ListCell<Article>>() {

      @Override
      public ListCell<Article> call(ListView<Article> list) {
        return new ListCell<Article>() {
          @Override
          public void updateItem(Article item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
              if (list.getItems().get(0).equals(item)) {
                LargeArticleBox largeArticleBox = new LargeArticleBox();
                largeArticleBox.groupNameTextProperty().bind(item.categoryNameProperty());
                String imgUrl = ContentTypeChecker.check(item);
                FetchImage fetchImage = new FetchImage(imgUrl);
                fetchImage.setOnSucceeded(worker -> item.setImageSmallProperty(fetchImage.getValue()));
                executorService().submit(fetchImage);
                largeArticleBox.getImage().imageProperty().bind(item.imageSmallPropertyProperty());
                largeArticleBox.titleTextProperty().bind(item.titleProperty());
                largeArticleBox.getTitle().styleProperty().bind(dataStore.titleStyleProperty());
                largeArticleBox.timeTextProperty().bindBidirectional(item.timeOfPublicationProperty(), dateConverter);
                largeArticleBox.setUserData(item.getId());
                largeArticleBox.setOnMouseClicked(event -> {
                  hideSidebar(tt);
                  manager.showArticleView(event);

                });
                setGraphic(largeArticleBox);
              } else {
                ArticleBox articleBox = new ArticleBox();
                LocalDateTime date = item.getTimeOfPublication();
                articleBox.setGroupName(String.format("%s  |  %s", item.getCategoryName(), dateConverter.toString(date)));
                String imgSmallUrl = ContentTypeChecker.check(item);
                //Thread for loading images later
                FetchImage fetchImage = new FetchImage(imgSmallUrl);
                fetchImage.setOnSucceeded(worker -> {
                  item.setImageSmallProperty(fetchImage.getValue());
                  logger.fine("Done!");
                });
                executorService().submit(fetchImage);
                articleBox.getImage().imageProperty().bind(item.imageSmallPropertyProperty());
                articleBox.titleTextProperty().bind(item.titleProperty());
                articleBox.authorTextProperty().bind(item.userProperty());
                articleBox.shortBodyTextProperty().bind(item.shortBodyProperty());
                articleBox.bindTitleStyle(dataStore.titleStyleProperty());
                articleBox.bindShortBodyStyle(dataStore.shortBodyStyleProperty());
                articleBox.setUserData(item.getId());
                articleBox.setOnMouseClicked(manager::showArticleView);
                setGraphic(articleBox);
              }
            }
          }
        };
      }

    });

    sidebar.setItems(dataStore.getCategories());
    sidebar.setCellFactory(new Callback<ListView<Category>, ListCell<Category>>() {
      @Override
      public ListCell<Category> call(ListView<Category> param) {
        return new ListCell<Category>() {

          @Override
          protected void updateItem(Category item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
              if (param.getItems().get(0).equals(item)) {
                Button allArticles = new Button(item.getName());
                allArticles.setAlignment(Pos.CENTER_LEFT);
                allArticles.getStyleClass().add("category-item");
                allArticles.setOnAction(event -> {
                  hideSidebar(tt);
                  manager.showHomeView(event);
                });
                setGraphic(allArticles);
              } else {
                Button categoryButton = new Button(item.getName());
                categoryButton.setUserData(item.getId());
                categoryButton.setAlignment(Pos.CENTER_LEFT);
                categoryButton.setOnAction(event -> {
                  hideSidebar(tt);
                  manager.showFilterView(event);
                });
                categoryButton.getStyleClass().add("category-item");
                setGraphic(categoryButton);
              }
            }
          }
        };
      }
    });

    // Attach transition handler for animation
    hamburgerButton.setOnAction(tt);

    Platform.runLater(() -> {
      ScrollBar bar = getListViewScrollBar(viewCatArticles);
      if (bar != null) {
        bar.valueProperty().addListener((observable, oldValue, newValue) -> {
          double value = newValue.doubleValue();
          ScrollBar bar1 = getListViewScrollBar(viewCatArticles);
          if (value == bar1.getMax()) {
            if (!scrollFinished) {
              double targetValue = value * categorizedList.size();
              addArticles();
              bar1.setValue(targetValue / categorizedList.size());
            }
          }
        });
      }
    });
    addArticles();
  }

  private void hideSidebar(TransitionHandler tt) {
    boolean opened = tt.getToggleFlag();
    if (opened) {
      tt.hide();
      tt.setToggleFlag(false);
    }
  }

  // Load new part of articles from the server
  private void addArticles() {
    Platform.runLater(() -> {
      logger.info("Loading new articles from category ...");
      RestClient client = RestClient.getInstance();
      Collection<Article> articlePortion = client.getArticlesByCategory(categoryId, pageIndex);
      articlePortion.forEach(article -> {
        String imgSmallUrl = ContentTypeChecker.check(article);
        FetchImage fetchImage = new FetchImage(imgSmallUrl);
        fetchImage.setOnSucceeded(worker -> {
          article.setImageSmallProperty(fetchImage.getValue());
          logger.fine("Done!");
        });
        fetchImage.setOnRunning(worker -> logger.info("Fetching image for article title: " + article.getTitle()));
        fetchImage.setOnFailed(worker -> logger.warning("Fetch image failed"));
        executorService().submit(fetchImage);
      });
      if (articlePortion.size() < 10) {
        scrollFinished = true;
      } else {
        ++pageIndex;
      }
      categorizedList.addAll(articlePortion);
      logger.info("articles: " + categorizedList.size());
    });

  }

  private ScrollBar getListViewScrollBar(ListView<?> listView) {
    ScrollBar scrollbar = null;
    for (Node node : listView.lookupAll(".scroll-bar")) {
      if (node instanceof ScrollBar) {
        ScrollBar bar = (ScrollBar) node;
        if (bar.getOrientation().equals(Orientation.VERTICAL)) {
          scrollbar = bar;
        }
      }
    }
    return scrollbar;
  }

}