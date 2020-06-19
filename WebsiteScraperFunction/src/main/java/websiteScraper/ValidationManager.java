package websiteScraper;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ValidationManager {
    private static ValidationManager instance = null;
    private LinkedList<String> log = null;
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
        return (List<String>)log.clone();
    }
    // ---
    public boolean isValidArticle(Map<String,String> article) {
        log = new LinkedList<>();
        boolean isValid = true;
        String[] attributes = ScraperApp.articleAttributes;
        for(Map.Entry entry: article.entrySet()) {
            String key = (String)entry.getKey();
            //System.out.println(key);
            switch(key) {
                case "articleKey": /*System.out.println("validating articleKey ...");*/ isValid &= isValidArticleKey((String)entry.getValue()); break;
                case "articleTitle": /*System.out.println("validating articleTitle ...");*/ isValid &= isValidArticleTitle((String)entry.getValue()); break;
                case "articleDate": /*System.out.println("validating articleDate ...");*/ isValid &= isValidArticleDate((String)entry.getValue()); break;
                case "matchVideoLinks": /*System.out.println("validating matchVideoLinks ...");*/ isValid &= isValidMatchVideoLinks((String)entry.getValue()); break;
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
                    if(token.length() < 20 || token.length() > 100) {
                        log.add("[ValidationManager][isValidMatchVideoLinks]: link invalid length");
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
                    if(token.length() < 5 || token.length() > 20) {
                        log.add("[ValidationManager][isValidMatchVideoLinks]: tag invalid length");
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
        if(teamName.equalsIgnoreCase("")) return false;
        return true;
    }
    //
}
