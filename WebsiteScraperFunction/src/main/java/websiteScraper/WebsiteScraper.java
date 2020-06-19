package websiteScraper;

import java.io.IOException;
import java.util.List;

public interface WebsiteScraper {
    public String getUrl();
    public List<String> getErrorLog();
    public String scrapeArticleKey(String html);
    public String scrapeArticleTitle(String html);
    public String scrapeArticleLinkUrl(String html);
    public String scrapeArticleDate(String html);
    public String scrapeArticleImageUrl(String html);
    public String scrapeMatchCompetition(String html);
    public String scrapeMatchWeek(String html);
    public String scrapeHomeTeam(String html);
    public String scrapeAwayTeam(String html);
    public String scrapeMatchDate(String html);
    public String scrapeStadium(String html);
    public String scrapeReferee(String html);
    public String scrapeYear(String html);
    public List<String> scrapeHtmlArticle(String html);
    public List<String> scrapeVideoLinkTags(String html);
    public List<String> scrapeVideoLinkPageUrls(String html);
    public String scrapeVideoLink(String html, String videoLinkTag);
    public List<String> scrapeTags(String html);
}
