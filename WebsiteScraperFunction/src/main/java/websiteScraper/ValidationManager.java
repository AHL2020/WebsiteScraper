package websiteScraper;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ValidationManager {
    //
    public static final int VIDEOLINK_LINK_MIN_LENGTH = 10;
    public static final int VIDEOLINK_LINK_MAX_LENGTH = 100;
    public static final int VIDEOLINK_TAG_MIN_LENGTH = 5;
    public static final int VIDEOLINK_TAG_MAX_LENGTH = 20;
    //
    private static ValidationManager instance = null;
    private List<String> log;
    private ConfigManager configManager = ConfigManager.getInstance();
    // ---
    private ValidationManager() {
        log = new LinkedList<>();
    }
    // ---
    public static ValidationManager getInstance() {
        if(instance == null) {
            instance = new ValidationManager();
        }
        return instance;
    }
    // ---
    public List<String> getLog() {
        return log;
    }
    // ---
    public boolean isValidArticle(Map<String,String> article) {
        log = new LinkedList<>();
        boolean isValid = true;
        //String[] attributes = ScraperApp.articleAttributes;
        for(Entry<String, String> entry: article.entrySet()) {
            String key = entry.getKey();
            //System.out.println(key);
            switch(key) {
                case "articleKey":       isValid &= isValidArticleKey(entry.getValue()); break;
                case "articleTitle":     isValid &= isValidArticleTitle(entry.getValue()); break;
                case "articleDate":      isValid &= isValidArticleDate(entry.getValue()); break;
                //case "matchHomeTeam":
                //case "matchAwayTeam":    isValid &= isValidTeam(entry.getValue()); break;
                case "matchVideoLinks":  isValid &= isValidMatchVideoLinks(entry.getValue()); break;
                case "matchCompetition": isValid &= isValidMatchCompetition(entry.getValue()); break;
            }
        }
        return isValid;
    }
    // ---
    public boolean isValidArticleKey(String articleKey) {
        //System.out.println("isValidArticleKey ...");
        if(articleKey == null) {
            log.add("[ValidationManager][isValidArticleKey]: articleKey is null");
            return false;
        }
        if(articleKey.equalsIgnoreCase("")) {
            log.add("[ValidationManager][isValidArticleKey]: articleKey is empty");
            return false;
        }
        if(articleKey.length() < 3 || articleKey.length() > 100) {
            log.add("[ValidationManager][isValidArticleKey]: articleKey invalid length");
            return false;
        }
        //System.out.println("isValidArticleKey ... valid");
        return true;
    }
    // ---
    public boolean isValidArticleTitle(String articleTitle) {
        //System.out.println("isValidArticleTitle ...");
        if(articleTitle == null) {
            log.add("[ValidationManager][isValidArticleTitle]: articleTitle is null");
            return false;
        }
        if(articleTitle.equalsIgnoreCase("")) {
            log.add("[ValidationManager][isValidArticleTitle]: articleTitle is empty");
            return false;
        }
        if(articleTitle.length() < 3 || articleTitle.length() > 100) {
            log.add("[ValidationManager][isValidArticleTitle]: articleTitle invalid length");
            return false;
        }
        //System.out.println("isValidArticleTitle ... valid");
        return true;
    }
    // ---
    public boolean isValidArticleDate(String articleDate) {
        //System.out.println("isValidArticleDate ...");
        if(articleDate == null) {
            log.add("[ValidationManager][isValidArticleDate]: articleDate is null");
            return false;
        }
        if(articleDate.equalsIgnoreCase("")) {
            log.add("[ValidationManager][isValidArticleDate]: articleDate is empty");
            return false;
        }
        try {
            Instant instant = Instant.parse(articleDate);
            if(instant == null) {
                log.add("[ValidationManager][isValidArticleDate]: articleDate Instant parse null");
                return false;
            }
        } catch(Exception e) {
            log.add("[ValidationManager][isValidArticleDate]: articleDate Instant parse Exception");
            return false;
        }
        //System.out.println("isValidArticleDate ... valid");
        return true;
    }
    // ---
    public boolean isValidMatchCompetition(String matchCompetition) {
        List<String> validCompetitions = configManager.getArticleCategoryNames();
        for(String competition: validCompetitions) {
            if(competition.equalsIgnoreCase(matchCompetition)) return true;
        }
        log.add("[ValidationManager][isValidMatchCompetition]: found invalid competition: [" + matchCompetition + "]");
        return false;
    }
    // ---
    public boolean isValidMatchVideoLinks(String matchVideoLinks) {
        //System.out.println("isValidMatchVideoLinks ...");
        String[] blacklist = {".jpg", ".jpeg", ".png"};
        if(matchVideoLinks == null) {
            log.add("[ValidationManager][isValidMatchVideoLinks]: matchVideoLinks is null");
            return false;
        }
        if(matchVideoLinks.equalsIgnoreCase("")) {
            log.add("[ValidationManager][isValidMatchVideoLinks]: matchVideoLinks is empty");
            return false;
        }
        try {
            String[] parts = matchVideoLinks.split(",");
            if(parts.length % 2 != 0) {
                //System.out.println("odd number tokens: " + parts.length);
                log.add("[ValidationManager][isValidMatchVideoLinks]: odd number of tokens");
                return false;
            }
            boolean odd = true;
            for(String token: parts) {
                token = token.trim();
                token = token.toLowerCase();
                if(token.equalsIgnoreCase("")) {
                    log.add("[ValidationManager][isValidMatchVideoLinks]: empty token");
                    return false;
                }
                if(odd) {
                    // link
                    if(token.length() < ValidationManager.VIDEOLINK_LINK_MIN_LENGTH
                        || token.length() > ValidationManager.VIDEOLINK_LINK_MAX_LENGTH) {
                        log.add("[ValidationManager][isValidMatchVideoLinks]: link invalid length (" + token.length() + ")");
                        return false;
                    }
                    for(String invalidToken: blacklist) {
                        if(token.contains(invalidToken)) {
                            log.add("[ValidationManager][isValidMatchVideoLinks]: link on blacklist");
                            return false;
                        }
                    }
                } else {
                    // tag
                    if(token.length() < ValidationManager.VIDEOLINK_TAG_MIN_LENGTH
                        || token.length() > ValidationManager.VIDEOLINK_TAG_MAX_LENGTH) {
                        log.add("[ValidationManager][isValidMatchVideoLinks]: tag invalid length (" + token.length() + ")");
                        return false;
                    }
                }
                odd = !odd;
                //System.out.println(odd);
            }
        } catch(Exception e) {
            log.add("[ValidationManager][isValidMatchVideoLinks]: Exception");
            return false;
        }
        //System.out.println("isValidMatchVideoLinks ... valid");
        return true;
    }
    //
    public boolean isValidTeam(String teamName) {
        if(teamName == null) return false;
        return !teamName.equalsIgnoreCase("");
    }
    //
    public Map<String, String> fixArticle(Map<String, String> article) {
        fixArticleTitle(article);
        fixVideoLinks(article);
        fixVideoLinksTags(article);
        fixMatchCompetition(article);
        return article;
    }
    //
    private Map<String, String> fixArticleTitle(Map<String, String> article) {
        String title = article.get("articleTitle");
        title = title.replaceAll("&amp;", "&");
        article.put("articleTitle", title);
        return article;
    }
    //
    private Map<String, String> fixVideoLinks(Map<String, String> article) {
        if(article.get("matchVideoLinks").equals("")) return article;
        String[] videoLinks = article.get("matchVideoLinks").split(",");
        //System.out.println("[ValidationManager][fixVideoLinks] before: " + article.get("matchVideoLinks"));
        List<String> videoLinksFixedLL = new LinkedList<>();
        for(int i = 0; i < videoLinks.length; i=i+2) {
            String link = videoLinks[i].trim();
            String tag = videoLinks[i+1].trim();
            if(link.length() > 0 && tag.length() > 0) {
                videoLinksFixedLL.add(link);
                videoLinksFixedLL.add(tag);
            }
        }
        String videoLinksFixedStr = videoLinksFixedLL.toString();
        videoLinksFixedStr = videoLinksFixedStr.replaceAll("\\[", "").replaceAll("]", "");
        //if(videoLinksFixedStr.indexOf("[") == 0) {
        //    videoLinksFixedStr = videoLinksFixedStr.substring(1);
        //}
        //if(videoLinksFixedStr.indexOf("]") == videoLinksFixedStr.length()-1) {
        //    videoLinksFixedStr = videoLinksFixedStr.substring(0, videoLinksFixedStr.length()-1);
        //}
        //System.out.println("[ValidationManager][fixVideoLinks] after:  " + videoLinksFixedStr);
        article.put("matchVideoLinks", videoLinksFixedStr);
        return article;
    }
    //
    private Map<String, String> fixVideoLinksTags(Map<String, String> article) {
        if(article.get("matchVideoLinks").equals("")) return article;
        String[] videoLinks = article.get("matchVideoLinks").split(",");
        //System.out.println("[ValidationManager][fixVideoLinksTags] before: " + article.get("matchVideoLinks"));
        StringBuilder videoLinksFixedStr = new StringBuilder();
        boolean foundIssues = false;
        for(int i = 0; i < videoLinks.length; i=i+2) {
            String link = videoLinks[i].trim();
            String tag = videoLinks[i+1].trim();
            tag = tag.replaceAll("&amp;", "");
            if(tag.length() > ValidationManager.VIDEOLINK_TAG_MAX_LENGTH) {
                int truncate = ValidationManager.VIDEOLINK_TAG_MAX_LENGTH - 3;
                tag = tag.substring(0,truncate) + "...";
                foundIssues = true;
            }
            videoLinksFixedStr.append(link).append(",").append(tag).append(",");
        }
        if(videoLinksFixedStr.lastIndexOf(",") == videoLinksFixedStr.length()-1) {
            videoLinksFixedStr = new StringBuilder(videoLinksFixedStr.substring(0, videoLinksFixedStr.length() - 2));
        }
        //System.out.println("[ValidationManager][fixVideoLinksTags] after:  " + videoLinksFixedStr);
        if(foundIssues) {
            article.put("matchVideoLinks", videoLinksFixedStr.toString());
        }
        return article;
    }
    //
    private Map<String, String> fixMatchCompetition(Map<String, String> article) {
        String defaultCategory = configManager.getDefaultArticleCategory();
        String oldMatchCompetition = article.get("matchCompetition");
        if(!isValidMatchCompetition(oldMatchCompetition)) {
            article.put("matchCompetition",defaultCategory);
            //System.out.println("[ValidationManager][fixMatchCompetition] fixed from [" + oldMatchCompetition + "] to [" + defaultCategory + "]");
        }
        return article;
    }
    //
}
