package com.imgdownloader;

import org.apache.commons.lang3.time.StopWatch;

public class App {

  public static void main(String[] args) {
    ImgDownloader downloader = new ImgDownloader("C:/Users/dojekon/Documents/GitHub/ImgDownloader/settings.property");
    StopWatch stopWatch = new StopWatch();

    stopWatch.start();
    downloader.getAndDownloadImages();
    stopWatch.stop();
    System.out.println("Прошло времени, с: " + stopWatch.getTime()/1000);

    stopWatch.reset();
    stopWatch.start();
    downloader.getAndDownloadImagesMultithread();
    stopWatch.stop();
    System.out.println("Прошло времени, с: " + stopWatch.getTime()/1000);
    System.exit(0);
  }
}
