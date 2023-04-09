package com.imgdownloader;

import java.util.*;

import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImgdownloaderTest {
    public ImgdownloaderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetPageElementsByTag() {
        String url = "https://www.example.com";
        String savePath = "C:/Users/dojekon/Documents/GitHub/img";
        String tag = "p";
        
        ImgDownloader downloader = new ImgDownloader(url, savePath, 0);
        
        List<Element> elements = downloader.getPageElementsByTag(url, tag);
        
        assertNotNull(elements); // проверяем, что результат не равен null
        assertTrue(elements.size() > 0); // проверяем, что список не пустой
        assertEquals(tag, elements.get(0).tagName()); //проверяем, что данные элементы имеют тег 'p'
    }

    @Test
    public void testGetPageLinks() {
        String url = "https://vk.com";
        String savePath = "C:/Users/dojekon/Documents/GitHub/img";
        ImgDownloader downloader = new ImgDownloader(url, savePath, 0);

        Set<String> links = downloader.getChildLinksFPage(downloader.getUrl());

        assertNotNull(links); // Проверяем, что не null
        assertTrue(links.size() > 0); // Проверяем, что количество записей больше 0
        for (String string : links) {
            assertTrue((string.contains("http://") || string.contains("https://"))); // Проверяем, что содержимое действительно ссылки
        }
    }

    @Test
    public void testGetImgLinks() {
        String url = "https://browsershots.org/";
        String savePath = "C:/Users/dojekon/Documents/GitHub/img";
        
        Set<String> expectedLinks = new HashSet<String>();
        expectedLinks.add("https://browsershots.org/static/images/logo/header_hover.png");
        expectedLinks.add("https://browsershots.org/static/images/logo/header_name.png");
        expectedLinks.add("https://browsershots.org/static/images/information.png");
        expectedLinks.add("https://browsershots.org/static/images/logo/header.png");
        expectedLinks.add("https://browsershots.org/static/images/cross_grey_small.gif");

        ImgDownloader downloader = new ImgDownloader(url, savePath, 0);
        Set<String> actualLinks = downloader.getImgLinks(url);
        assertEquals(expectedLinks, actualLinks);
    }
}
