package com.imgdownloader;

import java.io.IOException;
import java.util.List;

import org.jsoup.nodes.Element;

public class App {

  public static void main(String[] args) throws IOException {
    ImgDownloader downloader = new ImgDownloader("https://nextcloud.dojekon.ru/", null, null);
    List<Element> URLs = downloader.getPageElementsByTag(downloader.getUrl(), "a");
    for (Element element : URLs) {
      System.out.println(element.attr("href"));
    }
  }
}
