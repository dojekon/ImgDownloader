package com.imgdownloader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;




public class ImgDownloader {
    private String url;
    private String savePath;
    private int imgMinSize;
    private final AtomicInteger downloadedImages = new AtomicInteger(0);
    private final AtomicReference<String> currentImage = new AtomicReference<>("");

/** */
    public ImgDownloader(String url, String savePath, int imgMinSizeKB) {
      /* if (url == null || url.isEmpty()) {
        throw new IllegalArgumentException("URL cannot be empty");
       } */
       /*if (savePath == null || savePath.isEmpty()) {
        throw new IllegalArgumentException("savePath cannot be empty"); 
       }*/
       this.url = url;
        this.savePath = savePath;
        this.imgMinSize = imgMinSizeKB * 1024;
    }

    String getUrl() { return this.url; };
    String getSavePath() { return this.savePath; }
    int getImgMinSizeS() { return this.imgMinSize; }

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
            // TODO Make abs:href obj
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

    public void downloadImg(String imageUrl) { 
        if (!isImageSizeValid(imageUrl)) {
            return;
        }     

        // Обновляем информацию о текущей картинке
        currentImage.set(imageUrl);
        try {
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();
            byte[] buffer = new byte[4096];
            int length;
            // Создаем файл на диске и записываем в него изображение
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            // Обрезаем аргументы
            if (filename.contains("?")) {
                filename = filename.substring(0, filename.indexOf(("?")));
            }
            FileOutputStream fos = new FileOutputStream(this.getSavePath() + "/" + filename
            );
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            is.close();
            fos.close();

            // Увеличиваем количество скачанных изображений
             downloadedImages.incrementAndGet();
            
        } catch (IOException e) {
          //  e.printStackTrace();
        }
    }

    public void downloadImg(Set<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            downloadImg(imageUrl);
        }
    }

    public void downloadImgMultithread(Set<String> imageUrls) {
        // Создайте пул потоков с фиксированным количеством потоков, например, 10
        ExecutorService executorService = Executors.newFixedThreadPool(10);
    
        // Отправьте задачи на скачивание изображений в пул потоков
        for (String imageUrl : imageUrls) {
            executorService.submit(() -> downloadImg(imageUrl));
        }
    
        // Завершите работу пула потоков после отправки всех задач на скачивание
        executorService.shutdown();
    
        // Ожидайте завершения всех задач на скачивание или прерывания по таймауту, например, 10 минут
        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                // Если не все задачи успели завершиться, прерываем оставшиеся задачи
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Если ожидание прервано, прерываем оставшиеся задачи
            executorService.shutdownNow();
        }
    }
    

    public void getAndDownloadImages() {
        Set<String> siteLinks = getChildLinksFPage(this.url);
        siteLinks.add(this.getUrl());
        downloadImg(getImgLinks(siteLinks));
    }

    public void getAndDownloadImagesMultithread() {
        Set<String> siteLinks = getChildLinksFPage(this.url);
        siteLinks.add(this.getUrl());
        this.startStatisticsThread(1000);
        downloadImgMultithread(getImgLinks(siteLinks));
    }
    /***
     * Метод проверки размера скачиваемого изображения
     * @param imageUrl
     * @return
     */
    private boolean isImageSizeValid(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            int imageSize = connection.getContentLength(); // Размер изображения в байтах
            return imageSize >= imgMinSize; // Возвращаем true, если размер изображения больше или равен минимальному размеру
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void startStatisticsThread(int intervalMs) {
        // Создайте ScheduledExecutorService с одним потоком
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    
        // Запустите задачу с заданным интервалом (intervalMs)
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println("Текущее изображение: " + currentImage.get());
            System.out.println("Всего скачано изображений: " + downloadedImages.get());
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    
        // Остановите ScheduledExecutorService после завершения работы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduledExecutorService.shutdown();
        }));
    }
    
}
