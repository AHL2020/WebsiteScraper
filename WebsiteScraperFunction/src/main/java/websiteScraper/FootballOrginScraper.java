package websiteScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FootballOrginScraper implements WebsiteScraper {

    private final String URL = "https://www.footballorgin.com/";
    private List<String> errorLog = null;

    public FootballOrginScraper() {
        errorLog = new LinkedList<String>();
    }

    public static void main(String[] args) {
        FootballOrginScraper scraper = new FootballOrginScraper();
        String html = HttpManager.getHtmlFromUrl(scraper.getUrl());
        List<String> articlesHtml = scraper.scrapeHtmlArticle(html);
        //System.out.println(articlesHtml);
        for (int i = 0; i < articlesHtml.size(); i++) {
            String articleKey = scraper.scrapeArticleKey(articlesHtml.get(i));
            System.out.println("articleKey: [" + articleKey + "]");
            String articleTitle = scraper.scrapeArticleTitle(articlesHtml.get(i));
            //System.out.println("articleTitle: [" + articleTitle + "]");
            String articleDate = scraper.scrapeArticleDate(articlesHtml.get(i));
            //System.out.println("articleDate: [" + articleDate + "]");
            String articleImageUrl = scraper.scrapeArticleImageUrl(articlesHtml.get(i));
            //System.out.println("articleImageUrl: [" + articleImageUrl + "]");
            String articleLinkUrl = scraper.scrapeArticleLinkUrl(articlesHtml.get(i));
            //System.out.println("articleLinkUrl: [" + articleLinkUrl + "]");

            //articleLinkUrl = "https://www.footballorgin.com/werder-bremen-vs-bayer-leverkusen-full-match-bundesliga-18-may-2020/";

            String articleHtml = HttpManager.getHtmlFromUrl(articleLinkUrl);

            String matchDate = scraper.scrapeMatchDate(articleHtml);
            //System.out.println("matchDate: [" + matchDate + "]");

            String homeTeam = scraper.scrapeHomeTeam(articleHtml);
            //System.out.println("homeTeam: [" + homeTeam + "]");

            String awayTeam = scraper.scrapeAwayTeam(articleHtml);
            //System.out.println("awayTeam: [" + awayTeam + "]");

            String matchYear = scraper.scrapeYear(articleHtml);
            //System.out.println("matchYear: [" + matchYear + "]");

            String matchCompetition = scraper.scrapeMatchCompetition(articleHtml);
            //System.out.println("matchCompetition: [" + matchCompetition + "]");

            List<String> videoLinkTags = scraper.scrapeVideoLinkTags(articleHtml);
            //System.out.println("videoLinkTags: [" + videoLinkTags + "]");

            List<String> videoLinkPageUrls = scraper.scrapeVideoLinkPageUrls(articleHtml);
            //System.out.println("videoLinkPageUrls: [" + videoLinkPageUrls + "]");

            // todo: bug - need to retrieve articleHtml for each page where link is to be extracted
            //for (int j = 0; j < videoLinkTags.size(); j++) {
            //    String videoLink = scraper.scrapeVideoLink(articleHtml, videoLinkTags.get(j));
            //    System.out.println("videoLink: [" + videoLink + "]");
            //}
        }
        /*
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
        for (int i = 0; i < testTitle.length; i++) {
            String homeTeam = scraper.scrapeHomeTeam(testTitle[i]);
            String awayTeam = scraper.scrapeAwayTeam(testTitle[i]);
            System.out.println("TEST:   " + testTitle[i]);
            System.out.println("RESULT: homeTeam: [" + homeTeam + "]");
            System.out.println("RESULT: awayTeam: [" + awayTeam + "]");
        }

         */
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
            int p1 = html.indexOf(t1);
            int p2 = html.indexOf(t2, p1 + t1len);
            token = html.substring(p1 + t1len, p2);
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeArticleKey(String html) {
        //System.out.println(html);
        String token = "";
        try {
            token = scrapeToken(html, "href", "title").trim();
            token = token.substring(0, token.length() - 2);
            token = token.substring(token.lastIndexOf("/"));
            token = token.substring(1);
            //System.out.println("["+token+"]");
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        //System.out.printf("[FOS][scrapeArticleKey] token: %s \n", token);
        return token;
    }

    @Override
    public String scrapeArticleTitle(String html) {
        //System.out.println(html);
        String token = "";
        try {
            String t1 = "title=\"";
            String t2 = "\" ";
            token = scrapeToken(html, t1, t2);
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeArticleLinkUrl(String html) {
        //System.out.println(html);
        String token = "";
        try {
            String t1 = "<a href=\"";
            String t2 = "/\"";
            token = scrapeToken(html, t1, t2);
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeMatchCompetition(String html) {
        String token = "";
        try {
            String title = scrapeToken(html, "<title>", "</title>");
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
            for (int i = 0; i < leagues.length; i++) {
                if (title.indexOf(leagues[i]) != -1 || title.indexOf(leagues[i].replaceAll(" ", "")) != -1) {
                    token = leagues[i];
                }
            }
        } catch (Exception e) {
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

            pageTitle = pageTitle.replaceAll("Highlights", "");
            pageTitle = pageTitle.replaceAll("Highlight", "");
            pageTitle = pageTitle.replaceAll("Full Match", "");
            pageTitle = pageTitle.replaceAll("Preview", "");

            // if the title contains a match pairing information
            String[] tags = {" VS ", " vs ", " V ", " v "};
            String delimiter = " - ";
            boolean found = false;
            int count = 0;
            do {
                if (pageTitle.indexOf(tags[count]) != -1) {
                    found = true;
                }
                count = count + 1;
            } while (!found && count < tags.length);
            if (found) {
                // home team
                String tag = tags[count - 1];
                int p1 = pageTitle.indexOf(tag);
                int p2 = pageTitle.indexOf(delimiter);
                //System.out.println("pageTitle:" + pageTitle);
                //System.out.println("p1: " + p1);
                //System.out.println("p2: " + p2);
                if (p2 != -1 && p2 < p1) {
                    homeTeam = pageTitle.substring(p2 + delimiter.length(), p1);
                } else {
                    homeTeam = pageTitle.substring(0, p1);
                }
                teams.put("homeTeam", homeTeam.trim());
                // away team
                int p3 = pageTitle.indexOf(delimiter, p1);
                if (p3 != -1) {
                    awayTeam = pageTitle.substring(p1 + tag.length(), p3);
                } else {
                    int p4 = pageTitle.lastIndexOf(" ");
                    awayTeam = pageTitle.substring(p1 + tag.length(), p4);
                }
                teams.put("awayTeam", awayTeam.trim());
            }
        } catch (Exception e) {
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
        //System.out.println(html);
        String token = "";
        try {
            if (token.equalsIgnoreCase("")) {
                token = scrapeToken(html, "<title>", "</title>");
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
        //System.out.println(html);
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("time");
            Element article = articles.first();
            token = article.text();
            token = token.trim();
            token = DateManager.formatDate(DateManager.extractDate(token));
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public String scrapeStadium(String html) {
        String token = "";
        try {
            //
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token.trim();
    }

    @Override
    public String scrapeReferee(String html) {
        String token = "";
        try {
            //
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        return token.trim();
    }

    @Override
    public String scrapeArticleImageUrl(String html) {
        //System.out.println(html);
        String token = "";
        try {
            String imageName = "";
            String t1 = "data-src=\"";
            String t2 = "\"";
            int t1len = t1.length();
            int p1 = html.indexOf(t1) + t1len;
            int p2 = html.indexOf(t2, p1 + t1len);
            imageName = html.substring(p1, p2);
            token = imageName.trim();
            token = token.replace("www.", "i1.wp.com/");
        } catch (Exception e) {
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
            if (!token.equalsIgnoreCase("")) {
                //System.out.println("matchdate: " + token);
                token = token.substring(0, 4);
                //System.out.println("year: " + token);
                found = true;
            }
            if (!found) {
                // extract from the title
                // and use the date parts function
                token = scrapeToken(html, "<title>", "</title>");
                Map<String, String> dateParts = DateManager.extractDate(token);
                token = dateParts.get("year");
            }
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        if (token == null) token = "";
        return token;
    }

    @Override
    public List<String> scrapeHtmlArticle(String html) {
        List<String> htmlArticleLL = new LinkedList<String>();
        try {
            int counter = 0;
            int p1 = html.indexOf("Latest Post");
            int p2 = html.indexOf("<article", p1);
            int p3 = html.indexOf("</article>", p2);
            String tmpHtml = "";
            while (counter < 8 && p2 != -1) {
                tmpHtml = html.substring(p2, p3 + "</article>".length());
                htmlArticleLL.add(tmpHtml);
                p2 = html.indexOf("<article", p3);
                p3 = html.indexOf("</article>", p2);
                counter = counter + 1;
                //System.out.println(tmpHtml);
            }
        } catch (Exception e) {
            errorLog.add(formatException(e));
            htmlArticleLL = new LinkedList<String>();
        }
        return htmlArticleLL;
    }

    @Override
    public List<String> scrapeVideoLinkTags(String html) {
        List<String> videoLinkTagsLL = new LinkedList<String>();
        try {
            if (html.indexOf("mpp-toc toc") == -1) {
                videoLinkTagsLL.add("Full Match / Show");
                return videoLinkTagsLL;
            }
            int p1 = html.indexOf("entry-content");
            p1 = html.indexOf("<p>", p1 + 30);
            int p2 = html.indexOf("<", p1 + 3);
            String tag = html.substring(p1 + 3, p2);

            //System.out.println("tmp tag: " + tag);
            // sometimes this tag will contain the whole
            // title, and not the tag, in which case we
            // need to take the 'Intro' tag from the ul
            // below, even if it doesn't have a link!
            // tagAtTop (true) means there is a tag before
            // the ul ... this is the default.
            boolean tagAtTop = true;
            if (tag.length() < 5 || tag.length() > 15) {
                tagAtTop = false;
                // need to scrape the first item
                // in the ul below, even if it
                // doesn't have a link!
            } else {
                // only add the tag if there is a tag on top
                videoLinkTagsLL.add(tag.trim());
            }

            p1 = html.indexOf("mpp-toc toc", p2);
            p1 = html.indexOf("<ul>", p1);
            int p3 = html.indexOf("</ul>", p1);
            p1 = html.indexOf("<li>", p1);
            p2 = html.indexOf("</li>", p1);
            while (p1 != -1 && p1 < p3) {
                tag = html.substring(p1, p2);
                //System.out.println("tmp tag: " + tag);
                if (tag.indexOf("a href") != -1 || !tagAtTop) {
                    // scrape a tag if either or these conditions are met:
                    // 1) it has a link, i.e. there is a video link here to be scraped; or
                    // 2) it has no link, but there was no tag at the top, so we'll take
                    // the first one in the list (usually, Intro) that doesn't have a link

                    // set the flag to true, otherwise we may scrape furthre tags without links
                    tagAtTop = true;

                    tag = html.substring(html.indexOf("\">", p1) + 2, html.indexOf("</", p1));
                    videoLinkTagsLL.add(tag.trim());
                }
                p1 = html.indexOf("<li>", p2);
                p2 = html.indexOf("</li>", p1);
            }
        } catch (Exception e) {
            errorLog.add(formatException(e));
            videoLinkTagsLL = new LinkedList<String>();
        }
        return videoLinkTagsLL;
    }

    @Override
    public List<String> scrapeVideoLinkPageUrls(String html) {
        List<String> videoLinkPageUrlsLL = new LinkedList<String>();
        try {
            if (html.indexOf("mpp-toc toc") == -1) return videoLinkPageUrlsLL;
            int p1 = html.indexOf("entry-content");
            p1 = html.indexOf("a href", p1);
            int p2 = html.indexOf("\">", p1 + 8);
            int p3 = html.indexOf("</ul>", p1);
            while (p1 != -1 && p1 < p3) {
                String tag = html.substring(p1 + 8, p2);
                //System.out.println("tmp tag: " + tag);
                videoLinkPageUrlsLL.add(tag.trim());
                //System.out.printf("p1: %d, p2: %d, p3: %d \n", p1, p2, p3);
                p1 = html.indexOf("a href", p2);
                p2 = html.indexOf("\">", p1);
            }
        } catch (Exception e) {
            errorLog.add(formatException(e));
            videoLinkPageUrlsLL = new LinkedList<String>();
        }
        return videoLinkPageUrlsLL;
    }
    //
    @Override
    public String scrapeVideoLink(String html, String videoLinkTag) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Element el = null;
            String htmlOriginal = html;
            html = html.toLowerCase();
            // ----
            // Vooplayer
            if(token.equalsIgnoreCase("")) {
                //System.out.printf("[FOS][scrapeVideoLink] Parsing for Vooplayer ... \n");
                el = doc.select("iframe[data-playerId]").first();
                if (el != null) {
                    //System.out.printf("[FOS][scrapeVideoLink] found data-playerId \n");
                    String attr = el.attr("data-playerId");
                    //System.out.printf("[FOS][scrapeVideoLink] data-playerId: [%s] \n", attr);
                    token = "https://app.cdn.vooplayer.com/publish/" + attr;
                } else {
                    //System.out.printf("[FOS][scrapeVideoLink] NOT found data-playerId \n");
                }
            }
            //----
            // Bridplayer
            if(token.equalsIgnoreCase("")) {
                //System.out.printf("[FOS][scrapeVideoLink] Parsing for Bridplayer ... \n");
                if(html.contains("services.brid.tv/player/build/brid.min.js")) {
                    int p1 = html.indexOf("\"video\":\"") + "\"video\":\"".length();
                    int p2 = html.indexOf("\"}", p1);
                    token = html.substring(p1, p2).trim();
                    token = "bridplayer-id:" + token;
                }
            }
            //----
            // Default
            if(token.equalsIgnoreCase("")) {
                //System.out.printf("[FOS][scrapeVideoLink] Parsing for Default videoplayer ... \n");
                el = doc.select("div.player-api iframe").first();
                if (el != null) {
                    String attr = el.attr("src");
                    //System.out.printf("[FOS][scrapeVideoLink] src: [%s] \n", attr);
                    token = attr;
                } else {
                    //System.out.printf("[FOS][scrapeVideoLink] NOT found div.entry-content iframe \n");
                }
            }
            //----
            //System.out.println("Finished scraping for video links, found: [" + token + "]");
            //----
            // truncate at '?' sign
            int p1 = token.indexOf("?");
            //System.out.println("[debug] p1 indexOf '?':        [" + p1 + "]");
            if(p1 != -1) {
                //System.out.println("[debug] removing '?'");
                token = token.substring(0, p1);
                //System.out.println("[debug] token, removed '?':   [" + token + "]");
            }
//            //System.exit(0);

        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        //System.out.printf("[FOS][scrapeVideoLink] videoLink: [%s] \n", token);
        return token;
    }
    //
    @Override
    public List<String> scrapeTags(String html) {
        List<String> tags = new LinkedList<String>();
        try {
            //
        } catch (Exception e) {
            errorLog.add(formatException(e));
            tags = new LinkedList<String>();
        }
        return tags;
    }

}

