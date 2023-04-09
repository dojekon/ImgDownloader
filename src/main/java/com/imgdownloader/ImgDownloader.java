package com.imgdownloader;

import java.io.File;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import java.io.FileInputStream;
import java.util.Properties;

public class ImgDownloader {
    private static Properties loadProperties(String filename) throws IOException {
        Properties properties = new Properties();
        FileInputStream inputStream = new FileInputStream(filename);
        properties.load(inputStream);
        inputStream.close();
        return properties;
    }
    
    private String url;
    private String savePath;
    private int imgMinSize;
    private final AtomicInteger downloadedImagesCounter = new AtomicInteger(0);
    private final AtomicReference<String> currentImageURL = new AtomicReference<>("");

    public ImgDownloader(String url, String savePath, int imgMinSizeKB) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        if (savePath == null || savePath.isEmpty()) {
            throw new IllegalArgumentException("savePath cannot be empty");
        }
        this.url = url;
        this.savePath = savePath;
        this.imgMinSize = imgMinSizeKB * 1024;
    }

    public ImgDownloader(String propertiesPath) {
        try {
            Properties properties = loadProperties(propertiesPath);
            String url = properties.getProperty("url");
            String savePath = properties.getProperty("savePath");
            int imgMinSizeKB = Integer.parseInt(properties.getProperty("imgMinSizeKB"));
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("URL cannot be empty");
            }
            if (savePath == null || savePath.isEmpty()) {
                throw new IllegalArgumentException("savePath cannot be empty");
            }
            this.url = url;
            this.savePath = savePath;
            this.imgMinSize = imgMinSizeKB * 1024;
        } catch (Exception e) {}
    }

    String getUrl() {
        return this.url;
    };

    String getSavePath() {
        return this.savePath;
    }

    int getImgMinSizeS() {
        return this.imgMinSize;
    }

    /**
     * Получает список элементов на странице по заданному тегу элемента
     * 
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
        // TODO Make abs:href obj -- DONE
        String attr = "abs:href";
        for (Element element : elements) {

            if (!element.attr(attr).toString().equals(url) && !element.attr(attr).toString().equals("")) {
                links.add(element.attr(attr).toString());
            }
        }

        return links;
    }

    /**
     * Метод получения ссылок на изображения со списка веб-страниц
     * 
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
     * 
     * @param url ссылка на страницу
     * @return {@link Set} список ссылок на изображения с веб-страницы
     */
    public Set<String> getImgLinks(String url) {
        Set<String> imgLinks = new HashSet<String>();
        String attr = "abs:src";
        List<Element> elements = getPageElementsByTag(url, "img");
        for (Element element : elements) {
            imgLinks.add(element.attr(attr).toString());
        }

        return imgLinks;
    }

    /***
     * Метод скачивания картинки по ссылке из агрумента в директорию из поля класса
     * 
     * @param imageUrl {@link String} ссылка на изображение
     */
    public void downloadImg(String imageUrl) {
        // Проверка размера изображения
        if (!isImageSizeValid(imageUrl)) {
            return;
        }
        // Обновляем информацию о текущей картинке
        currentImageURL.set(imageUrl);
                   
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
            FileOutputStream fos = new FileOutputStream(this.getSavePath() + "/" + filename);
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

             // Увеличиваем количество скачанных изображений
             downloadedImagesCounter.incrementAndGet();

            is.close();
            fos.close();

        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    /***
     * Скачать несколько изображений
     * @param imageUrls {@link Set} список ссылок на изображения
     */
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

        // Ожидайте завершения всех задач на скачивание или прерывания по таймауту,
        // например, 10 минут
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

    /***
     * Получить ссылки на дочерние страницы и скачать изображения в однопоточном
     * режиме
     */
    public void getAndDownloadImages() {
        // Очищаем директорию перед скачиванием
        try {
            FileUtils.cleanDirectory(new File(this.getSavePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> siteLinks = getChildLinksFPage(this.url);
        siteLinks.add(this.getUrl());
        downloadImg(getImgLinks(siteLinks));
    }

    /***
     * Получить ссылки на дочерние страницы и скачать изображения в многопоточном
     * режиме
     */
    public void getAndDownloadImagesMultithread() {
        // Очищаем директорию перед скачиванием
        try {
            FileUtils.cleanDirectory(new File(this.getSavePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        downloadedImagesCounter.set(0);
        currentImageURL.set("");
        Set<String> siteLinks = getChildLinksFPage(this.url);
        siteLinks.add(this.getUrl());
        this.startStatisticsThread(1000);
        downloadImgMultithread(getImgLinks(siteLinks));
    }

    /***
     * Метод проверки размера скачиваемого изображения
     * 
     * @param imageUrl {@link String} ссылка на изображение
     * @return {@link bool} 
     */
    private boolean isImageSizeValid(String imageUrl) {
        try {   
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            int imageSize = connection.getContentLength(); // Размер изображения в байтах
            return imageSize >= imgMinSize; // Возвращаем true, если размер изображения больше или равен минимальному
                                            // размеру
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /***
     * Метод, инициализирующий поток статистики работы программы
     * @param intervalMs интервал вывода статистики в мс
     */
    public void startStatisticsThread(int intervalMs) {
        // Создайте ScheduledExecutorService с одним потоком
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        // Запустите задачу с заданным интервалом (intervalMs)
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println("Текущее изображение: " + currentImageURL.get());
            System.out.println("Всего скачано изображений: " + downloadedImagesCounter.get());
        }, 0, intervalMs, TimeUnit.MILLISECONDS);

        // Остановите ScheduledExecutorService после завершения работы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduledExecutorService.shutdown();
        }));
    }

}
