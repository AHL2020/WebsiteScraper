package websiteScraper;

import com.amazonaws.regions.Regions;
import legacy.MatchBean;

import java.util.*;

public class LegacyScraperManager {
    //
    public enum RunMode {TEST, PROD}
    //
    public static void main(String[] args) {
        S3Manager.DeploymentType type = S3Manager.DeploymentType.LOCAL;
        Regions region = Regions.US_EAST_1;
        String bucketName = ConfigManager.S3_BUCKET;
        String databaseKey = ConfigManager.DB_KEY;
        RunMode runMode = RunMode.TEST;
        run(type, region, bucketName, databaseKey, runMode);
    }
    //
    public static void run(S3Manager.DeploymentType deploymentType, Regions region, String bucketName, String databaseKey, RunMode runMode) {
        //
        // Start
        //
        //System.out.println("[LSM][run] ---------------------------------");
        System.out.println("[LSM][run] START: LegacyScraperManager.run()");
        //System.out.println("[LSM][run] ---------------------------------");
        //
        // Configure
        //
        S3Manager s3Manager = S3Manager.getInstance(deploymentType, region);
        ConfigManager configManager;
        configManager = ConfigManager.getInstance();
        if(deploymentType == S3Manager.DeploymentType.LOCAL) {
            configManager.loadConfig();
            System.out.println("[LSM][run] Loaded config from LOCAL: isConfigLoaded: [" + configManager.isConfigLoaded() + "]");
        } else {
            configManager.loadConfig(s3Manager);
            System.out.println("[LSM][run] Loaded config from PROD: isConfigLoaded: [" + configManager.isConfigLoaded() + "]");
        }
        boolean configSuccess = configManager.loadConfig();
        if(!configSuccess) System.exit(1);
        ScraperSession[] scraperSessions = {
                new ScraperSession(new FootballOrginScraper()
                    , "FootballOrgin"
                    , new Anonymiser(configManager).setAttributesToAnonymise(new String[]{"articleTitle", "articleImageUrl"}))
                , new ScraperSession(new FullMatchesAndShowsScraper()
                    , "FullMatchesAndShows"
                    , new Anonymiser(configManager).setAttributesToAnonymise(new String[]{"articleTitle", "articleImageUrl"}))
                , new ScraperSession(new HDMatchesScraper()
                    , "HDMatches"
                    , new Anonymiser(configManager).setAttributesToAnonymise(new String[]{"articleTitle", "articleImageUrl"}))
        };
        ValidationManager validationManager = ValidationManager.getInstance();
        ComparisonManager comparisonManager = ComparisonManager.getInstance(configManager);
        UpdateManager updateManager = UpdateManager.getInstance();
        //
        // Load data from database
        //
        //System.out.println("[LSM][run] ---------------------");
        System.out.println("[LSM][run] Load database from S3");
        //System.out.println("[LSM][run] ---------------------");
        List<Map<String, String>> database = LegacyIntegration.loadArticlesFromS3(bucketName, databaseKey);
        int dbSize1 = database.size();
        System.out.printf("[LSM][run] Loaded database: [%d] \n", dbSize1);
        //
        // Process scraper sessions
        //
        for(ScraperSession session: scraperSessions) {
            //
            // Get necessary configuration objects
            //
            Anonymiser anonymiser = session.getAnonymiser();
            WebsiteScraper scraper = session.getScraper();
            ScraperApp app = new ScraperApp(scraper);
            //
            // Scrape articles
            //
            System.out.println();
            System.out.println("[LSM][run] Starting new scraper session ...");
            System.out.println("[LSM][run] Scraping data from: [" + scraper.getUrl() + "] ...");
            List<Map<String, String>> articles = app.run();
            System.out.printf("[LSM][run] Scraped articles: [%d] \n", articles.size());
            System.out.printf("[LSM][run] Anonymise attributes: [%s] \n", anonymiser.getAttributesToAnonymiseAsString());
            //
            // Statistics - for current scraper session
            //
            int aProcessed = 0;
            int aAdded = 0;
            int aUpdated = 0;
            int aError = 0;
            int aInvalid = 0;
            //
            // Process scraped articles
            //
            //System.out.println("[LSM][run] ------------------------");
            System.out.println("[LSM][run] Process scraped articles");
            //System.out.println("[LSM][run] ------------------------");
            for (Map<String, String> article : articles) {
                System.out.println();
                aProcessed++;
                String articleKey = article.get("articleKey");
                System.out.println("[LSM][run] Processing article: [" + article.get("articleKey") + "]");
                System.out.println("[LSM][run] Article details: [" + article + "]");
                //
                // Fix & Validate
                //
                article = validationManager.fixArticle(article);
                System.out.println("[LSM][run] fixed article: " + article);
                boolean isValid = validationManager.isValidArticle(article);
                System.out.println("[LSM][run] article isValid: " + isValid);
                System.out.println("[LSM][run] validationManager log: " + validationManager.getLog());
                if (!isValid) {
                    //
                    // Invalid article -> skip
                    //
                    aInvalid++;
                } else {
                    //
                    // Anonymise
                    //
                    article = anonymiser.setOriginalArticle(article).anonymise().getAnonymisedArticle();
                    System.out.println("[LSM][run] anonymised article: " + article);
                    //
                    // Check if article already exists in database
                    //
                    Map<String, String> existingArticle = findArticle(database, article, comparisonManager);
                    System.out.println("[LSM][run] articleExists: " + existingArticle);
                    if(existingArticle != null) {
                        //
                        // Article exists -> update
                        //
                        System.out.println("[LSM][run] updating existing article ...");
                        updateManager.setExistingArticle(existingArticle).setNewArticle(article).updateArticle();
                        System.out.println("[LSM][run] updateLog: " + updateManager.getUpdateLogString());
                        System.out.println("[LSM][run] updated article: " + existingArticle);
                        if(runMode == RunMode.TEST) {
                            MatchBean mb = LegacyIntegration.convertArticleToMatchBean(existingArticle);
                            System.out.println("[LSM][run] TEST CSV String: " + mb.toCsvString());
                        }
                        aUpdated++;
                    } else {
                        //
                        // New article
                        //
                        System.out.printf("[LSM][run] Inserting new article, articleKey: [%s] \n", articleKey);
                        //
                        // Process image
                        //
                        if(!anonymiser.isAttributeSet("articleImageUrl")) {
                            String objectUrl = (article.get("articleImageUrl"));
                            String contentType = ContentTypeManager.getContentType(article.get("articleImageUrl"));
                            String objectKey = LegacyIntegration.getImageObjectKey(article);
                            System.out.printf("[LSM][run] Saving image objectKey: [%s] \n", objectKey);
                            boolean success = true;
                            if (runMode == RunMode.PROD) {
                                success = s3Manager.saveObjectFromUrl(objectUrl, contentType, bucketName, objectKey);
                                System.out.printf("[LSM][run] Saving image success: [%b] \n", success);
                            } else {
                                System.out.println("[LSM][run] Image not saved - running in TEST mode.");
                            }
                            if (success) {
                                article.put("articleImageUrl", objectKey);
                            } else {
                                article.put("articleImageUrl", "images/default-match-image.jpg");
                                System.out.println("[LSM][run] Error downloading image, using default image.");
                                aError++;
                            }
                        } else {
                            System.out.println("[LSM][run] Not processing image from scraped source.");
                        }
                        //
                        // Add article to database
                        //
                        if(runMode == RunMode.TEST) {
                            System.out.println("[LSM][run] TEST Adding article: " + article);
                            MatchBean mb = LegacyIntegration.convertArticleToMatchBean(article);
                            System.out.println("[LSM][run] TEST CSV String: " + mb.toCsvString());
                        }
                        database.add(article);
                        aAdded++;
                        System.out.println("[LSM][run] Added article to database.");
                    }
                }
            }
            //
            // Print statistics
            //
            System.out.println();
            System.out.println("[LSM][run] Process scraped articles COMPLETED: " + session.getSessionName());
            System.out.println("[LSM][run]    Processed:       [" + aProcessed + "]");
            System.out.println("[LSM][run]    Invalid:         [" + aInvalid + "]");
            System.out.println("[LSM][run]    Added:           [" + aAdded + "]");
            System.out.println("[LSM][run]    Updated:         [" + aUpdated + "]");
            System.out.println("[LSM][run]    Image error:     [" + aError + "]");
            System.out.println("[LSM][run]    Clubs not found: [" + configManager.getClubsNotFound().keySet() + "]");
        }
        //
        // Save database
        //
        //System.out.println("[LSM][run] -----------------------");
        System.out.println("[LSM][run] Save the database to S3");
        //System.out.println("[LSM][run] -----------------------");
        int dbSize2 = database.size();
        if(dbSize2 >= dbSize1) {
            if(runMode == RunMode.PROD) {
                System.out.printf("[LSM][run] Saving database: [%d] \n", database.size());
                boolean success = LegacyIntegration.saveArticlesToS3(bucketName, databaseKey, database);
                System.out.printf("[LSM][run] Saving database success: [%b] \n", success);
            } else {
                System.out.println("[LSM][run] Database not saved. Running in TEST mode.");
            }
        } else {
            System.out.println("[LSM][run] Error: trying to save less records than what was loaded. " +
                    "Database NOT saved back to S3!");
            System.out.printf("[LSM][run] dbSize1: [%d], dbSize2: [%d] \n", dbSize1, dbSize2);
        }
        //
        // Finish
        //
        //System.out.println("[LSM][run] ----------------------------------");
        System.out.println("[LSM][run] FINISH: LegacyScraperManager.run()");
        //System.out.println("[LSM][run] ----------------------------------");
    }
    //
    private static Map<String, String> findArticle(List<Map<String, String>> database, Map<String, String> searchArticle, ComparisonManager comparisonManager) {
        //String searchArticleKey = searchArticle.get("articleKey");
        //System.out.println("[LSM][findArticle] searchArticleKey: [" + searchArticleKey + "]");
        for(Map<String, String> tmpArticle: database) {
            if(comparisonManager.equalsArticle(tmpArticle, searchArticle)) {
                //System.out.println("[LSM][findArticle] article found: " + tmpArticle);
                return tmpArticle;
            }
        }
        //System.out.println("[LSM][findArticle] article not found, returning null");
        return null;
    }
    //
}
