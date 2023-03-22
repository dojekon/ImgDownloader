package com.imgdownloader;

import java.util.Set;

public class App {

  public static void main(String[] args) {
    ImgDownloader downloader = new ImgDownloader("https://vk.com/", null, null);
    Set<String> links = downloader.getImgLinks("https://browsershots.org/");
    for (String string : links) {
      System.out.println(string);
    }
  }
}
