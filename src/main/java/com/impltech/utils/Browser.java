package com.impltech.utils;

import com.sun.javafx.scene.NodeEventDispatcher;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.event.EventDispatcher;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents embedded browser in the scene.
 *
 * @author Yuri Podolsky, 28-08-18
 */
public class Browser extends Region {
  private static final Logger logger = Logger.getLogger(Browser.class.getName());

  private final WebView webview = new WebView();
  private final WebEngine webEngine = webview.getEngine();

  public Browser(String content, int fontSize, String fontMetric, String backgroundColor, ScrollPane scrollPane) {

    String CSS = String.format("data: ,body { font: %d%s Roboto; background-color: %s; color: #333; margin: 0; padding: 0;}",
            fontSize, fontMetric, backgroundColor);

    webEngine.setUserStyleSheetLocation(CSS);
    webview.setFontSmoothingType(FontSmoothingType.GRAY);
    webview.setContextMenuEnabled(false);
    webview.setPrefWidth(550);
    webview.setMaxWidth(550);
    webview.setFocusTraversable(false);
//    webview.setDisable(true);

    EventDispatcher dispatcher = webview.getEventDispatcher();
    this.setPadding(new Insets(15, 0, 15, 0));
//    ((NodeEventDispatcher) dispatcher).eventHandlerManager.eventHandlerMap.remove("SCROLL")
//    ;
//    ((NodeEventDispatcher) dispatcher).getEventHandlerManager().removeEventHandler(ScrollEvent.SCROLL, );
    ((NodeEventDispatcher) dispatcher).getEventHandlerManager().addEventFilter(ScrollEvent.SCROLL, event -> {
      double deltaY = event.getDeltaY() / 4;
      double width = scrollPane.getContent().getBoundsInLocal().getWidth();
      double x = scrollPane.getVmax();
      double vvalue = scrollPane.getVvalue();
      scrollPane.setVvalue(vvalue + -deltaY/width);
    });




    webview.getEngine().getLoadWorker().stateProperty().addListener((arg0, oldState, newState) -> {
      if (newState == State.SUCCEEDED) {
        logger.info("Article body is loaded!");
        adjustHeight();
//        rty(content);
      }
    });

    /*
      Youtube link on desktop browers
     */
    webview.getEngine().setCreatePopupHandler(new BrowserPopupHandler());



    /*
      Hide scrollbar in the javafx WebView

      http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview

     */
    webview.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
      Set<Node> scrolls = webview.lookupAll(".scroll-bar");
      for (Node scroll : scrolls) {
        scroll.setVisible(false);
        scroll.setDisable(false);
      }
    });

    setContent(content);
    getChildren().add(webview);
  }

  private void setContent(final String content) {
    Platform.runLater(() -> {
      webEngine.loadContent(html(content));
      adjustHeight();
      rty(content);
    });
  }

  @Override
  protected void layoutChildren() {
    double w = getWidth();
    double h = getHeight();
    layoutInArea(webview, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
  }

  /**
   * Adjust browser height
   *
   */
  private void adjustHeight() {
    Platform.runLater(() -> {
      try {
        Object result = webEngine.executeScript("document.getElementById('content').offsetHeight");

        if (result instanceof Integer) {
          Integer i = (Integer) result;
          double height = new Double(i);
          webview.setPrefHeight(height);
        }
      } catch (JSException e) {
        // not important
      }
    });

  }


  /**
   * Returns the vertical scrollbar of the webview.
   *
   * @return vertical scrollbar of the webview or {@code null} if no vertical
   * scrollbar exists
   */
  private ScrollBar getVScrollBar() {

    Set<Node> scrolls = webview.lookupAll(".scroll-bar");
    for (Node scrollNode : scrolls) {

      if (scrollNode instanceof ScrollBar) {
        ScrollBar scroll = (ScrollBar) scrollNode;
        if (scroll.getOrientation() == Orientation.VERTICAL) {
          return scroll;
        }
      }
    }
    return null;
  }

  /**
   * Get HTML from the browser
   *
   * @param content
   * Article content
   * @return
   * Embedded content in html
   *
   */
  private String html(String content) {
//    System.out.println(content);
//    System.out.println((Jsoup.parse(content)).body().getElementsByTag("iframe").attr("src"));
//    String attr = (Jsoup.parse(content)).body().getElementsByTag("iframe").attr("src");
//
//    System.out.println((Jsoup.parse(content)).body().getElementsByTag("iframe").attr("src",attr));
//
//    System.out.println(attr.replaceAll("showinfo=0", "controls=0"));


    return "<!DOCTYPE html><html lang=\"ru-RU\"><head><meta charset=utf-8><style> * { box-sizing: border-box; }" +
            "img { width: 100%; height: auto; user-drag: none;\n" +
            "user-select: none;\n" +
            "-moz-user-select: none;\n" +
            "-webkit-user-drag: none;\n" +
            "-webkit-user-select: none;\n" +
            "-ms-user-select: none;} video, iframe { width: 100%; height: 309px; }</style></head><body>" +
            "<div id=\"content\">" + content + "</div>" +
            "</body></html>";
  }
  void rty(String content) {
  final String url = (Jsoup.parse(content)).body().getElementsByTag("iframe").attr("src");
  final Hyperlink hyperlink = new Hyperlink();
  hyperlink.setOnAction(event -> new Thread(() -> {
    try {
      Desktop.getDesktop().browse(new URI(url));
    } catch (IOException | URISyntaxException e1) {
      e1.printStackTrace();
    }
  }).start());
}

}