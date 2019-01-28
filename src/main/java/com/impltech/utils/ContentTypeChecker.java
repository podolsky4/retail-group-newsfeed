package com.impltech.utils;

import com.impltech.model.Article;

/**
 * Check content type of the article
 */
public class ContentTypeChecker {

  public static String check(Article article) {
    return (article.getType() == Article.Type.videoType) ? article.getScreenshot() : article.getImgSmallUrl();
  }
}
