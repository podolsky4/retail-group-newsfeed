package com.impltech.model;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

import javax.json.JsonObject;

/**
 * Observable text style
 */
public class TextStyle {

  private final ReadOnlyIntegerProperty fontSize;
  private final ReadOnlyIntegerProperty length;

  public TextStyle(JsonObject jsonObject) {
    this.fontSize = new ReadOnlyIntegerWrapper(this, "fontSize", jsonObject.getInt("fontSize"));
    this.length = new ReadOnlyIntegerWrapper(this, "length", jsonObject.getInt("length"));
  }

  public int getFontSize() {
    return fontSize.get();
  }

  public ReadOnlyIntegerProperty fontSizeProperty() {
    return fontSize;
  }

  public int getLength() {
    return length.get();
  }

  public ReadOnlyIntegerProperty lengthProperty() {
    return length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TextStyle textStyle = (TextStyle) o;

    if (fontSize != null ? !fontSize.equals(textStyle.fontSize) : textStyle.fontSize != null) return false;
    return length != null ? length.equals(textStyle.length) : textStyle.length == null;
  }

  @Override
  public int hashCode() {
    int result = fontSize != null ? fontSize.hashCode() : 0;
    result = 31 * result + (length != null ? length.hashCode() : 0);
    return result;
  }
}
