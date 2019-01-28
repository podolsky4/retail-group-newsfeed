package com.impltech.handler;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.util.Duration;

/**
 * Transition Handler for transition animation.
 *
 * @author Yuri Podolsky, 18-10-17
 */
public class TransitionHandler implements EventHandler<ActionEvent> {
  private final Button button;
  private final Node sidebar;
  private final TranslateTransition transition;
  private Boolean toggleFlag = false;

  public TransitionHandler(Button button, Node sidebar) {
    this.button = button;
    this.sidebar = sidebar;
    transition = new TranslateTransition(Duration.millis(300), sidebar);
    this.sidebar.setTranslateX(-250f);
    transition.setOnFinished(event -> button.setDisable(false));
  }

  @Override
  public void handle(ActionEvent event) {
    button.setDisable(true);
    toggleFlag = !toggleFlag;
    if (toggleFlag) {
      show();
    } else {
      hide();
    }
  }

  public void hide() {
    transition.setByX(-250f);
    transition.play();
    sidebar.setTranslateX(-250f);
  }

  private void show() {
    transition.setByX(250f);
    transition.play();
    sidebar.setTranslateX(0f);
  }

  public Boolean getToggleFlag() {
    return toggleFlag;
  }

  public void setToggleFlag(Boolean toggleFlag) {
    this.toggleFlag = toggleFlag;
  }
}
