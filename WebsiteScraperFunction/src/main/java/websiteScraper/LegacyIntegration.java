package websiteScraper;

import legacy.DataUtils;
import legacy.MatchBean;
import legacy.MatchUrlBean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is for Legacy Integration
 */
public class LegacyIntegration {

    public static void main(String[] args) {

        String s3Bucket = ConfigManager.S3_BUCKET;
        String databaseKey = ConfigManager.DB_KEY;

        /* test 1 */
        HashMap<String, MatchBean> matchBeanObjects = null;
        matchBeanObjects = DataUtils.loadMatchBeanObjects(s3Bucket, databaseKey);
        List<Map<String, String>> articles = new LinkedList<Map<String, String>>();
        for(Map.Entry<String, MatchBean> matchBeanEntry: matchBeanObjects.entrySet()) {
            MatchBean matchBean = matchBeanEntry.getValue();
            System.out.println("matchBean: " + matchBean);
            Map<String, String> article = convertMatchBeanToArticle(matchBean);
            articles.add(article);
            System.out.println(new PrettyPrintingMap<String, String>(article));
        }


        /* test 2


        List<Map<String, String>> articles = loadArticlesFromS3(s3Bucket, databaseKey);
        System.out.println("Articles loaded from S3: [" + articles.size() + "]");
        for(int i = 0; i < articles.size(); i++) {
            Map<String, String> article = articles.get(i);
            if(article.get("articleDate").indexOf("2020")==0) {
                //System.out.println(new PrettyPrintingMap<String, String>(article));
                System.out.println(article.get("articleKey"));
                //System.out.println(article.get("articleDate"));
                System.out.println(article.get("articleDate"));
            }
        }

         */
        //boolean success = saveArticlesToS3(s3Bucket, databaseKey, articles);
        //System.out.println("Articles saved to S3: [" + success + "]");
    }

    // where to save the images
    public static String getImageObjectKey(Map<String, String> article) {
        String articleImageUrl = article.get("articleImageUrl");
        String imageFilename = articleImageUrl.substring(articleImageUrl.lastIndexOf("/")+1);
        String objectKey = "images" + "/" + getMatchCategory(article) + "/" + imageFilename;
        return objectKey;
    }

    // the category determines into which sub-folder on S3 image and html/page files are stored
    public static String getMatchCategory(Map<String, String> article) {
        String competition = article.get("matchCompetition");
        switch(competition) {
            case "Premier League" : return "PremierLeague";
            case "Bundesliga" : return "Bundesliga";
            case "La Liga" : return "LaLiga";
            case "Serie A" : return "SerieA";
            case "Ligue 1" : return "Ligue1";
            case "Champions League" : return "ChampionsLeague";
            case "Europa League" : return "EuropaLeague";
            default : return "Misc";
        }
    }

    //
    public static MatchBean convertArticleToMatchBean(Map<String, String> article) {
        MatchBean match = new MatchBean();

        String articleDate = article.get("articleDate"); // format: "2019-12-19T00:00:00.000Z"
        String legacyDate = DateManager.formatDate(DateManager.toInstant(articleDate), "EEEE, dd MMMM yyyy");
        match.setDate(legacyDate); // format: "Tuesday, 26 May 2020"

        String matchCompetition = "Misc";
        if(!article.get("matchCompetition").equalsIgnoreCase("")) {
            matchCompetition = article.get("matchCompetition");
        }
        //System.out.println("matchCompetition:["+matchCompetition+"]");
        match.setCompetition(matchCompetition);

        String articleTitle = article.get("articleTitle");
        articleTitle = articleTitle.replaceAll("&amp;", "&");
        match.setMatchPairing(articleTitle);

        match.setMatchKey(article.get("articleKey"));
        match.setYear(article.get("matchYear"));
        match.setScrapedDate(DateManager.toInstant(article.get("scrapedDate")));

        String updatedDate = article.get("updatedDate");
        if(updatedDate != null) {
            if(!updatedDate.equalsIgnoreCase("")) {
                match.setPageLastUpdatedDate(DateManager.toInstant(article.get("updatedDate")));
            }
        }

        String createdDate = article.get("createdDate");
        if(createdDate != null) {
            if(!createdDate.equalsIgnoreCase("")) {
                match.setPageCreatedDate(DateManager.toInstant(article.get("createdDate")));
            }
        }

        match.setHomeTeam(article.get("matchHomeTeam"));
        match.setAwayTeam(article.get("matchAwayTeam"));
        match.setMatchWeek(article.get("matchWeek"));
        match.setImageName(article.get("articleImageUrl"));
        match.setLinkUrl(""); // this is the url of the .html page once it's created by the website builder
        match.setMatchUrls(article.get("matchVideoLinks"));
        match.setMatchUrlString(article.get("articleLinkUrl"));
        match.setMatchUrlsAndTags(new LinkedList<MatchUrlBean>()); // this is set by the website builder
        return match;
    }

