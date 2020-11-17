package websiteScraper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ComparisonManager {
    //
    private static ComparisonManager instance = null;
    private ConfigManager configManager = null;
    //
    public static void main(String[] args) {

        //testEqualsArticleOnDB();

        testEqualsArticleFromScraper();

        //String dbKey = "burnley-vs-watford-full-match-premier-league-25-june-2020";
        //String scrapedKey = "burnley-vs-watford-highlights-full-match-2";
        //testEqualsArticleFromScraper(dbKey, scrapedKey);

        //testDetectArticleTypes();
    }
    //
    public static void testEqualsArticleOnDB() {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.loadConfig();
        ComparisonManager comparisonManager = ComparisonManager.getInstance(configManager);
        List<Map<String, String>> articles = LegacyIntegration.loadArticlesFromS3(configManager.getS3bucket(), configManager.getDatabaseKey());
        for(int i = 0; i < articles.size(); i++) {
            for(int j = 0; j < articles.size(); j++) {
                comparisonManager.equalsArticle(articles.get(i), articles.get(j));
            }
        }
    }
    //
    public static void testEqualsArticleFromScraper() {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.loadConfig();
        ComparisonManager comparisonManager = ComparisonManager.getInstance(configManager);
        List<Map<String, String>> articlesDB = LegacyIntegration.loadArticlesFromS3(configManager.getS3bucket(), configManager.getDatabaseKey());
        List<Map<String, String>> articlesScraped = new ScraperApp(new FullMatchesAndShowsScraper()).run();
        System.out.println("----------------------------------");
        for(int i = 0; i < articlesScraped.size(); i++) {
            System.out.printf(
                    "[ComparisonManager][testEqualsArticleFromScraper] checking article: [%s] \n"
                    , articlesScraped.get(i).get("articleKey"));
            for(int j = 0; j < articlesDB.size(); j++) {
                boolean articleExists = comparisonManager.equalsArticle(articlesScraped.get(i), articlesDB.get(j));
                if(articleExists) {
                    System.out.printf(
                            "[ComparisonManager][testEqualsArticleFromScraper] articles match: \n\t[%s][%s][%s] \n\t[%s][%s][%s] \n"
                            , articlesScraped.get(i).get("articleKey")
                            , articlesScraped.get(i).get("articleDate")
                            , comparisonManager.detectArticleTypes(articlesScraped.get(i)).toString()
                            , articlesDB.get(j).get("articleKey")
                            , articlesDB.get(j).get("articleDate")
                            , comparisonManager.detectArticleTypes(articlesDB.get(j)).toString()
                    );
                }
            }
        }
    }
    //
    public static void testEqualsArticleFromScraper(String dbKey, String scrapedKey) {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.loadConfig();
        ComparisonManager comparisonManager = ComparisonManager.getInstance(configManager);
        List<Map<String, String>> articlesDB = LegacyIntegration.loadArticlesFromS3(configManager.getS3bucket(), configManager.getDatabaseKey());
        List<Map<String, String>> articlesScraped = new ScraperApp(new FullMatchesAndShowsScraper()).run();
        Map<String, String> article1 = null;
        Map<String, String> article2 = null;
        for(int i = 0; i < articlesDB.size(); i++) {
            if(articlesDB.get(i).get("articleKey").equalsIgnoreCase(dbKey)) {
                article1 = articlesDB.get(i);
            }
        }
        for(int i = 0; i < articlesScraped.size(); i++) {
            if(articlesScraped.get(i).get("articleKey").equalsIgnoreCase(scrapedKey)) {
                article2 = articlesScraped.get(i);
            }
        }
        boolean equals = comparisonManager.equalsArticle(article1, article2);
        System.out.println("[ComparisonManager][testEqualsArticleFromScraper] article1: " + article1);
        System.out.println("[ComparisonManager][testEqualsArticleFromScraper] article2: " + article2);
        System.out.println("[ComparisonManager][testEqualsArticleFromScraper] equals: " + equals);
    }
    //
    public static void testDetectArticleTypes() {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.loadConfig();
        ComparisonManager comparisonManager = ComparisonManager.getInstance(configManager);
        List<Map<String, String>> articlesScraped = new ScraperApp(new FullMatchesAndShowsScraper()).run();
        for(int i = 0; i < articlesScraped.size(); i++) {
            Map<String, String> articleTypes = comparisonManager.detectArticleTypes(articlesScraped.get(i));
            System.out.println("[ComparisonManager][testDetectArticleTypes] ------------------------------------");
            System.out.println("[ComparisonManager][testDetectArticleTypes] article title: " + articlesScraped.get(i).get("articleTitle"));
            System.out.println("[ComparisonManager][testDetectArticleTypes] articleTypes: " + articleTypes.keySet());
            System.out.println("[ComparisonManager][testDetectArticleTypes] matchComp: " + articlesScraped.get(i).get("matchCompetition"));
        }
    }
    //
    private ComparisonManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
    //
    public static ComparisonManager getInstance(ConfigManager configManager) {
        if(ComparisonManager.instance == null) {
            ComparisonManager.instance = new ComparisonManager(configManager);
        }
        return ComparisonManager.instance;
    }
    //

    /**
     * compares 2 articles and returns whether they equal
     * @param article1: the existing article
     * @param article2: the comparison article
     * @return true if they equal, false otherwise
     */
    public boolean equalsArticle(Map<String, String> article1, Map<String, String> article2) {
        boolean equals = true;
        //
        // (1) Collect data
        //
        String article1Key = article1.get("articleKey");
        String article2Key = article2.get("articleKey");
        //
        String article1HomeTeam = article1.get("matchHomeTeam");
        String article2HomeTeam = article2.get("matchHomeTeam");
        String article1AwayTeam = article1.get("matchAwayTeam");
        String article2AwayTeam = article2.get("matchAwayTeam");
        String article1Competition = article1.get("matchCompetition");
        String article2Competition = article2.get("matchCompetition");
        //
        String article1HomeTeamClubId = configManager.getClubIdByName(article1HomeTeam).trim();
        String article2HomeTeamClubId = configManager.getClubIdByName(article2HomeTeam).trim();
        String article1AwayTeamClubId = configManager.getClubIdByName(article1AwayTeam).trim();
        String article2AwayTeamClubId = configManager.getClubIdByName(article2AwayTeam).trim();
        //
        Instant article1Instant = DateManager.toInstant(article1.get("articleDate"));
        Instant article2Instant = DateManager.toInstant(article2.get("articleDate"));
        //
        Map<String, String> article1Types = detectArticleTypes(article1);
        Map<String, String> article2Types = detectArticleTypes(article2);

        Map<String, String> commonTypesHM = commonElements(article1Types, article2Types);
        int commonTypes = commonTypesHM.size();
        //
        // (2) Check for empty fields and null
        //
        if(article1Key.equals("")) equals = false;
        if(article2Key.equals("")) equals = false;
        if(article1Instant == null) equals = false;
        if(article2Instant == null) equals = false;
        if(commonTypes == 0) equals = false;
        //
        // (3) Perform checks
        //
        boolean hasTeams = !isEmpty(article1HomeTeam) && !isEmpty(article1AwayTeam)
                && !isEmpty(article2HomeTeam) && !isEmpty(article2AwayTeam);
        boolean hasClubIds = !isEmpty(article1HomeTeamClubId) && !isEmpty(article1AwayTeamClubId)
                && !isEmpty(article2HomeTeamClubId) && !isEmpty(article2AwayTeamClubId);
        boolean isDefaultArticleType = isDefaultArticleType(article1) && isDefaultArticleType(article2);
        boolean hasCompetition = !isEmpty(article1Competition) && !isEmpty(article2Competition);
        boolean equalsArticleKey = article1Key.equals(article2Key);
        boolean equalsCompetition = hasCompetition && article1Competition.equals(article2Competition);

        boolean teamsEqualByClubId = hasTeams && hasClubIds
                && article1HomeTeamClubId.equals(article2HomeTeamClubId)
                && article1AwayTeamClubId.equals(article2AwayTeamClubId);

        boolean teamsEqualByName = equalsTeamsNames(article1, article2);

        boolean equalsTeams = teamsEqualByClubId || teamsEqualByName;

        //boolean equalsHomeTeam = hasTeams && hasClubIds && article1HomeTeamClubId.equals(article2HomeTeamClubId);
        //boolean equalsAwayTeam = hasTeams && hasClubIds && article1AwayTeamClubId.equals(article2AwayTeamClubId);
        long daysDiff = Math.abs(article1Instant.until(article2Instant, ChronoUnit.DAYS));
        //
        // (4) Perform comparison
        //
        if(equals && !equalsArticleKey) {
            if(equalsCompetition && equalsTeams && daysDiff < 4 && commonTypes > 0) {
                // could be a match, highlights, pre/post show, ...
                // problem: will match pre/post show with different manager names
                // todo: need a check for whether it's from the same source
            } else if(equalsTeams && daysDiff < 4 && commonTypes > 0) {
                // could be an FA Cup match, a match that has no Competition flag and that
                // would normally be in the 'Misc' category
            } else if(!isDefaultArticleType && !hasTeams && equalsCompetition && daysDiff < 4 && commonTypes > 0) {
                // could be the highlights show
            } else {
                equals = false;
            }
        }
        return equals;
    }
    //
    private boolean equalsTeamsNames(Map<String, String> article1, Map<String, String> article2) {
        String a1matchHomeTeam = article1.get("matchHomeTeam");
        String a1matchAwayTeam = article1.get("matchAwayTeam");
        String a2matchHomeTeam = article2.get("matchHomeTeam");
        String a2matchAwayTeam = article2.get("matchAwayTeam");
        boolean equalsHomeTeam = a1matchHomeTeam.equalsIgnoreCase(a2matchHomeTeam);
        boolean equalsAwayTeam = a1matchAwayTeam.equalsIgnoreCase(a2matchAwayTeam);
        return equalsHomeTeam && equalsAwayTeam;
    }
    //
    public boolean isEmpty(String s) {
        return s.trim().length() == 0;
    }
    //
    public boolean isFullMatchAndHighlights(Map<String, String> article) {
        Map<String, String> types = detectArticleTypes(article);
        if(types.keySet().contains("full match") && types.keySet().contains("highlights")) return true;
        return false;
    }
    //
    public boolean isFullMatchOnly(Map<String, String> article) {
        Map<String, String> types = detectArticleTypes(article);
        if(types.keySet().contains("full match") && !types.keySet().contains("highlights")) return true;
        return false;
    }
    //
    public boolean isHighlightsOnly(Map<String, String> article) {
        Map<String, String> types = detectArticleTypes(article);
        if(!types.keySet().contains("full match") && types.keySet().contains("highlights")) return true;
        return false;
    }
    //
    public boolean isDefaultArticleType(Map<String, String> article) {
        Map<String, String> types = detectArticleTypes(article);
        if(types.keySet().contains("default")) return true;
        return false;
    }
    //
    public Map<String, String> detectArticleTypes(Map<String, String> article) {
        //
        if(article == null) return new HashMap<>();
        String articleTitle = article.get("articleTitle").trim().replaceAll("-", " ");
        if(articleTitle.equals("")) return new HashMap<>();
        //
        List<String> articleTypesConfig = configManager.getArticleTypeNames(); // todo: validate against the config
        //
        Map<String, String> detectedArticleTypesHM = new HashMap<>();
        //List<String> detectedArticleTypesLL;
        //
        if(articleTitle.toLowerCase().contains("full match")) detectedArticleTypesHM.put("full match", articleTitle);
        if(articleTitle.toLowerCase().contains("highlights")) {
            if (article.get("matchHomeTeam").length() > 0 && article.get("matchAwayTeam").length() > 0) {
                detectedArticleTypesHM.put("highlights", articleTitle);
            } else if (article.get("matchCompetition").length() > 0) {
                detectedArticleTypesHM.put("highlights show", articleTitle);
            } else {
                detectedArticleTypesHM.put("default", articleTitle);
            }
        }
        if(articleTitle.toLowerCase().contains("preview")) detectedArticleTypesHM.put("preview", articleTitle);
        if(articleTitle.toLowerCase().contains("pre match press conference")) detectedArticleTypesHM.put("pre-match-press-conference", articleTitle);
        if(articleTitle.toLowerCase().contains("post match press conference")) detectedArticleTypesHM.put("post-match-press-conference", articleTitle);
        //
        if(detectedArticleTypesHM.size() == 0) {
            detectedArticleTypesHM.put("default", articleTitle);
        }
        //detectedArticleTypesLL = mapEntrySetToList(detectedArticleTypesHM);
        return detectedArticleTypesHM;
    }
    //
    public List<String> mapEntrySetToList(Map<String, String> map) {
        List<String> list = new LinkedList<>();
        for(Map.Entry entry: map.entrySet()) {
            list.add((String)entry.getKey());
        }
        return list;
    }
    //
    public Map<String, String> commonElements(Map<String, String> map1, Map<String, String> map2) {
        Map<String, String> mapResult = new HashMap<>(map1);
        mapResult.keySet().retainAll(map2.keySet());
        return mapResult;
    }
    //
}
