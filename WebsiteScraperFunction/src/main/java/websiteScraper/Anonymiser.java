package websiteScraper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Anonymiser {
    //
    private Map<String, String> originalArticle;
    private Map<String, String> anonymisedArticle;
    private final ConfigManager configManager;
    private final ComparisonManager comparisonManager;
    private final Map<String, String> attributesToAnonymiseMap;
    //
    public Anonymiser(ConfigManager configManager) {
        attributesToAnonymiseMap = new HashMap<>();
        this.configManager = configManager;
        this.comparisonManager = ComparisonManager.getInstance(this.configManager);
    }
    //
    public Anonymiser setOriginalArticle(Map<String, String> originalArticle) {
        this.originalArticle = originalArticle;
        return this;
    }
    //
    public Anonymiser anonymise() {
        if(originalArticle == null) return null;
        if(originalArticle.size() == 0) return null;
        anonymisedArticle = new HashMap<>();
        Set<String> keys = originalArticle.keySet();
        Iterator<String> iterator = keys.iterator();
        String anonymisedValue;
        while(iterator.hasNext()) {
            String key = iterator.next();
            anonymisedValue = originalArticle.get(key);
            switch(key) {
                case "articleTitle" :
                    if(isAttributeSet("articleTitle")) {
                        anonymisedValue = anonymiseTitle();
                    } break;
                case "articleImageUrl" :
                    if(isAttributeSet("articleImageUrl")) {
                        anonymisedValue = anonymiseImageUrl();
                    } break;
                //case "" : anonymisedValue = xyz(); break;
                //case "" : anonymisedValue = xyz(); break;
                default : break;
            }
            anonymisedArticle.put(key, anonymisedValue);
        }
        return this;
    }
    //
    public Map<String, String> getAnonymisedArticle() {
        return anonymisedArticle;
    }
    //
    public Anonymiser setAttributesToAnonymise(String[] attributesToAnonymise) {
        for(String s: attributesToAnonymise) {
            attributesToAnonymiseMap.put(s, s);
        }
        return this;
    }
    //
    public String[] getAttributesToAnonymise() {
        String[] attrArr = new String[attributesToAnonymiseMap.size()];
        int i = 0;
        for(String s: attributesToAnonymiseMap.keySet()) {
            attrArr[i++] = s;
        }
        return attrArr;
    }
    //
    public String getAttributesToAnonymiseAsString() {
        StringBuilder sb = new StringBuilder();

        for(String s: attributesToAnonymiseMap.keySet()) {
            sb.append(s);
            sb.append(", ");
        }
        String attrStr = sb.toString();
        if(attrStr.length() >= 2) attrStr = attrStr.substring(0, attrStr.length() - 2);
        return attrStr;
    }
    //
    public boolean isAttributeSet(String attributeName) {
        return attributesToAnonymiseMap.containsKey(attributeName);
    }
    //
    private String anonymiseTitle() {

        //System.out.println("[Anonymiser][anonymiseTitle] Start ... ");

        String title = originalArticle.get("articleTitle");

        //System.out.println("[Anonymiser][anonymiseTitle] originalArticle: " + title);

        boolean isFullMatchAndHighlights = comparisonManager.isFullMatchAndHighlights(originalArticle);

        //System.out.println("[Anonymiser][anonymiseTitle] isFullMatchAndHighlights: " + isFullMatchAndHighlights);

        boolean isFullMatchOnly = comparisonManager.isFullMatchOnly(originalArticle);

        //System.out.println("[Anonymiser][anonymiseTitle] isFullMatchOnly: " + isFullMatchOnly);

        boolean isHighlightsOnly = comparisonManager.isHighlightsOnly(originalArticle);

        //System.out.println("[Anonymiser][anonymiseTitle] isHighlightsOnly: " + isHighlightsOnly);

        if(isFullMatchAndHighlights || isFullMatchOnly || isHighlightsOnly) {
            String homeTeamId = configManager.getClubIdByName(originalArticle.get("matchHomeTeam"));

            //System.out.println("[Anonymiser][anonymiseTitle] homeTeamId: " + homeTeamId);

            String awayTeamId = configManager.getClubIdByName(originalArticle.get("matchAwayTeam"));

            //System.out.println("[Anonymiser][anonymiseTitle] awayTeamId: " + awayTeamId);

            String homeTeamName = configManager.getClubDefaultNameById(homeTeamId);

            //System.out.println("[Anonymiser][anonymiseTitle] homeTeamName: " + homeTeamName);

            String awayTeamName = configManager.getClubDefaultNameById(awayTeamId);

            //System.out.println("[Anonymiser][anonymiseTitle] awayTeamName: " + awayTeamName);

            if(homeTeamName.length() > 0 && awayTeamName.length() > 0) {
                String matchCompetition = originalArticle.get("matchCompetition");
                String dateStr = originalArticle.get("matchDate");
                if(dateStr.trim().length()==0) {
                    dateStr = originalArticle.get("articleDate");
                }
                String matchDate = DateManager.formatDate(DateManager.toInstant(dateStr), "dd MMMM yyyy");
                String matchType;
                if(isFullMatchAndHighlights) {
                    matchType = "Full Match & Highlights";
                } else if(isFullMatchOnly) {
                    matchType = "Full Match";
                } else {
                    matchType = "Highlights";
                }
                title = "" + homeTeamName + " vs. " + awayTeamName
                        + " - " + matchType;
                        //+ " - " + matchCompetition
                        //+ " - " + matchDate;
            }
        }

        //System.out.println("[Anonymiser][anonymiseTitle] Finish ... ");

        return title;
    }
    //
    private String anonymiseImageUrl() {
        String matchCompetition = originalArticle.get("matchCompetition");
        String logoDir = configManager.getImageCategoriesDir();
        String lang = ConfigManager.LANG_CN;
        String logoFile = configManager.getCategoryLogoByNameAndLanguage(matchCompetition, lang);
        if(logoFile.trim().length() == 0) {
            //System.out.println("\n\n >>> " + configManager.getDefaultArticleCategory() + "\n\n");
            logoFile = configManager.getCategoryLogoByNameAndLanguage(configManager.getDefaultArticleCategory(), lang);
        }
        return logoDir + logoFile;
    }
}
