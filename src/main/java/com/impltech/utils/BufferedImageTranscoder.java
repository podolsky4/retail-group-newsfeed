package com.impltech.utils;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.image.BufferedImage;

class BufferedImageTranscoder extends ImageTranscoder {

  private BufferedImage img;

  @Override
  public BufferedImage createImage(int width, int height) {
    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }

  @Override
  public void writeImage(BufferedImage img, TranscoderOutput to) {
    this.img = img;
  }

  BufferedImage getBufferedImage() {
    return img;
  }
}
