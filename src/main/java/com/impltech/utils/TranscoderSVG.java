package com.impltech.utils;

import com.impltech.Main;
import com.impltech.controller.FilterArticleController;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transcode SVG to FX Image
 */
public class TranscoderSVG {

  public static void transcode(String pathSvg, Button button) {
    BufferedImageTranscoder biTranscoder = new BufferedImageTranscoder();
    try (InputStream file = Main.class.getResourceAsStream(pathSvg)) {
      TranscoderInput transIn = new TranscoderInput(file);
      try {
        biTranscoder.transcode(transIn, null);
        Image img = SwingFXUtils.toFXImage(biTranscoder.getBufferedImage(), null);
        button.setGraphic(new ImageView(img));
      } catch (TranscoderException ex) {
        Logger.getLogger(FilterArticleController.class.getName()).log(Level.SEVERE, null, ex);
      }
    } catch (IOException io) {
      Logger.getLogger(FilterArticleController.class.getName()).log(Level.SEVERE, null, io);
    }
  }
}
