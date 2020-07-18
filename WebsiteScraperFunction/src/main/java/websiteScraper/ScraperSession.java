package websiteScraper;

public class ScraperSession {
    //
    private WebsiteScraper scraper;
    private String sessionName;
    private Anonymiser anonymiser;
    //
    public ScraperSession(WebsiteScraper scraper, String sessionName, Anonymiser anonymiser) {
        this.scraper = scraper;
        this.sessionName = sessionName;
        this.anonymiser = anonymiser;
    }
    //
    public WebsiteScraper getScraper() {
        return scraper;
    }
    //
    public void setScraper(WebsiteScraper scraper) {
        this.scraper = scraper;
    }
    //
    public String getSessionName() {
        return sessionName;
    }
    //
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    //
    public Anonymiser getAnonymiser() { return this.anonymiser; }
    //
}
