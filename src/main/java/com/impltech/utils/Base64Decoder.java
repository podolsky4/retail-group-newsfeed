package com.impltech.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Decoder for Base64 encoding.
 */
public class Base64Decoder {
  /**
   * Decode image by URL
   * @param imageBody
   * Image URL
   * @return
   * Buffered image
   */
  public static Image decode(String imageBody) {

    BufferedImage bufferedImage;
    try {
      URL imageUrl = new URL(imageBody);
      BufferedInputStream bis = new BufferedInputStream(imageUrl.openConnection().getInputStream());
      bufferedImage = ImageIO.read(bis);
      bis.close();
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }

    return SwingFXUtils.toFXImage(bufferedImage, null);
  }
}

