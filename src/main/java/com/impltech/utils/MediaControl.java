package com.impltech.utils;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Customized media player in the scene.
 *
 * @author Yuri Podolsky
 */
public class MediaControl extends BorderPane {

  private final MediaPlayer mp;
  private final MediaView mediaView;
  private final boolean repeat = false;
  private boolean stopRequested = false;
  private boolean atEndOfMedia = false;
  private Duration duration;
  private final Slider timeSlider;
  private final Label playTime;
  private final Slider volumeSlider;
  private final HBox mediaBar;
  private final Pane mvPane;
  private Stage newStage;
  private boolean fullScreen = false;

  @Override
  protected void layoutChildren() {
    if (mediaView != null && getBottom() != null) {
      mediaView.setFitWidth(getWidth());
      mediaView.setFitHeight(getHeight() - getBottom().prefHeight(-1));
    }
    super.layoutChildren();
    if (mediaView != null && getCenter() != null) {
      mediaView.setTranslateX((((Pane) getCenter()).getWidth() -
              mediaView.prefWidth(-1)) / 2);
      mediaView.setTranslateY((((Pane) getCenter()).getHeight() -
              mediaView.prefHeight(-1)) / 2);
    }
  }

  @Override
  protected double computeMinWidth(double height) {
    return mediaBar.prefWidth(-1);
  }

  @Override
  protected double computeMinHeight(double width) {
    return 200;
  }

  @Override
  protected double computePrefWidth(double height) {
    return Math.max(mp.getMedia().getWidth(), mediaBar.prefWidth(height));
  }

  @Override
  protected double computePrefHeight(double width) {
    return mp.getMedia().getHeight() + mediaBar.prefHeight(width);
  }

  @Override
  protected double computeMaxWidth(double height) {
    return Double.MAX_VALUE;
  }

  @Override
  protected double computeMaxHeight(double width) {
    return Double.MAX_VALUE;
  }

