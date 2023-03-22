package com.imgdownloader;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;


public class ImgDownloader {
    private String url;
    private String savePath;
    private String imgMinSize;
/** */
    public ImgDownloader(String url, String savePath, String imgMinSize) {
        this.url = url;
        this.savePath = savePath;
        this.imgMinSize = imgMinSize;
    }

    String getUrl() { return this.url; };
    String getSavePath() { return this.savePath; }
    String getImgMinSizeS() { return this.imgMinSize; }

    /**
     * Получает список элементов на странице по заданному тегу элемента
     * @param url Ссылка на веб-страницу
     * @param tag Название тега, экземляры которого необходимо собрать
     * @return {@link Elements} - список элементов, отвечающих параметрам
    **/
    public List<Element> getPageElementsByTag(String url, String tag) {
        try {
            return Jsoup.connect(url).get().select(tag);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Метод получения ссылок на дочерние страницы
     * @param url адрес страницы
     * @return {@link Set} список ссылок
     */
    public Set<String> getChildLinksFPage(String url) {
        Set<String> links = new HashSet<String>();
        List<Element> elements = getPageElementsByTag(url, "a");

        for (Element element : elements) {
            if (!element.attr("abs:href").toString().equals(url) && !element.attr("abs:href").toString().equals("")) {
            links.add(element.attr("abs:href").toString());
            }
        }

        return links;
    }

    /**
     * Метод получения ссылок на изображения со списка веб-страниц
     * @param urls {@link Set} список веб-страниц
     * @return {@link Set} список ссылок на изображения с веб-страниц
     */
    public Set<String> getImgLinks(Set<String> urls) {
        Set<String> imgLinks = new HashSet<String>();     

        for (String url : urls) {
                imgLinks.addAll(getImgLinks(url)); // Вызов перегруженного метода
        }
        return imgLinks;
    }
    
    /**
     * Метод получения ссылок на изображения c одной веб-страницы
     * @param url ссылка на страницу
     * @return {@link Set} список ссылок на изображения с веб-страницы
     */
    public Set<String> getImgLinks(String url) {
        Set<String> imgLinks = new HashSet<String>();     

        List<Element> elements = getPageElementsByTag(url, "img");
        for (Element element : elements) {
            imgLinks.add(element.attr("abs:src").toString());
        }

        return imgLinks;
    }
}
