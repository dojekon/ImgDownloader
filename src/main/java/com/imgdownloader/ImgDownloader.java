package com.imgdownloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.*;


public class ImgDownloader {
    private String url;
    private String savePath;
    private String imgMinSize;

    public ImgDownloader(String url, String savePath, String imgMinSize) {
        this.url = url;
        this.savePath = savePath;
        this.imgMinSize = imgMinSize;
    }

    String getUrl() { return this.url; };
    String getSavePath() { return this.savePath; }
    String getImgMinSizeS() { return this.imgMinSize; }

    public List<Element> getPageElementsByTag(String url, String tag) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.select(tag);
    }
}
