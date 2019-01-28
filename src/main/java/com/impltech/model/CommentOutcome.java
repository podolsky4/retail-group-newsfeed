package com.impltech.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "comment")
@XmlAccessorType(XmlAccessType.FIELD)
public class CommentOutcome {

  private String body;
  private Boolean confirmation = false;

  public CommentOutcome() { }

  public CommentOutcome(String body) {
    this.body = body;
  }

  public CommentOutcome(String body, Boolean confirmation) {
    this.body = body;
    this.confirmation = confirmation;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Boolean getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(Boolean confirmation) {
    this.confirmation = confirmation;
  }

}
