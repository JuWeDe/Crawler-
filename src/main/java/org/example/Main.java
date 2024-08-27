package org.example;


import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {

    private static int MAX_DEPTH = 0;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);

    public static void main(String[] args) {
//        String startUrl = "https://example.com";
        Scanner sc = new Scanner(System.in);

//        MAX_DEPTH = Integer.parseInt(sc.nextLine());
        String startUrl = "https://linuxmasterclub.ru/nomacs/";
        crawl(startUrl, 0, new HashSet<>(), Paths.get("crawled_data"));
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void crawl(String url, int depth, Set<String> visitedUrls, java.nio.file.Path outputDir) {
        if (depth > MAX_DEPTH) {
            return;
        }

        if (!visitedUrls.add(url)) {
            return;
        }

        try {
            Document doc = Jsoup.connect(url).get();
            System.out.println("Crawling: " + url);

            String textContent = doc.body().text();
            File textFile = new File(outputDir.toFile(), getFileName(url, ".txt"));
            FileUtils.writeStringToFile(textFile, textContent, "UTF-8");

            saveMediaContent(doc, outputDir);


            List<String> links = getLinks(doc);
            for (String link : links) {
                crawl(link, depth + 1, visitedUrls, outputDir);
            }
        } catch (IOException e) {
//            System.err.println("Error crawling " + url + ": " + e.getMessage());
        }
    }



    private static List<Future<?>> saveMediaContent(Document doc, Path outputDir) {
        List<Future<?>> futures = Lists.newArrayList();

        Elements imgElements = doc.select("img");
        futures.addAll(saveMediaFiles(imgElements, "img", outputDir));

        Elements videoElements = doc.select("video");
        futures.addAll(saveMediaFiles(videoElements, "video", outputDir));

        return futures;
    }

    private static List<Future<?>> saveMediaFiles(Elements elements, String type, Path outputDir) {
        List<Future<?>> futures = Lists.newArrayList();
        for (Element element : elements) {
            String url = element.attr("src");
            String fileName = getFileName(url, "." + type);
            Path filePath = Paths.get(outputDir.toString(), fileName);
            if (!Files.exists(filePath)) {
                Future<?> future = executorService.submit(() -> {
                    try (InputStream in = new URL(url).openStream()) {
                        Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Downloaded " + type + ": " + filePath);
                    } catch (IOException e) {

// экспешон
//                    System.err.println("Error downloading " + type + ": " + url);
                }
            });
            futures.add(future);
        }
    }
        return futures;
}

//    private static void saveMediaContent(Document doc, java.nio.file.Path outputDir) {
//        Elements imgElements = doc.select("img");
//        saveMediaFiles(imgElements, "img", outputDir);
//
//
//        Elements videoElements = doc.select("video");
//        saveMediaFiles(videoElements, "video", outputDir);
//    }
//    private static void saveMediaFiles(Elements elements, String type, java.nio.file.Path outputDir) {
//        for (Element element : elements) {
//            String url = element.attr("src");
//            String fileName = getFileName(url, "." + type);
//            File file = new File(outputDir.toFile(), fileName);
//            if (!file.exists()) {
//                executorService.submit(() -> {
//                    try (InputStream in = new URL(url).openStream()) {
//                        FileUtils.copyInputStreamToFile(in, file);
//                        System.out.println("Downloaded " + type + ": " + file.getAbsolutePath());
//                    } catch (IOException e) {
//                        System.err.println("Error downloading " + type + ": " + url);
//                    }
//                });
//            }
//        }
//    }

    private static List<String> getLinks(Document doc) {
        Elements linkElements = doc.select("a[href]");
        List<String> links = Lists.newArrayList();
        for (Element linkElement : linkElements) {
            String linkUrl = linkElement.attr("abs:href");
            links.add(linkUrl);
        }
        return links;
    }

    private static String getFileName(String url, String extension) {
        int lastSlashIndex = url.lastIndexOf("/");
        String fileName = url.substring(lastSlashIndex + 1);
        if (fileName.isEmpty()) {
            fileName = "index";
        }
        return fileName + extension;
    }
}