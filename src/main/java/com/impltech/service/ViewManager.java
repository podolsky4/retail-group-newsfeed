package com.impltech.service;

import com.impltech.Main;
import com.impltech.controller.ArticleController;
import com.impltech.controller.FilterArticleController;
import com.impltech.utils.MyResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * <p>Manages views, switches scenes when changing views.</p>
 *
 * @author Yuri Podolsky
 */
public class ViewManager {
  // Singleton i.e.
  private static ViewManager instance;
  private Parent homeRoot;
  private final MyResourceBundle bundle = Main.getBundle();

  private ViewManager() { }

  public static ViewManager getInstance() {
    if (instance == null) {
      instance = new ViewManager();
    }
    return instance;
  }

  /**
   * Root view
   *
   * @param root root node
   */
  public void rootView(Parent root) {
    Stage stage = Main.getMainWindow();
    Scene scene = new Scene(root);
    stage.setResizable(false);
    stage.setTitle(bundle.getString("title.app"));
    stage.setScene(scene);
    stage.getScene().getStylesheets().add("/css/styles.css");
    Image image = new Image(bundle.getString("img.path.icon"));
    stage.getIcons().add(image);
    stage.show();
  }

  /**
   * Loads home view
   *
   * @return loaded home parent node
   */
  public Parent loadHomeView() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getClass().getResource("/fxml/HomeView.fxml"));
    try {
      loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    homeRoot = loader.getRoot();
    return homeRoot;
  }

  /**
   * Switch scene
   *
   * @param event mouse event
   */
  public void showArticleView(MouseEvent event) {
    Parent articleWindow;
    String articleId = (String) ((Node) event.getSource()).getUserData();
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ArticleView.fxml"));
    Stage mainWindow = Main.getMainWindow();
    ArticleController articleController = new ArticleController(articleId);
    fxmlLoader.setController(articleController);

    try {
      articleWindow = fxmlLoader.load();
      //we don't need to change whole scene, only set new root.
      mainWindow.getScene().setRoot(articleWindow);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Show home view without loading
   *
   * @param event mouse event
   */
  public void showHomeView(ActionEvent event) {
    Stage mainWindow = Main.getMainWindow();
    //we don't need to change whole scene, only set new root.
    mainWindow.getScene().setRoot(homeRoot);
  }

  /**
   * Show article by category
   *
   * @param event mouse event
   */
  public void showFilterView(ActionEvent event) {
    Parent articleWindow;
    String id = (String) ((Node) event.getSource()).getUserData();
    FilterArticleController filterArticleController = new FilterArticleController(id);
    FXMLLoader viewLoader = new FXMLLoader();
    viewLoader.setLocation(getClass().getResource("/fxml/FilterView.fxml"));
    viewLoader.setController(filterArticleController);
    Stage mainWindow = Main.getMainWindow();
    try {
      articleWindow = viewLoader.load();
      //we don't need to change whole scene, only set new root.
      mainWindow.getScene().setRoot(articleWindow);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Show notification window at top right corner
   *
   * @param jsonObject incoming article in JSON
   *
   * @throws JSONException
   * May throws JSONException
   */
  public void showNotificationPopUp(JSONObject jsonObject) throws JSONException {
    Notifications notification = Notifications.create()
              .title(bundle.getString("title.notification"))
              .text(bundle.getString("article.notify") + ": " + jsonObject.getString("title"))
              .graphic(null)
              .position(Pos.TOP_RIGHT);
    notification.showInformation();
  }

}
