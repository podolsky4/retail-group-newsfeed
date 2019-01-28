package com.impltech.controller;

import com.impltech.Main;
import com.impltech.model.Article;
import com.impltech.model.Comment;
import com.impltech.model.CommentOutcome;
import com.impltech.service.RestClient;
import com.impltech.service.ViewManager;
import com.impltech.shared.DataStore;
import com.impltech.task.FetchImage;
import com.impltech.utils.Browser;
import com.impltech.utils.MediaControl;
import com.impltech.utils.MyResourceBundle;
import com.impltech.utils.TranscoderSVG;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;
import org.json.JSONException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.impltech.Main.executorService;

/**
 * Single article controller.
 *
 * @author Yuri Podolsky, 26-09-2018
 */
public class ArticleController implements Initializable {

  private static final Logger logger = Logger.getLogger(ArticleController.class.getName());
  private final MyResourceBundle bundle = Main.getBundle();
  private final RestClient client;
  private final String articleId;
  private Article article;
  private MediaControl mediaControl;

  @FXML
  public ScrollPane scrollPane;
  @FXML
  private Pane articleBody;
  @FXML
  private VBox fullArticleTextsScroll;
  @FXML
  private VBox listOfComments;
  @FXML
  private Text categoryName;
  @FXML
  private Text articleDate;
  @FXML
  private Text articleTitle;
  @FXML
  private Text articleAuthor;
  @FXML
  private Text articleLikeCount;
  @FXML
  private BorderPane mediaContainer;
  @FXML
  private TextArea commentContent;
  @FXML
  private Text commentCount;
  @FXML
  private Button backButton;
  @FXML
  private Button likeButton;
  @FXML
  private Button sendButton;
  @FXML
  private Label commentNotification;

