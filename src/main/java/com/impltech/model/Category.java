package com.impltech.model;

import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * <p>Class: Category</p>
 * <p>Description: Represents category</p>
 * <p>Created: 30.10.18</p>
 *
 * @author Platon Tsybulskiy
 */
public class Category {

    private String id;
    private String name;

    public Category(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public Category(JsonValue jsonValue) {
        JsonObject jsonObject = (JsonObject) jsonValue;
        this.id = jsonObject.getString("_id");
        this.name = capitalize(jsonObject.getString("name"));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Category category = (Category) o;

    if (id != null ? !id.equals(category.id) : category.id != null) return false;
    return name != null ? name.equals(category.name) : category.name == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }

  private String capitalize(String original) {
    if (original == null || original.length() == 0) {
      return original;
    }
    return original.substring(0, 1).toUpperCase() + original.substring(1);
  }
}
