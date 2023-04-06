package com.imgdownloader;


public class App {

  public static void main(String[] args) {
    ImgDownloader downloader = new ImgDownloader("https://mail.ru/", "C:/Users/dojekon/Documents/GitHub/img", 0);
     // Вывод статистики каждую секунду
    downloader.getAndDownloadImagesMultithread();


  }
}
