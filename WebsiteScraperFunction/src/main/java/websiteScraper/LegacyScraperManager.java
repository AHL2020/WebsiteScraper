package websiteScraper;

import com.amazonaws.regions.Regions;
import legacy.DataUtils;
import legacy.MatchBean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LegacyScraperManager {
    public static void main(String[] args) {
        S3Manager.DeploymentType type = S3Manager.DeploymentType.LOCAL;
        Regions region = Regions.US_EAST_1;
        String bucketName = "my-sports-website";
        String databaseKey = "data/database.csv";
        run(type, region, bucketName, databaseKey);
    }
    public static void run(S3Manager.DeploymentType type, Regions region, String bucketName, String databaseKey) {
        System.out.println("[LSM][run] ---------------------------------");
        System.out.println("[LSM][run] START: LegacyScraperManager.run()");
        System.out.println("[LSM][run] ---------------------------------");
        // scrape data

        // todo: pass in a config file that lists all scrapers,
        // todo: then use Class.forName() to instantiate scrapers
        //WebsiteScraper scraper = new FullMatchesAndShowsScraper();
        WebsiteScraper scraper = new FootballOrginScraper();

        ScraperApp app = new ScraperApp(scraper);
        System.out.println("[LSM][run] Scraping data from: [" + scraper.getUrl() + "] ...");
        List<Map<String, String>> articles = app.run();
        System.out.printf("[LSM][run] Scraped articles: [%d] \n", articles.size());
        // load database from S3
        System.out.println("[LSM][run] ---------------------");
        System.out.println("[LSM][run] Load database from S3");
        System.out.println("[LSM][run] ---------------------");
        HashMap<String, MatchBean> database = DataUtils.loadMatchBeanObjects(bucketName, databaseKey);
        int dbSize1 = database.size();
        System.out.printf("[LSM][run] Loaded database: [%d] \n", dbSize1);

        System.out.println("[LSM][run] ------------------------------------");
        System.out.println("[LSM][run] Deleting invalid entries in database");
        System.out.println("[LSM][run] ------------------------------------");
        int dbRemoved = 0;
        // Get the iterator over the HashMap
        Iterator<Map.Entry<String,MatchBean>> iterator = database.entrySet().iterator();
        // Iterate over the HashMap
        while (iterator.hasNext()) {
            // Get the entry at this iteration
            Map.Entry<String,MatchBean> entry = iterator.next();
            // Check if this key is the required key
            MatchBean mb = entry.getValue();
            boolean condition1 = mb.getCompetition().equalsIgnoreCase("");
            boolean condition2 = mb.getDate().equalsIgnoreCase("");
            boolean condition3 = mb.getMatchKey().equalsIgnoreCase("");
            boolean condition4 = mb.getMatchKey().equalsIgnoreCase("null");
            if(condition1 || condition2 || condition3 || condition4) {
                iterator.remove();
                System.out.printf("[LSM][run] Deleted entry with key [%s] \n", mb.getMatchKey());
                dbRemoved++;
            }
        }
        System.out.println("[LSM][run] Deleting invalid entries COMPLETED. Deleted: [" + dbRemoved + "]");

        // process scraped articles
        System.out.println("[LSM][run] ------------------------");
        System.out.println("[LSM][run] Process scraped articles");
        System.out.println("[LSM][run] ------------------------");
        int processed = 0;
        int added = 0;
        int exists = 0;
        int error = 0;
        int invalid = 0;
        S3Manager s3Manager = S3Manager.getInstance(type, region);
        ValidationManager validationManager = ValidationManager.getInstance();
        for(int i = 0; i < articles.size(); i++) {
            processed++;
            Map<String, String> article = articles.get(i);

            String articleKey = article.get("articleKey");
            System.out.println("[LSM][run] Processing article: [" + article + "]");

            boolean isValid = validationManager.isValidArticle(article);
            System.out.println("[LSM][run] validationManager article isValid: " + isValid);
            System.out.println("[LSM][run] validationManager article log: " + validationManager.getLog());

            // todo: for testing only
            //System.out.println("--- next article -----------");
            //System.out.println(new PrettyPrintingMap<>(article));
            //System.out.println("----------------------------");

            if (!isValid) {
                //System.out.println("Article invalid: matchKey: [" + articleKey + "]");
                invalid++;
            } else {
                // if a new article has been scraped, insert to database
                // and also download image and save it to S3
                // todo: need a more detailed check whether an article exists
                // todo: f.ex. compare match key, home/away teams, dates, competition, etc...
                if (database.containsKey(articleKey)) {
                    // process existing article, e.g. update it
                    exists++;
                } else {
                    // process new article
                    System.out.printf("[LSM][run] Inserting articleKey: [%s] \n", articleKey);
                    String objectUrl = (article.get("articleImageUrl"));
                    String contentType = ContentTypeManager.getContentType(article.get("articleImageUrl"));
                    String objectKey = LegacyIntegration.getImageObjectKey(article);
                    System.out.printf("[LSM][run] Saving image objectKey: [%s] \n", objectKey);
                    // saving image to S3
                    boolean success = s3Manager.saveObjectFromUrl(objectUrl, contentType, bucketName, objectKey);
                    System.out.printf("[LSM][run] Saving image success: [%b] \n", success);
                    if (success) {
                        articles.get(i).put("articleImageUrl", objectKey);
                        added++;
                    } else {
                        articles.get(i).put("articleImageUrl", "images/default-match-image.jpg");
                        System.out.printf("[LSM][run] Error downloading image, using default image. \n");
                        error++;
                    }
                    MatchBean matchBean = LegacyIntegration.convertArticleToMatchBean(article);
                    database.put(articleKey, matchBean);
                    System.out.printf("[LSM][run] Added article/match to database. \n");
                }
            }
        }
        System.out.println("[LSM][run] Process scraped articles COMPLETED.");
        System.out.println("[LSM][run]    Processed:       [" + processed + "]");
        System.out.println("[LSM][run]    Added:           [" + added + "]");
        System.out.println("[LSM][run]    Existing:        [" + exists + "]");
        System.out.println("[LSM][run]    Error (image):   [" + error + "]");
        System.out.println("[LSM][run]    Invalid:         [" + invalid + "]");

        // save database to S3
        System.out.println("[LSM][run] -----------------------");
        System.out.println("[LSM][run] Save the database to S3");
        System.out.println("[LSM][run] -----------------------");
        int dbSize2 = database.size();
        if(dbSize2 >= dbSize1) {

            // FOR TESTING ONLY
            //System.out.printf("[LSM][run] Database not saved \n");

            System.out.printf("[LSM][run] Saving database: [%d] \n", database.size());
            boolean success = DataUtils.saveMatchBeanObjects(database, bucketName, databaseKey);
            System.out.printf("[LSM][run] Saving database success: [%b] \n", success);


        } else {
            System.out.printf("[LSM][run] Error: trying to save less records than what was loaded. " +
                    "Database NOT saved back to S3! \n");
            System.out.printf("[LSM][run] dbSize1: [%d], dbSize2: [%d], dbRemoved: [%d] \n", dbSize1, dbSize2, dbRemoved);
        }

        System.out.println("[LSM][run] ----------------------------------");
        System.out.println("[LSM][run] FINISH: LegacyScraperManager.run()");
        System.out.println("[LSM][run] ----------------------------------");
    }
    //
}