  public MediaControl(final String url) {
    Media media = new Media(url);
    mp = new MediaPlayer(media);
    mp.setAutoPlay(false);
    this.getStyleClass().add("media");
    mediaView = new MediaView(mp);
    mvPane = new Pane();
    mvPane.getChildren().add(mediaView);
    mvPane.getStyleClass().clear();
    mvPane.getStyleClass().add("mv-pane");
    setCenter(mvPane);
    mediaBar = new HBox(5.0);
    mediaBar.setPadding(new Insets(5, 10, 5, 10));
    mediaBar.setAlignment(Pos.CENTER_LEFT);
    BorderPane.setAlignment(mediaBar, Pos.CENTER);

    final Button playButton = new Button();
    playButton.setMinWidth(Control.USE_PREF_SIZE);

    String PLAY = "/img/play.png";
    String PAUSE = "/img/pause.png";
    Image PlayButton = new Image(getClass().getResourceAsStream(PLAY));
    Image PauseButton = new Image(getClass().getResourceAsStream(PAUSE));
    ImageView imageViewPlay = new ImageView(PlayButton);
    ImageView imageViewPause = new ImageView(PauseButton);
    playButton.setGraphic(imageViewPlay);
    playButton.setOnAction((ActionEvent e) -> {
      updateValues();
      MediaPlayer.Status status = mp.getStatus();
      if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
        // don't do anything in these states
        return;
      }

      if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
        // rewind the movie if we're sitting at the end
        if (atEndOfMedia) {
          mp.seek(mp.getStartTime());
          atEndOfMedia = false;
          playButton.setGraphic(imageViewPlay);
          updateValues();
        }
        mp.play();
        playButton.setGraphic(imageViewPause);
      } else {
        mp.pause();
      }
    });
    ReadOnlyObjectProperty<Duration> time = mp.currentTimeProperty();
    time.addListener((ObservableValue<? extends Duration> observable,
                      Duration oldValue, Duration newValue) -> updateValues());
    mp.setOnPlaying(() -> {
      if (stopRequested) {
        mp.pause();
        stopRequested = false;
      } else {
        playButton.setGraphic(imageViewPause);
      }
    });
    mp.setOnPaused(() -> playButton.setGraphic(imageViewPlay));
    mp.setOnReady(() -> {
      duration = mp.getMedia().getDuration();
      updateValues();
    });

    mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
    mp.setOnEndOfMedia(() -> {
      if (!repeat) {
        playButton.setGraphic(imageViewPlay);
        stopRequested = true;
        atEndOfMedia = true;
      }
    });
    mediaBar.getChildren().add(playButton);

    // Time label
    Label timeLabel = new Label("Время");
    timeLabel.setMinWidth(Control.USE_PREF_SIZE);
    mediaBar.getChildren().add(timeLabel);

    // Time slider
    timeSlider = new Slider();
    timeSlider.setMinWidth(30);
    timeSlider.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(timeSlider, Priority.ALWAYS);

    DoubleProperty timeValue = timeSlider.valueProperty();
    timeValue.addListener((ObservableValue<? extends Number> observable,
                           Number old, Number now) -> {
      if (timeSlider.isValueChanging()) {
        // multiply duration by percentage calculated by slider position
        if (duration != null) {
          mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
        }
        updateValues();
      } else if (Math.abs(now.doubleValue() - old.doubleValue()) > 1.5) {
        // multiply duration by percentage calculated by slider position
        if (duration != null) {
          mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
        }
      }
    });
    mediaBar.getChildren().add(timeSlider);

    // Play label
    playTime = new Label();
    playTime.setMinWidth(Control.USE_PREF_SIZE);
    mediaBar.getChildren().add(playTime);

    /* ---------- FULLSCREEN BUTTON ---------- */
    String FULLSCREEN = "/img/display.png";
    Image fullScreenImage = new Image(getClass().getResourceAsStream(FULLSCREEN));
    ImageView fullScreenImageView = new ImageView(fullScreenImage);
    Button buttonFullScreen = new Button();
    buttonFullScreen.setGraphic(fullScreenImageView);
    buttonFullScreen.setMinWidth(Control.USE_PREF_SIZE);

    buttonFullScreen.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        if (!fullScreen) {
          newStage = new Stage();
          ReadOnlyBooleanProperty full = newStage.fullScreenProperty();
          full.addListener((ObservableValue<? extends Boolean> ov, Boolean old, Boolean now) -> onFullScreen());
          final BorderPane borderPane = new BorderPane() {

            @Override
            protected void layoutChildren() {
              if (getBottom() != null) {
                mediaView.setFitWidth(getWidth());
                double height = getHeight() -
                        getBottom().prefHeight(-1);
                mediaView.setFitHeight(height);
              }
              super.layoutChildren();
              final Pane center = (Pane) getCenter();
              if (center != null) {
                //if smaller pane has content
                double width = center.getWidth() - mediaView.prefWidth(-1);
                double height = center.getHeight() - mediaView.prefHeight(-1);

                mediaView.setTranslateX(width / 2.0);
                mediaView.setTranslateY(height / 2.0);
              }
            }
          };

          setCenter(null);
          setBottom(null);
          borderPane.setCenter(mvPane);
          borderPane.setBottom(mediaBar);

          Scene newScene = new Scene(borderPane);
          newStage.setScene(newScene);
          //Workaround for disposing stage when exit fullscreen
          newStage.setX(-100000);
          newStage.setY(-100000);

          newStage.setFullScreen(true);
          fullScreen = true;
          newStage.show();

        } else {
          //toggle FullScreen
          fullScreen = false;
          newStage.setFullScreen(false);

        }
      }
    });
    mediaBar.getChildren().add(buttonFullScreen);

    // Volume label
    Label volumeLabel = new Label("Звук");
    volumeLabel.setMinWidth(Control.USE_PREF_SIZE);
    mediaBar.getChildren().add(volumeLabel);

    // Volume slider
    volumeSlider = new Slider();
    volumeSlider.setPrefWidth(70);
    volumeSlider.setMinWidth(30);
    volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
    volumeSlider.valueProperty().addListener((Observable ov) -> {
    });

    final DoubleProperty volume = volumeSlider.valueProperty();
    volume.addListener((ObservableValue<? extends Number> observable,
                        Number old, Number now) -> mp.setVolume(volumeSlider.getValue() / 100.0));
    mediaBar.getChildren().add(volumeSlider);
    setBottom(mediaBar);
  }

  // Toggle to Fullscreen mode
  private void onFullScreen() {
    if (!newStage.isFullScreen()) {

      fullScreen = false;
      BorderPane smallBP = (BorderPane) newStage.getScene().getRoot();
      smallBP.setCenter(null);
      setCenter(mvPane);

      smallBP.setBottom(null);
      setBottom(mediaBar);
      Platform.runLater(() -> newStage.close());
    }
  }

  // Update values
  private void updateValues() {
    if (playTime != null && timeSlider != null && volumeSlider != null && duration != null) {
      Platform.runLater(() -> {
        Duration now = mp.getCurrentTime();
        playTime.setText(formatTime(now, duration));
        timeSlider.setDisable(duration.isUnknown());
        if (!timeSlider.isDisabled() &&
                duration.greaterThan(Duration.ZERO) &&
                !timeSlider.isValueChanging()) {
          final double value = now.divide(duration).toMillis() * 100.0;
          timeSlider.setValue(value);
        }
        if (!volumeSlider.isValueChanging()) {
          final int value = (int) Math.round(mp.getVolume() * 100);
          volumeSlider.setValue(value);
        }
      });
    }
  }

  private String formatTime(Duration elapsed, Duration duration) {
    int intElapsed = (int) Math.floor(elapsed.toSeconds());
    int elapsedHours = intElapsed / (60 * 60);
    if (elapsedHours > 0) {
      intElapsed -= elapsedHours * 60 * 60;
    }
    int elapsedMinutes = intElapsed / 60;
    int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 -
            elapsedMinutes * 60;

    if (duration.greaterThan(Duration.ZERO)) {
      int intDuration = (int) Math.floor(duration.toSeconds());
      int durationHours = intDuration / (60 * 60);
      if (durationHours > 0) {
        intDuration -= durationHours * 60 * 60;
      }
      int durationMinutes = intDuration / 60;
      int durationSeconds = intDuration - durationHours * 60 * 60 -
              durationMinutes * 60;

      if (durationHours > 0) {
        return String.format("%d:%02d:%02d/%d:%02d:%02d",
                elapsedHours, elapsedMinutes, elapsedSeconds,
                durationHours, durationMinutes, durationSeconds);
      } else {
        return String.format("%02d:%02d/%02d:%02d",
                elapsedMinutes, elapsedSeconds,
                durationMinutes, durationSeconds);
      }
    } else {
      if (elapsedHours > 0) {
        return String.format("%d:%02d:%02d",
                elapsedHours, elapsedMinutes, elapsedSeconds);
      } else {
        return String.format("%02d:%02d",
                elapsedMinutes, elapsedSeconds);
      }
    }
  }

  public void stop() {
    mp.stop();
  }
}