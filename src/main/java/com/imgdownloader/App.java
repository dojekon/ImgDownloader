package com.imgdownloader;

import java.util.Set;

public class App {

  public static void main(String[] args) {
    ImgDownloader downloader = new ImgDownloader("https://vk.com/", null, null);
    Set<String> links = downloader.getImgLinks(downloader.getChildLinksFPage(downloader.getUrl()));
    for (String string : links) {
      System.out.println(string);
    }
  }
}