  /**
   * The controller receives the article in JSON format
   */
  public ArticleController(String articleId) {
    this.articleId = articleId;
    client = RestClient.getInstance();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    DataStore dataStore = Main.getDataStore();
    article = client.fetchArticle(articleId);
    TranscoderSVG.transcode((article.likedProperty().get())
            ? bundle.getString("img.path.thumbUp.filled")
            : bundle.getString("img.path.thumbUp"), likeButton);
    ViewManager manager = ViewManager.getInstance();
    String backgroundColor = dataStore.getBackgroundColor();
    fullArticleTextsScroll.styleProperty().bind(dataStore.backgroundColorProperty());

    // Map JSON property to view
    if (article.getType() == Article.Type.videoType) {
      String videoURL = article.getVideoMP4Url();
      URL url;
      Path tempFile;
      try {
        url = new URL(videoURL);
        tempFile = Files.createTempFile("now-playing", ".mp4");
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }

      try (InputStream stream = url.openStream()) {
        Files.copy(stream, Objects.requireNonNull(tempFile), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }

      String fileUrl = Objects.requireNonNull(tempFile).toFile().toURI().toString();
      mediaControl = new MediaControl(fileUrl);
      mediaControl.setMinSize(550, 320);
      mediaControl.setPrefSize(550, 320);
      mediaControl.setMaxSize(550, 320);
      mediaContainer.setCenter(mediaControl);
    } else {
      ImageView articleMedia = new ImageView();
      articleMedia.setPreserveRatio(true);
      articleMedia.setFitWidth(550);
      FetchImage fetchImage = new FetchImage(article.getImgUrl());
      fetchImage.setOnSucceeded(worker -> articleMedia.setImage(fetchImage.getValue()));
      executorService().submit(fetchImage);
      mediaContainer.setCenter(articleMedia);
    }
    categoryName.textProperty().bind(article.categoryNameProperty());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(bundle.getString("datetime.format"));
    LocalDateTimeStringConverter converter = new LocalDateTimeStringConverter(formatter, null);
    articleDate.textProperty().bindBidirectional(article.timeOfPublicationProperty(), converter);
    articleTitle.textProperty().bind(article.titleProperty());
    articleTitle.styleProperty().bind(dataStore.titleStyleProperty());
    // Load article content into embedded browser
    String rawHtmlBody = article.getBody();
    int fontSizeBody = dataStore.getFontSizeBody();
    String fontMetric = dataStore.getTemplate().getGeneralStyles().get("fontSizeMetric");

    ScrollBar scrollbar = null;
    for (Node node : scrollPane.lookupAll(".scroll-bar")) {
      if (node instanceof ScrollBar) {
        ScrollBar bar = (ScrollBar) node;
        if (bar.getOrientation().equals(Orientation.VERTICAL)) {
          scrollbar = bar;
        }
      }
    }

    final Browser browser = new Browser(rawHtmlBody, fontSizeBody, fontMetric, backgroundColor, scrollPane);

    articleBody.getChildren().add(browser);
    articleAuthor.textProperty().bind(article.userProperty());
    articleLikeCount.textProperty().bindBidirectional(article.likesProperty(), new StringConverter<JsonArray>() {
      @Override
      public String toString(JsonArray object) {
        return String.valueOf(object.size());
      }

      @Override
      public JsonArray fromString(String string) {
        return null;
      }
    });

    // Button "Like"
    likeButton.setOnAction(event -> {
      likeButton.setDisable(true);
      try {
        JsonObject resp = client.likeArticle(article.getId(), Entity.json(""));
        JsonArray likes = resp.getJsonArray("likes");
        article.setLikes(likes);
        likeButton.setDisable(false);
      } catch (JSONException e) {
        logger.severe("Cannot put like to the article " + e.getMessage());
      }
    });

    ChangeListener<JsonArray> likeListener = (observable, oldValue, newValue) -> {

      String userId = null;
      try {
        userId = client.user().getString("_id");
      } catch (JSONException e) {
        e.printStackTrace();
      }

      if (!newValue.isEmpty()) {
        for (int i = 0; i < newValue.size(); i++) {
          if (newValue.getString(i).equals(userId)) {
            article.setLiked(true);
          } else {
            article.setLiked(false);
          }
        }
      } else {
        article.setLiked(false);
      }

    };
    article.likesProperty().addListener(likeListener);

    ChangeListener<Boolean> thumbUpListener = (observable, oldValue, newValue) ->
            TranscoderSVG.transcode((newValue) ?
                    bundle.getString("img.path.thumbUp.filled") :
                    bundle.getString("img.path.thumbUp"), likeButton);
    article.likedProperty().addListener(thumbUpListener);

    // Button "Send"
    sendButton.setOnAction(event -> {
      // Validate comment content
      if (!commentContent.getText().isEmpty() && !commentContent.getText().trim().isEmpty()) {
        try {
          client.sendComment(new CommentOutcome(commentContent.getText()), article.getId());
        } catch (JSONException e) {
          logger.severe("Cannot send comment to server " + e.getMessage());
        }
        commentContent.clear();
        commentNotification.setText(bundle.getString("article.comment.success"));
        commentNotification.setStyle("-fx-text-fill: #0a0");
      } else {
        commentNotification.setText(bundle.getString("article.comment.failed"));
        commentNotification.setStyle("-fx-text-fill: #a00");
      }
      commentNotification.setLabelFor(commentContent);
    });

    List<Comment> commentList = article.getComments()
            .stream()
            .map(Comment::new)
            .collect(Collectors.toList());

    commentCount.setText(String.valueOf(commentList.size()));

    // Show comments list for this article
    commentList.stream()
            .filter(Comment::getConfirmation)
            .forEach(i -> {
              try {
                VBox commentBox = FXMLLoader.load(getClass().getResource("/fxml/CommentBox.fxml"));
                Text commentAuthor = (Text) commentBox.getChildren().get(0);
                Text commentDate = (Text) commentBox.getChildren().get(1);
                Text commentContent = (Text) commentBox.getChildren().get(2);
                commentAuthor.textProperty().bind(i.authorProperty());
                commentDate.setText(i.getTime().format(formatter));
                commentContent.textProperty().bind(i.bodyProperty());
                listOfComments.getChildren().addAll(commentBox);
              } catch (IOException e) {
                logger.severe("Cannot load FXML for comment from the server: " + e.getMessage());
              }
            });

    // Returns to Home when click to the back button
    backButton.setOnAction(event -> {
      if (article.getType() == Article.Type.videoType) {
        mediaControl.stop();
      }
      article.likesProperty().removeListener(likeListener);
      article.likedProperty().removeListener(thumbUpListener);
      int indexArticle = IntStream.range(0, dataStore.getArticles().size())
              .filter(i -> article.getId().equals(dataStore.getArticles().get(i).getId()))
              .findFirst().getAsInt();
      dataStore.getArticles().set(indexArticle, article);
      manager.showHomeView(event);
    });
  }

}