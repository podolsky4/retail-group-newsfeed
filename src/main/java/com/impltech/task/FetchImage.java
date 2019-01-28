package com.impltech.task;

import com.impltech.utils.Base64Decoder;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

/**
 * The task for fetching an image to place at individual ImageView
 */
public class FetchImage extends Task<Image> {

  /**
   * Reference to an image url
   */
  private final String imageUrl;

  public FetchImage(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Override
  protected Image call() {
    return Base64Decoder.decode(imageUrl);
  }

}
