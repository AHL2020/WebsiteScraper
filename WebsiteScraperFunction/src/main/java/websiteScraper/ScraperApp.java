package websiteScraper;

import legacy.MatchBean; // only for testing, otherwise, remove dependency

import java.time.Instant;
import java.util.*;

/**
 * Singleton class
 */
public class ScraperApp {

    public static final String[] articleAttributes = {
            "articleKey"
            , "articleTitle"
            , "articleDate"
            , "articleImageUrl"
            , "articleLinkUrl"
            , "matchDate"
            , "matchHomeTeam"
            , "matchAwayTeam"
            , "matchYear"
            , "matchReferee"
            , "matchStadium"
            , "matchWeek"
            , "matchCompetition"
            , "matchTags"
            , "matchVideoLinks"
            , "scrapedDate"
    };

    private WebsiteScraper scraper = null;

    /**
     * For local testing
     */
    public static void main(String[] args) {
        try {

            // create a new scraper app, passing in the scraper implementation as parameter
            //ScraperApp app = new ScraperApp(new FullMatchesAndShowsScraper());
            ScraperApp app = new ScraperApp(new FootballOrginScraper());

            // run the scraper app, it will return the list of articles
            List<Map<String, String>> articles = app.run();

            // print error log
            System.out.println("Error Log: " + app.getErrorLog());

            // print articles
            for(int i = 0; i < articles.size(); i++) {

                System.out.println("");

                System.out.println(new PrettyPrintingMap<String, String>(articles.get(i)) + "\n");

                MatchBean match = LegacyIntegration.convertArticleToMatchBean(articles.get(i));

                System.out.println("CSV String: " + match.toCsvString());

                System.out.println("Match category: " + LegacyIntegration.getMatchCategory(articles.get(i)));

                System.out.println("Image object key: " + LegacyIntegration.getImageObjectKey(articles.get(i)));

                System.out.println("Image content type: " + ContentTypeManager.getContentType(articles.get(i).get("articleImageUrl")));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ScraperApp(WebsiteScraper scraper) {
        this.scraper = scraper;
    }

    public List<String> getErrorLog() {
        return scraper.getErrorLog();
    }

    public List<Map<String, String>> run() {

        String html = HttpManager.getHtmlFromUrl(scraper.getUrl());

        //System.out.println(html);

        // get the html of all articles
        List<String> htmlArticleLL = scraper.scrapeHtmlArticle(html);

        // list to store the scraped data (key value pairs)
        List<Map<String, String>> articlesDataLL = new LinkedList<>();

        //System.out.println(htmlArticleLL.get(0));

        // process each article- from the back to get newest articles first in the list
        for (int i = htmlArticleLL.size()-1; i >= 0; i--) {

            html = htmlArticleLL.get(i);

            // store attributes for the current article
            Map<String, String> articleAttr = new HashMap<>();

            // these are attributes we can extract from the listing
            // e.g. key, title, date, image url, link
            String articleKey = scraper.scrapeArticleKey(html);
            articleAttr.put("articleKey", articleKey);
            articleAttr.put("articleTitle", scraper.scrapeArticleTitle(html));
            articleAttr.put("articleDate", scraper.scrapeArticleDate(html));
            articleAttr.put("articleImageUrl", scraper.scrapeArticleImageUrl(html));
            articleAttr.put("articleLinkUrl", scraper.scrapeArticleLinkUrl(html));

            // now we're getting the details ...
            // get the html of the article's details page
            html = HttpManager.getHtmlFromUrl(articleAttr.get("articleLinkUrl"));

            // scrape any attribute from any type of article
            articleAttr.put("matchDate", scraper.scrapeMatchDate(html));
            articleAttr.put("matchHomeTeam", scraper.scrapeHomeTeam(html));
            articleAttr.put("matchAwayTeam", scraper.scrapeAwayTeam(html));
            articleAttr.put("matchYear", scraper.scrapeYear(html));
            articleAttr.put("matchReferee", scraper.scrapeReferee(html));
            articleAttr.put("matchStadium", scraper.scrapeStadium(html));
            articleAttr.put("matchWeek", scraper.scrapeMatchWeek(html));
            articleAttr.put("matchCompetition", scraper.scrapeMatchCompetition(html));

            List<String> matchTags = scraper.scrapeTags(html);
            articleAttr.put("matchTags", matchTags.toString());

            // process video links: video-tags, page-links, page-html, video-links
            List<String> videoLinkTags = scraper.scrapeVideoLinkTags(html);
            //articleAttr.put("TMP_videoLinkTags", videoLinkTags.toString());

            List<String> videoLinkPagesUrls = scraper.scrapeVideoLinkPageUrls(html);
            //articleAttr.put("TMP_videoLinkPagesUrls", videoLinkPagesUrls.toString());

            //System.out.println(new PrettyPrintingMap<String, String>(articleAttr));
            //System.exit(0);

            List<String> videoLinkPagesHtml = new LinkedList<>();
            videoLinkPagesHtml.add(html);
            for (int j = 0; j < videoLinkPagesUrls.size(); j++) {
                videoLinkPagesHtml.add(HttpManager.getHtmlFromUrl(videoLinkPagesUrls.get(j)));
            }
            //articleAttr.put("TMP_videoLinkPagesHtml_count", "[elements:"+videoLinkPagesHtml.size()+"]");
            //System.out.println(new PrettyPrintingMap<String, String>(articleAttr));
            //System.exit(0);

            // check: videoLinkPagesHtml should either have:
            // a) same number of elements as videoLinkTags; or
            // b) only 1 element
            // if not, then error

            List<String> videoLinkUrls = new LinkedList<>();
            for (int j = 0; j < videoLinkTags.size(); j++) {
                String videoLink = "";
                if (videoLinkPagesHtml.size() == videoLinkTags.size()) {
                    // scrape match links from separate pages
                    // or if there is only 1 video link
                    videoLink = scraper.scrapeVideoLink(videoLinkPagesHtml.get(j), videoLinkTags.get(j));
                    //videoLinkUrls.add(
                    //        scraper.scrapeVideoLink(videoLinkPagesHtml.get(j), videoLinkTags.get(j))
                    //);
                } else {
                    // scrape match links from the same page
                    videoLink = scraper.scrapeVideoLink(videoLinkPagesHtml.get(0), videoLinkTags.get(j));
                    //videoLinkUrls.add(
                    //        scraper.scrapeVideoLink(videoLinkPagesHtml.get(0), videoLinkTags.get(j))
                    //);
                }
                videoLinkUrls.add(videoLink);
            }
            //articleAttr.put("TMP_videoLinkUrls", videoLinkUrls.toString());

            // check: videoLinkUrls should have same number of elements
            // as videoLinkTags

            String videoLinksStr = "";
            for (int j = 0; j < videoLinkTags.size(); j++) {
                videoLinksStr += "" + videoLinkUrls.get(j) + "," + videoLinkTags.get(j) + "";
                if (j < videoLinkTags.size() - 1) {
                    videoLinksStr += ",";
                }
            }
            articleAttr.put("matchVideoLinks", videoLinksStr);

            articleAttr.put("scrapedDate", Instant.now().toString());

            // add attribute map of current article to list
            articlesDataLL.add(articleAttr);
        }

        // return the list
        return articlesDataLL;
    }
}
