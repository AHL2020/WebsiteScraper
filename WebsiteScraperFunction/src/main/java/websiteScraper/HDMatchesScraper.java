package websiteScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.util.*;

public class HDMatchesScraper implements WebsiteScraper {

    private final String URL = "https://hdmatches.net/";
    private List<String> errorLog = null;

    public HDMatchesScraper() {
        errorLog = new LinkedList<>();
    }

    public static void main(String[] args) {
        HDMatchesScraper scraper = new HDMatchesScraper();
        String html = HttpManager.getHtmlFromUrl(scraper.getUrl());
        List<String> articlesHtml = scraper.scrapeHtmlArticle(html);
        //System.out.println(articlesHtml);
        for (int i = 0; i < articlesHtml.size(); i++) {

            //System.out.println("articlesHtml(i): \n" + articlesHtml.get(i));

            String articleKey = scraper.scrapeArticleKey(articlesHtml.get(i));
            System.out.println("articleKey: [" + articleKey + "]");

            String articleTitle = scraper.scrapeArticleTitle(articlesHtml.get(i));
            System.out.println("articleTitle: [" + articleTitle + "]");

            String articleDate = scraper.scrapeArticleDate(articlesHtml.get(i));
            System.out.println("articleDate: [" + articleDate + "]");

            String articleImageUrl = scraper.scrapeArticleImageUrl(articlesHtml.get(i));
            System.out.println("articleImageUrl: [" + articleImageUrl + "]");

            String articleLinkUrl = scraper.scrapeArticleLinkUrl(articlesHtml.get(i));
            System.out.println("articleLinkUrl: [" + articleLinkUrl + "]");

            String articleHtml = HttpManager.getHtmlFromUrl(articleLinkUrl);
            System.out.println("articleHtml: [" + articleHtml + "]");

            String matchDate = scraper.scrapeMatchDate(articleHtml);
            System.out.println("matchDate: [" + matchDate + "]");

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
            errorLog.add("scrapeToken: " + formatException(e));
            token = "";
        }
        return token;
    }

    @Override
    public List<String> scrapeHtmlArticle(String html) {
        List<String> htmlArticleLL = new LinkedList<>();
        try {
            Document doc = Jsoup.parse(html);
            Elements articles = doc.select("div.td-block-span4");
            for(Element article: articles) {
                String articleHtml = article.html();
                //System.out.println(articleHtml);
                htmlArticleLL.add(articleHtml);
            }
            Collections.reverse(htmlArticleLL);
        } catch (Exception e) {
            errorLog.add("scrapeHtmlArticle: " + formatException(e));
            htmlArticleLL = new LinkedList<>();
        }
        //System.out.printf("scraped %d articles \n", htmlArticleLL.size());
        return htmlArticleLL;
    }

