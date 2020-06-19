package websiteScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FullMatchesAndShowsScraper implements WebsiteScraper {

    private final String URL = "https://www.fullmatchesandshows.com/";
    private List<String> errorLog = null;

    public FullMatchesAndShowsScraper() {
         errorLog = new LinkedList<String>();
    }

    public static void main(String[] args) {
        FullMatchesAndShowsScraper scraper = new FullMatchesAndShowsScraper();
        String[] testTitle = {
                  "<title>UCL Classic Match – Real Madrid vs Liverpool – 2018 Final</title>"
                , "<title>UCL Classic Match – Real Madrid VS Liverpool – 2018 Final</title>"
                , "<title>UCL Classic Match – Real Madrid v Liverpool – 2018 Final</title>"
                , "<title>UCL Classic Match – Real Madrid V Liverpool – 2018 Final</title>"
                , "<title>Real Madrid vs Liverpool – 2018 Final</title>"
                , "<title>Real Madrid VS Liverpool – 2018 Final</title>"
                , "<title>Real Madrid v Liverpool – 2018 Final</title>"
                , "<title>Real Madrid V Liverpool – 2018 Final</title>"
                , "<title>Full Show - Real Madrid vs Liverpool – 2018 Final</title>"
                , "<title>Full Show - Real Madrid VS Liverpool – 2018 Final</title>"
                , "<title>Full Show - Real Madrid v Liverpool – 2018 Final</title>"
                , "<title>Full Show - Real Madrid V Liverpool – 2018 Final</title>"
                , "<title>Full Show - Real Madrid vs Liverpool</title>"
                , "<title>Full Show - Real Madrid VS Liverpool</title>"
                , "<title>Full Show - Real Madrid v Liverpool</title>"
                , "<title>Full Show - Real Madrid V Liverpool</title>"
                , "<title>EPL Classic Match – Southampton v Liverpool – 20th March 2016</title>"
                , "<title>Arsenal Season Review 1997-98</title>"
                , "<title>La Liga Classics – Barcelona vs Valladolid -16-05-2010</title>"
                , "<title>La Liga Classics – Barcelona vs Valladolid 16-05-2010</title>"

        };
        for(int i = 0; i < testTitle.length; i++) {
            String homeTeam = scraper.scrapeHomeTeam(testTitle[i]);
            String awayTeam = scraper.scrapeAwayTeam(testTitle[i]);
            System.out.println("TEST:   " + testTitle[i]);
            System.out.println("RESULT: homeTeam: [" + homeTeam + "]");
            System.out.println("RESULT: awayTeam: [" + awayTeam + "]");
        }
    }

    @Override
    public String getUrl() {
        return URL;
    }

    @Override
    public List<String> getErrorLog() {
         return errorLog;
    }

    private String formatException(Exception e) {
         return e.toString();
    }

    private String scrapeToken(String html, String t1, String t2) {
        String token = "";
        try {
            int t1len = t1.length();
            int t2len = t2.length();
            int p1 = html.indexOf(t1);
            int p2 = html.indexOf(t2, p1);
            //System.out.println(p1 + "," + p2);
            token = html.substring(p1 + t1len, p2);
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeArticleKey(String html) {
        String token = "";
        try {
            String tmp = "";
            String t1 = "/";
            String t2 = "/\" rel=\"bookmark\"";
            int t1len = t1.length();
            int t2len = t2.length();
            int p = html.indexOf(t2);
            tmp = html.substring(0, p);
            p = tmp.lastIndexOf(t1);
            token = tmp.substring(p + t1len);
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeArticleTitle(String html) {
        String token = "";
        try {
            String t1 = "title=\"";
            String t2 = "\"><";
            token = scrapeToken(html, t1, t2);
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeArticleLinkUrl(String html) {
        String token = "";
        try {
            String t1 = "<a href=\"";
            String t2 = "/\"";
            token = scrapeToken(html, t1, t2);
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeMatchCompetition(String html) {

        String token = "";

        // for regular matches posts, these will have the
        // 'Competition' tag and we can scrape from there
        try {
            String t1 = "<br>Competition:";
            int p1 = html.indexOf(t1);
            String tmp = html.substring(p1);
            //System.out.println(tmp.substring(0, 200));
            t1 = "<strong>";
            int t1len = t1.length();
            p1 = tmp.indexOf(t1);
            //System.out.println(p1);
            String t2 = "</strong>";
            int p2 = tmp.indexOf(t2);
            token = tmp.substring(p1 + t1len, p2);
            //System.out.println(token);
        } catch(Exception e) {
            errorLog.add(formatException(e));
        }

        // if that doesn't work, checks the tags
        // todo: maybe this check should be done at another
        // todo: level in the application, e.g. the ScraperManager?
        try {
            if(token.equalsIgnoreCase("")) {
                List<String> tags = scrapeTags(html);
                String[] leagues = {
                        "Premier League"
                        , "La Liga"
                        , "Bundesliga"
                        , "Serie A"
                        , "Ligue 1"
                        , "Champions League"
                        , "Europa League"
                        //, "Eredivisie"
                };
                for(int i = 0; i < tags.size(); i++) {
                    for(int j = 0; j < leagues.length; j++) {
                        if(tags.get(i).equalsIgnoreCase(leagues[j])) {
                            token = leagues[j];
                        }
                    }
                }
            }
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeMatchWeek(String html) {
        // this website doesn't provide match week info
        return "";
    }

    private Map<String, String> scrapeTeams(String html) {
        Map<String, String> teams = new HashMap<String, String>();
        String homeTeam = "";
        String awayTeam = "";
        try {
            String t1 = "<title>";
            String t2 = "</title>";
            String pageTitle = scrapeToken(html, t1, t2);
            pageTitle = pageTitle.replaceAll("\\p{Pd}", "-");
            // if the title contains a match pairing information
            String[] tags = {" VS ", " vs ", " V ", " v "};
            String delimiter = " - ";
            boolean found = false;
            int count = 0;
            do {
                if(pageTitle.indexOf(tags[count]) != -1) {
                    found = true;
                }
                count = count + 1;
            } while(!found && count < tags.length);
            if(found) {
                // home team
                String tag = tags[count - 1];
                int p1 = pageTitle.indexOf(tag);
                int p2 = pageTitle.indexOf(delimiter);
                //System.out.println("pageTitle:" + pageTitle);
                //System.out.println("p1: " + p1);
                //System.out.println("p2: " + p2);
                if(p2 != -1 && p2 < p1) {
                    homeTeam = pageTitle.substring(p2+delimiter.length(), p1);
                } else {
                    homeTeam = pageTitle.substring(0, p1);
                }
                teams.put("homeTeam", homeTeam.trim());
                // away team
                int p3 = pageTitle.indexOf(delimiter, p1);
                if(p3 != -1) {
                    awayTeam = pageTitle.substring(p1+tag.length(), p3);
                } else {
                    int p4 = pageTitle.lastIndexOf(" ");
                    awayTeam = pageTitle.substring(p1+tag.length(), p4);
                }
                teams.put("awayTeam", awayTeam.trim());
            }
        } catch(Exception e) {
            errorLog.add(formatException(e));
            teams = new HashMap<String, String>();
        }
        return teams;
    }

    @Override
    public String scrapeHomeTeam(String html) {
        Map<String, String> teams = scrapeTeams(html);
        if (!teams.isEmpty()) {
            return scrapeTeams(html).get("homeTeam");
        }
        return "";
    }

    @Override
    public String scrapeAwayTeam(String html) {
        Map<String, String> teams = scrapeTeams(html);
        if (!teams.isEmpty()) {
            return scrapeTeams(html).get("awayTeam");
        }
        return "";
    }

    @Override
    public String scrapeMatchDate(String html) {

        String token = "";

        // first, try to extract the date from the 'Date:' field
        try {
            String t1 = "<br>Date:";
            int p1 = html.indexOf(t1);
            String tmp = html.substring(p1);
            t1 = "<strong>";
            int t1len = t1.length();
            p1 = tmp.indexOf(t1);
            String t2 = "</strong>";
            int p2 = tmp.indexOf(t2);
            token = tmp.substring(p1 + t1len, p2);
            token = token.trim();
            token = DateManager.formatDate(DateManager.extractDate(token));
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }

        // if that fails, then try to extract it from the <title>
        try {
            // if that doesn't work, scrape from title ...
            if(token.equalsIgnoreCase("")) {
                token = scrapeArticleTitle(html);
                token = DateManager.formatDate(DateManager.extractDate(token));
            }
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }



    @Override
    public String scrapeArticleDate(String html) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("time");
            Element article = articles.first();
            token = article.text();
            token = token.trim();
            token = DateManager.formatDate(DateManager.extractDate(token));
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeStadium(String html) {
        String token = "";
        try {
            String t1 = "<br>Stadium:";
            int p1 = html.indexOf(t1);
            String tmp = html.substring(p1);
            t1 = "<strong>";
            int t1len = t1.length();
            p1 = tmp.indexOf(t1);
            String t2 = "</strong>";
            int p2 = tmp.indexOf(t2);
            token = tmp.substring(p1 + t1len, p2);
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token.trim();
    }

    @Override
    public String scrapeReferee(String html) {
        String token = "";
        try {
            String t1 = "<br>Referee:";
            int p1 = html.indexOf(t1);
            String tmp = html.substring(p1);
            t1 = "<strong>";
            int t1len = t1.length();
            p1 = tmp.indexOf(t1);
            String t2 = "</strong>";
            int p2 = tmp.indexOf(t2);
            token = tmp.substring(p1 + t1len, p2);
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token.trim();
    }

    @Override
    public String scrapeArticleImageUrl(String html) {
        String token = "";
        try {
            String imageName = "";
            String t1 = "data-img-url=\"";
            String t2 = "\"";
            int t1len = t1.length();
            int p1 = html.indexOf(t1) + t1len;
            int p2 = html.indexOf(t2, p1 + t1len);
            imageName = html.substring(p1, p2);
            token = imageName.trim();
            token = token.replace("www.", "i1.wp.com/");
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeYear(String html) {
        String token = "";
        try {
            boolean found = false;
            // this returns format:  "2011-01-29T00:00:00.000Z"
            token = scrapeMatchDate(html);
            if(!token.equalsIgnoreCase("")) {
                //System.out.println("matchdate: " + token);
                token = token.substring(0, 4);
                //System.out.println("year: " + token);
                found = true;
            }
            if(!found) {
                // extract from the title
                // and use the date parts function
                token = scrapeToken(html, "<title>", "</title>");
                Map<String, String> dateParts = DateManager.extractDate(token);
                token = dateParts.get("year");
            }
        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        if(token == null) token = "";
        return token;
    }

    @Override
    public List<String> scrapeHtmlArticle(String html) {
        List<String> htmlArticleLL = new LinkedList<String>();
        try {
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("div.td-block-span4");
            htmlArticleLL = new LinkedList<String>();
            //System.out.printf("Articles to scrape: [%d] \n", articles.size());
            for (Element article : articles) {
                htmlArticleLL.add(article.html());
            }
        } catch(Exception e) {
            errorLog.add(formatException(e));
            htmlArticleLL = new LinkedList<String>();
        }
        return htmlArticleLL;
    }

    @Override
    public List<String> scrapeVideoLinkTags(String html) {
        List<String> videoLinkTagsLL = new LinkedList<String>();
        try {
            // find '<h1 class="entry-title">'
            // find all 'acp_title">'
            String token = "";
            String t1 = "<h1 class=\"entry-title\">";
            int p1 = html.indexOf(t1);
            String htmlTmp = html.substring(p1);
            String t2 = "acp_title\">";
            String t3 = "<";
            while(htmlTmp.contains(t2)) {
                p1 = htmlTmp.indexOf(t2) + t2.length();
                int p2 = htmlTmp.indexOf(t3, p1);
                token = htmlTmp.substring(p1, p2);
                videoLinkTagsLL.add(token.trim());
                htmlTmp = htmlTmp.substring(p2);
            }
        } catch(Exception e) {
            errorLog.add(formatException(e));
            videoLinkTagsLL = new LinkedList<String>();
        }
        return videoLinkTagsLL;
    }

    @Override
    public List<String> scrapeVideoLinkPageUrls(String html) {
        List<String> videoLinkPageUrlsLL = new LinkedList<String>();
        try {
            // find 'id="item2"><a href="' and then 3, 4, ...
            String token = "";
            int i = 2;
            String t1 = "id=\"item" + i + "\"><a href=\"";
            String t2 = "\"";
            int p1 = html.indexOf(t1);
            String htmlTmp = html.substring(p1);
            while(htmlTmp.contains(t1)) {
                p1 = htmlTmp.indexOf(t1) + t1.length();
                int p2 = htmlTmp.indexOf(t2, p1);
                token = htmlTmp.substring(p1, p2);
                videoLinkPageUrlsLL.add(token.trim());
                htmlTmp = htmlTmp.substring(p2);
                i = i + 1;
                t1 = "id=\"item" + i + "\"><a href=\"";
            }
        } catch(Exception e) {
            errorLog.add(formatException(e));
            videoLinkPageUrlsLL = new LinkedList<String>();
        }
        return videoLinkPageUrlsLL;
    }

    @Override
    public String scrapeVideoLink(String html, String videoLinkTag) {
        /*
        For this scraper, there's no need to use the videoLinkTag
        because each page only contains 1 video link

        This will be different on other sites where all video links
        are scraped from the same page, so we need to know which
        link to scrape - hence we will need the tag
         */
        String token = "";
        try {
            // find 'iframe', then 'src="', end '"'
            String t1 = "iframe";
            String t2 = "src=\"";
            String t3 = "\"";
            int p1 = html.indexOf(t1);
            int p2 = html.indexOf(t2, p1);
            int p3 = html.indexOf(t3, p2+t2.length());
            token = html.substring(p2+t2.length(), p3).trim();

            // truncate at '?' sign
            p1 = token.indexOf("?");
            if(p1 != -1) {
                token = token.substring(0, p1);
            }

        } catch(Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public List<String> scrapeTags(String html) {
        List<String> tags = new LinkedList<String>();
        try {
            Document doc = Jsoup.parse(html);
            Elements tagsEl = doc.select("li.entry-category");
            for(Element tagEl: tagsEl) {
                //System.out.printf("[%s] ", tagEl.text().trim());
                tags.add(tagEl.text().trim());
            }
            //System.exit(0);
        } catch(Exception e) {
            errorLog.add(formatException(e));
            tags = new LinkedList<String>();
        }
        return tags;
    }
}
