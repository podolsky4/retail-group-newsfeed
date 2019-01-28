package com.impltech.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

public class MyResourceBundle {

  // feature variables
  private ResourceBundle bundle;
  private String fileEncoding;

  public MyResourceBundle(Locale locale, String fileEncoding){
    this.bundle = ResourceBundle.getBundle("bundles.RGN", locale);
    this.fileEncoding = fileEncoding;
  }

  public MyResourceBundle(Locale locale){
    this(locale, "UTF-8");
  }

  public String getString(String key){
    String value = bundle.getString(key);
    try {
      return new String(value.getBytes(StandardCharsets.ISO_8859_1), fileEncoding);
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }
}
