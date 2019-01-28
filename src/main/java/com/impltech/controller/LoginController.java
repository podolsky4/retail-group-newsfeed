package com.impltech.controller;

import com.impltech.model.Credentials;
import com.impltech.service.RestClient;
import com.impltech.service.ViewManager;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Login page for JavaFX application.
 */
public class LoginController implements Initializable {

  /* Controls */
  @FXML
  public Label alert;
  @FXML
  public VBox loginForm;
  @FXML
  public TextField login;
  @FXML
  public TextField password;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @FXML
  public void submitRequest(ActionEvent event) {
    if (login.getLength() == 0) {
      Tooltip loginTip = new Tooltip("Имя пользователя не должно быть пустым.");
      login.setTooltip(loginTip);
    } else if (password.getLength() == 0) {
      Tooltip passwordTip = new Tooltip("Пароль не должен быть пустым.");
      password.setTooltip(passwordTip);
      password.setStyle("-fx-border-color: #c00;");
    } else {
      Credentials prepared = new Credentials(login.getText(), password.getText());
      Task task = new Task<Integer>() {
        @Override
        public Integer call() {
          int responseStatus = 0;
          try {
            responseStatus = RestClient.getInstance().authorize(prepared);
          } catch (IOException | JSONException e) {
            e.printStackTrace();
          }

          return responseStatus;
        }
      };
      new Thread(task).start();

      try {
        int responseStatus = (Integer) task.get();
        if (responseStatus == 200) {
          ViewManager mainManager = ViewManager.getInstance();
          mainManager.loadHomeView();
          mainManager.showHomeView(event);
        } else if (responseStatus == 401) {
          alert.setText("Неправильный логин или пароль.");
          alert.getStyleClass().add(".label-error");
        } else if (responseStatus == 403) {
          alert.setText("Нет доступа.");
          alert.getStyleClass().add(".label-error");
        } else if (responseStatus == 404) {
          alert.setText("Пользователь не найден.");
          alert.getStyleClass().add(".label-error");
        } else if (responseStatus == 500) {
          alert.setText("Сервер не доступен.");
          alert.getStyleClass().add(".label-error");
        }
      } catch (InterruptedException | ExecutionException e) {
        alert.setText("Сервер не доступен");
        alert.getStyleClass().add(".label-error");
        throw new RuntimeException(e.getMessage());
      }
    }
  }
}
