package websiteScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HttpManager {
    public static String getHtmlFromUrl(String url) {
        Document doc = null;
        String html = "";
        try {
            doc = Jsoup.connect(url).get();
            html = doc.html();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return html;
    }
}