    @Override
    public String scrapeArticleKey(String html) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Element link = doc.select("h3 > a").first();
            token = link.attr("href");
            //System.out.println("url: " + token);
            int p = token.lastIndexOf("/");
            boolean isLast = p == token.length() - 1;
            if (isLast) {
                token = token.substring(0, token.length() - 1);
            }
            p = token.lastIndexOf("/");
            token = token.substring(p + 1);
            //System.out.println("url: " + token);
            token = token.replaceAll("video-", "");
        } catch (Exception e) {
            errorLog.add("scrapeArticleKey: " + formatException(e));
            token = "";
        }
        //System.out.println("[HDMatchesScraper][scrapeArticleKey] key: [" + token + "]");
        return token;
    }

    @Override
    public String scrapeArticleTitle(String html) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Element link = doc.select("h3 > a").first();
            token = link.attr("title");
        } catch (Exception e) {
            errorLog.add("scrapeArticleTitle: " + formatException(e));
            token = "";
        }
        //System.out.println("[HDMatchesScraper][scrapeArticleTitle] title: [" + token + "]");
        return token;
    }

    @Override
    public String scrapeArticleDate(String html) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Element link = doc.select("time").first();
            token = link.attr("datetime");
            Instant instant = DateManager.toInstant(token);
            token = instant.toString();

        } catch (Exception e) {
            errorLog.add("scrapeArticleDate: " + formatException(e));
            token = "";
        }
        //System.out.println("[HDMatchesScraper][scrapeArticleDate] article date: [" + token + "]");
        return token;
    }

    @Override
    public String scrapeArticleImageUrl(String html) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Element link = doc.select("img.entry-thumb").first();
            token = link.attr("data-img-url");
        } catch (Exception e) {
            errorLog.add("scrapeArticleImageUrl: " + formatException(e));
            token = "";
        }
        //System.out.println("[HDMatchesScraper][scrapeArticleImageUrl] image-url: [" + token + "]");
        return token;
    }

    @Override
    public String scrapeArticleLinkUrl(String html) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            Element link = doc.select("div.td-module-thumb > a").first();
            token = link.attr("href");
        } catch (Exception e) {
            errorLog.add("scrapeArticleLinkUrl: " + formatException(e));
            token = "";
        }
        //System.out.println("[HDMatchesScraper][scrapeArticleLinkUrl] url: [" + token + "]");
        return token;
    }

    @Override
    public String scrapeMatchDate(String html) {
        String token = "";
        Document doc = Jsoup.parse(html);
        try {
            token = scrapeToken(html, "Date : ", "</strong>").replaceAll("-", " ").trim();
            token = DateManager.formatDate(DateManager.extractDate(token));
        } catch (Exception e) {
            errorLog.add("scrapeMatchDate: " + formatException(e));
            token = "";
        }
        if(token.equalsIgnoreCase("")) {
            try {
                String title = doc.select("title").first().html().replaceAll("-", " ");
                token = DateManager.formatDate(DateManager.extractDate(title));
            } catch (Exception e) {
                errorLog.add("scrapeMatchDate: " + formatException(e));
                token = "";
            }
        }
        //System.out.println("[HDMatchesScraper][scrapeMatchDate] match date: [" + token + "]");
        return token;
    }

    @Override
    public String scrapeMatchCompetition(String html) {
        String token = "";
        boolean found = false;
        try {
            Document doc = Jsoup.parse(html);
            token = doc.select("div.td-post-header > ul > li > a").first().html().trim();
            String[] leagues = {
                    "Premier League"
                    , "La Liga"
                    , "Bundesliga"
                    , "Serie A"
                    , "Ligue 1"
                    , "Champions League"
                    , "Europa League"
            };
            for(String s: leagues) {
                if(!found && token.contains(s)) {
                    token = s;
                    found = true;
                }
            }
            if(!found) {
                String title = doc.select("title").first().html();
                for(String s: leagues) {
                    if(!found && title.contains(s)) {
                        token = s;
                    }
                }
            }
            if(!found) {
                token = "";
            }
        } catch (Exception e) {
            errorLog.add("scrapeMatchCompetition: " + formatException(e));
            token = "";
        }
        //System.out.println("[HDMatchesScraper][scrapeMatchCompetition] comp: [" + token + "]");
        return token;
    }

    @Override
    public String scrapeMatchWeek(String html) {
        // this website doesn't provide match week info
        return "";
    }

    private Map<String, String> scrapeTeams(String html) {
        Map<String, String> teams = new HashMap<>();
        String homeTeam = "";
        String awayTeam = "";
        try {
            String t1 = "<title>";
            String t2 = "</title>";
            String token = scrapeToken(html, t1, t2);
            token = token.replaceAll("\\p{Pd}", "-");

            String[] tags = {" VS ", " vs ", " V ", " v "};
            String[] delims = {" -", "Highlight"};
            boolean isMatch = false;

            // check if it's a match with home and away team
            boolean found = false;
            int count = 0;
            int tagPos = -1;
            do {
                if (token.indexOf(tags[count]) != -1) {
                    tagPos = token.indexOf(tags[count]);
                    found = true;
                }
                count = count + 1;
            } while (!found && count < tags.length);

            // find the delimiter and its position
            if(found) {
                int delimPos = 999;
                for(String del: delims) {
                    int pos = token.indexOf(del);
                    if(pos != -1 && pos < delimPos) {
                        delimPos = pos;
                    }
                }
                if(delimPos < tagPos) {
                    System.out.println("[HDMatchesScraper][scrapeTeams] error: delimPos < tagPos");
                } else {
                    token = token.substring(0, delimPos);
                    // safeguard against scraping issues
                    if(token.length() < 60) {
                        isMatch = true;
                    }
                }
            }
            //System.out.println("[HDMatchesScraper][scrapeTeams] token: [" + token + "]");

            if(isMatch) {
                homeTeam = token.substring(0, tagPos).trim();
                teams.put("homeTeam", homeTeam.trim());
                awayTeam = token.substring(tagPos + tags[count].length()).trim();
                teams.put("awayTeam", awayTeam.trim());
            }
        } catch (Exception e) {
            errorLog.add("scrapeTeams: " + formatException(e));
            teams = new HashMap<>();
        }
        //System.out.println("[HDMatchesScraper][scrapeTeams] teams: [" + teams + "]");
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
    public String scrapeStadium(String html) {
        String token = "";
        try {
            //
        } catch (Exception e) {
            errorLog.add("scrapeStadium: " + formatException(e));
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
            errorLog.add("scrapeReferee: " + formatException(e));
            token = "";
        }
        return token.trim();
    }

    @Override
    public String scrapeYear(String html) {
        String token = "";
        try {
            token = scrapeToken(html, "<title>", "</title>").replaceAll("-", "").trim();
            Map<String, String> dateParts = DateManager.extractDate(token);
            token = dateParts.get("year");
        } catch (Exception e) {
            errorLog.add(formatException(e));
            token = "";
        }
        if (token == null) token = "";
        //System.out.println("[HDMatchesScraper][scrapeYear] year: [" + token + "]");
        return token;
    }

    @Override
    public List<String> scrapeVideoLinkTags(String html) {
        List<String> videoLinkTagsLL = new LinkedList<>();
        try {

            // if there are multiple video links
            Document doc = Jsoup.parse(html);
            Elements els = doc.select("div.vc_tta-panel-heading > h4 > a > span");
            for(Element el: els) {
                String token = el.text().trim();
                videoLinkTagsLL.add(token);
            }

            // if there is only 1 video link
            if(videoLinkTagsLL.size() == 0) {
                videoLinkTagsLL.add("Full Show / Match");
            }

        } catch (Exception e) {
            errorLog.add("scrapeVideoLinkTags: " + formatException(e));
            videoLinkTagsLL = new LinkedList<>();
        }
        //System.out.println("[HDMatchesScraper][scrapeVideoLinkTags] videoLinkTagsLL: [" + videoLinkTagsLL + "]");
        return videoLinkTagsLL;
    }

    @Override
    public List<String> scrapeVideoLinkPageUrls(String html) {
        List<String> videoLinkPageUrlsLL = new LinkedList<>();
        try {
            Document doc = Jsoup.parse(html);
            String url = doc.select("link[rel=canonical]").first().attr("href").trim();
            //System.out.println("[HDMatchesScraper][scrapeVideoLinkPageUrls] url: [" + url + "]");
            Elements els = doc.select("div.vc_tta-panel-heading > h4 > a");
            if(els.size() > 1) {
                int count = 0;
                for (Element el : els) {
                    if(count > 0) {
                        String token = el.attr("href").trim();
                        videoLinkPageUrlsLL.add(url + token);
                        //System.out.println("[HDMatchesScraper][scrapeVideoLinkPageUrls] token: [" + token + "]");
                    }
                    count++;
                }
            }
        } catch (Exception e) {
            errorLog.add("scrapeVideoLinkPageUrls: " + formatException(e));
            videoLinkPageUrlsLL = new LinkedList<>();
        }
        //System.out.println("[HDMatchesScraper][scrapeVideoLinkPageUrls] videoLinkPageUrlsLL: [" + videoLinkPageUrlsLL + "]");
        return videoLinkPageUrlsLL;
    }
    //
    @Override
    public String scrapeVideoLink(String html, String videoLinkTag) {
        String token = "";
        try {
            Document doc = Jsoup.parse(html);
            // check how many tags there are --> to determine how many links we need to scrape
            // and to find the index of the tag whose link we're scraping
            Elements els = doc.select("div.vc_tta-panel-heading > h4 > a > span");
            //System.out.println("[HDMatchesScraper][scrapeVideoLinkPageUrls] els.size(): " + els.size());
            if(els.size() < 2) {
                token = doc.selectFirst("iframe").attr("src").trim();
            } else {
                // find which index the video link has got
                // for the videoLinkTag that is passed in
                int idx = -1;
                int cnt = 0;
                for(Element el: els) {
                    if(el.text().trim().equalsIgnoreCase(videoLinkTag)) {
                        idx = cnt;
                    }
                    cnt++;
                }
                if(idx == -1) {
                    errorLog.add("scrapeVideoLink: error scraping video link");
                } else {
                    // based on the index, find the link
                    els = doc.select("div.td-fix-index > iframe");
                    cnt = 0;
                    for (Element el : els) {
                        if (idx == cnt) {
                            token = el.attr("src").trim();
                        }
                        cnt++;
                    }
                }
            }
        } catch (Exception e) {
            errorLog.add("scrapeVideoLink: " + formatException(e));
            token = "";
        }
        //System.out.printf("[HDMatchesScraper][scrapeVideoLink] videoLink: [%s] \n", token);
        return token;
    }
    //
    @Override
    public List<String> scrapeTags(String html) {
        List<String> tags = new LinkedList<>();
        try {
            //
        } catch (Exception e) {
            errorLog.add("scrapeTags: " + formatException(e));
            tags = new LinkedList<>();
        }
        return tags;
    }
}