    //
    public static Map<String, String> convertMatchBeanToArticle(MatchBean matchBean) {

        Map<String, String> article = new HashMap<String, String>();

        String legacyDate = matchBean.getDate(); // format: "Tuesday, 26 May 2020"
        String articleDate = DateManager.formatDate(DateManager.extractDate(legacyDate));
        article.put("articleDate", articleDate); // format: "2019-12-19T00:00:00.000Z"
        //System.out.println("articleDate:["+articleDate+"]");

        String matchCompetition = "Misc";
        if(!matchBean.getCompetition().equalsIgnoreCase("")) {
            matchCompetition = matchBean.getCompetition();
        }
        article.put("matchCompetition", matchCompetition);
        //System.out.println("matchCompetition:["+matchCompetition+"]");

        String articleTitle = matchBean.getMatchPairing();
        articleTitle = articleTitle.replaceAll("&amp;", "&");
        article.put("articleTitle", articleTitle);
        //System.out.println("articleTitle:["+articleTitle+"]");

        String articleKey = matchBean.getMatchKey();
        article.put("articleKey", articleKey);
        //System.out.println("articleKey:["+articleKey+"]");

        String matchYear = matchBean.getYear();
        article.put("matchYear", matchYear);
        //System.out.println("matchYear:["+matchYear+"]");

        String scrapedDate = "";
        if(matchBean.getScrapedDate() != null) {
            scrapedDate = matchBean.getScrapedDate().toString();
        }
        article.put("scrapedDate", scrapedDate);
        //System.out.println("scrapedDate:["+scrapedDate+"]");

        String updatedDate = "";
        if(matchBean.getPageLastUpdatedDate() != null) {
            updatedDate = matchBean.getPageLastUpdatedDate().toString();
        }
        article.put("updatedDate", updatedDate);
        //System.out.println("updatedDate:["+updatedDate+"]");

        String createdDate = "";
        if(matchBean.getPageCreatedDate() != null) {
            createdDate = matchBean.getPageCreatedDate().toString();
        }
        article.put("createdDate", createdDate);
        //System.out.println("createdDate:["+createdDate+"]");

        String matchHomeTeam = matchBean.getHomeTeam();
        article.put("matchHomeTeam", matchHomeTeam);
        //System.out.println("matchHomeTeam:["+matchHomeTeam+"]");

        String matchAwayTeam = matchBean.getAwayTeam();
        article.put("matchAwayTeam", matchAwayTeam);
        //System.out.println("matchAwayTeam:["+matchAwayTeam+"]");

        String matchWeek = matchBean.getMatchWeek();
        article.put("matchWeek", matchWeek);
        //System.out.println("matchWeek:["+matchWeek+"]");

        String articleImageUrl = matchBean.getImageName();
        article.put("articleImageUrl", articleImageUrl);
        //System.out.println("articleImageUrl:["+articleImageUrl+"]");

        String articlePageUrl = matchBean.getLinkUrl();
        article.put("articlePageUrl", articlePageUrl);
        //System.out.println("articlePageUrl:["+articlePageUrl+"]");

        String matchVideoLinks = matchBean.getMatchUrls();
        article.put("matchVideoLinks", matchVideoLinks);
        //System.out.println("matchVideoLinks:["+matchVideoLinks+"]");

        String articleLinkUrl = matchBean.getMatchUrlString();
        article.put("articleLinkUrl", articleLinkUrl);
        //System.out.println("articleLinkUrl:["+articleLinkUrl+"]");

        String matchUrlsAndTags = matchBean.getMatchUrlsAndTags().toString();
        article.put("matchUrlsAndTags", matchUrlsAndTags);
        //System.out.println("matchUrlsAndTags:["+matchUrlsAndTags+"]");

        return article;
    }

    //
    public static List<Map<String, String>> loadArticlesFromS3(String s3Bucket, String objectKey) {
        List<Map<String, String>> articlesLL = new LinkedList<>();
        HashMap<String, MatchBean> articlesHM = DataUtils.loadMatchBeanObjects(s3Bucket, objectKey);
        for(Map.Entry<String, MatchBean> entry: articlesHM.entrySet()) {
            articlesLL.add(convertMatchBeanToArticle(entry.getValue()));
        }
        return articlesLL;
    }

    //
    public static boolean saveArticlesToS3(String s3Bucket, String objectKey, List<Map<String, String>> articles) {

        HashMap<String, MatchBean> articlesHM = new HashMap<>();
        for(int i = 0; i < articles.size(); i++) {
            articlesHM.put(articles.get(i).get("articleKey"), convertArticleToMatchBean(articles.get(i)));
        }
        return DataUtils.saveMatchBeanObjects(articlesHM, s3Bucket, objectKey);
    }

}
