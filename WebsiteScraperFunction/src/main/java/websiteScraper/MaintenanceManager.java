package websiteScraper;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MaintenanceManager {
    private static String s3Bucket = ConfigManager.S3_BUCKET;
    private static String objectKey = ConfigManager.DB_KEY;
    public static void main(String[] args) {
        //scrapeVideoLinkTest1();
        //scrapeVideoLinkTest2();
        //fixVideoLinks();
        deleteRecords();
    }
    public static void scrapeVideoLinkTest1() {
        System.out.println("[scrapeVideoLinkTest1]");
        WebsiteScraper scraper = new FootballOrginScraper();
        String[] testUrls = {"https://www.footballorgin.com/bundesliga-highlights-show-25-may-2020/"
                , "https://www.footballorgin.com/bundesliga-highlights-show-25-may-2020/2/"};
        for (String url: testUrls) {
            String testUrlHtml = HttpManager.getHtmlFromUrl(url);
            String videoLink = scraper.scrapeVideoLink(testUrlHtml, "xxx");
            List<String> errors = scraper.getErrorLog();
            System.out.printf("\nerrors: [%s]\nurl: [%s] \nvideoLink: [%s] \n", errors, url, videoLink);
        }
    }
    public static void scrapeVideoLinkTest2() {
        System.out.println("[scrapeVideoLinkTest2]");
        WebsiteScraper scraper = new FootballOrginScraper();
        String testUrl = "https://www.footballorgin.com/borussia-dortmund-vs-bayern-munich-full-match-bundesliga-26-may-2020/2/";
        String testUrlHtml = HttpManager.getHtmlFromUrl(testUrl);
        String videoLink = scraper.scrapeVideoLink(testUrlHtml, "xxx");
        List<String> errors = scraper.getErrorLog();
        System.out.printf("\nerrors: [%s]\nurl: [%s] \nvideoLink: [%s] \n", errors, testUrl, videoLink);
    }
    public static void fixVideoLinks() {
        List<Map<String, String>> articles = LegacyIntegration.loadArticlesFromS3(s3Bucket, objectKey);
        System.out.println("articles.size(): " + articles.size());
        Iterator it = articles.iterator();
        while(it.hasNext()) {
            Map<String, String> article = (Map<String, String>)it.next();
            String videoLinks = article.get("matchVideoLinks");
            if(videoLinks.indexOf(".jpg") != -1 || videoLinks.indexOf(".png") != -1) {
                String articleKey = article.get("articleKey");
                System.out.printf("Removing articleKey: [%s] \n", articleKey);
                it.remove();
            }
            //Instant articleDate = Instant.parse(article.get("scrapedDate"));
            //Instant searchDate = Instant.parse("2020-05-27T00:00:00.000Z");
            //if(articleDate.compareTo(searchDate) > 0) {
            //    String articleKey = article.get("articleKey");
            //    System.out.printf("articleKey: [%s] \n", articleKey);
            //    System.out.printf("videoLinks: [%s] \n", videoLinks);
            //}
        }
        System.out.println("articles.size(): " + articles.size());
        //LegacyIntegration.saveArticlesToS3(s3Bucket, objectKey, articles);
    }
    public static void deleteRecords() {
        List<Map<String, String>> articles = LegacyIntegration.loadArticlesFromS3(s3Bucket, objectKey);
        System.out.println("articles.size(): " + articles.size());
        Iterator it = articles.iterator();
        while(it.hasNext()) {
            Map<String, String> article = (Map<String, String>)it.next();
            Instant articleDate = Instant.parse(article.get("scrapedDate"));
            Instant searchDate = Instant.parse("2020-06-15T00:00:00.000Z");
            if(articleDate.compareTo(searchDate) > 0) {
                String articleKey = article.get("articleKey");
                System.out.printf("articleKey: [%s] \n", articleKey);
                //it.remove();
            }
        }
        System.out.println("articles.size(): " + articles.size());
        //LegacyIntegration.saveArticlesToS3(s3Bucket, objectKey, articles);
    }
}
