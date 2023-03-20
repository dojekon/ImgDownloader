package com.imgdownloader;

public class ImgDownloader {
    String url;
    String savePath;
    String imgMinSize;

    public ImgDownloader(String url, String savePath, String imgMinSize) {
        this.url = url;
        this.savePath = savePath;
        this.imgMinSize = imgMinSize;
    }
}
