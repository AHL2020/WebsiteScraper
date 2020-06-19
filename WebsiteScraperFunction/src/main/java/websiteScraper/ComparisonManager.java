package websiteScraper;

import com.amazonaws.regions.Regions;

import java.util.List;
import java.util.Map;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class ComparisonManager {
    //
    private static ComparisonManager instance = null;
    private Map<String, String> clubNamesLogosConfig = null;
    //
    public static void main(String[] args) {

        String s3Bucket = "my-sports-website";
        String mappingCfgObjKey = "config/clubs-names-logos.cfg";
        String databaseKey = "data/database.csv";

        S3Manager s3Manager = S3Manager.getInstance(S3Manager.DeploymentType.LOCAL, Regions.US_EAST_1);
        ComparisonManager comparisonManager = ComparisonManager.getInstance(s3Manager.loadClubNamesLogosConfig(s3Bucket, mappingCfgObjKey));
        List<Map<String, String>> articles = LegacyIntegration.loadArticlesFromS3(s3Bucket, databaseKey);
        ValidationManager validationManager = ValidationManager.getInstance();

        System.out.println("loaded articles: [" + articles.size() + "]");

        for(Map<String, String> article: articles) {
            String searchClubName = article.get("matchAwayTeam");
            String matchCompetition = article.get("matchCompetition");
            String clubKey = "";
            if(validationManager.isValidTeam(searchClubName)) {
                clubKey = comparisonManager.findClubKey(searchClubName);
                if(!clubKey.equalsIgnoreCase("")) {
                    System.out.println("matched searchClubName: " + searchClubName + ", clubKey: " + clubKey);
                } else if(matchCompetition.equalsIgnoreCase("Bundesliga")) {
                    System.out.println("cannot match searchClubName: " + searchClubName);
                }
            }
        }
    }
    //
    private ComparisonManager(Map<String, String> clubNamesLogosConfig) {
        this.clubNamesLogosConfig = clubNamesLogosConfig;
    }
    //
    public static ComparisonManager getInstance(Map<String, String> clubNamesLogosConfig) {
        if(ComparisonManager.instance == null) {
            ComparisonManager.instance = new ComparisonManager(clubNamesLogosConfig);
        }
        return ComparisonManager.instance;
    }
    //
    public boolean equalsArticle(Map<String, String> article1, Map<String, String> article2) {
        boolean equals = true;
        //
        return equals;
    }
    //
    public String findClubKey(String searchClubName) {
        String clubKey = "";
        LevenshteinDistance lsdObj = LevenshteinDistance.getDefaultInstance();
        //System.out.println("[ComparisonManager][findClubKey] searchClubName: " + searchClubName);
        int lsdInt = 0;
        for(Map.Entry entry: clubNamesLogosConfig.entrySet()) {
            String tmpClubName = (String)entry.getKey();
            String tmpClubKey = (String)entry.getValue();
            //System.out.println("[ComparisonManager][findClubKey] tmpClubName: " + tmpClubName);
            //System.out.println("[ComparisonManager][findClubKey] tmpClubKey: " + tmpClubKey);
            lsdInt = lsdObj.apply(searchClubName, tmpClubName);
            //System.out.println("[ComparisonManager][findClubKey] lsdInt: " + lsdInt);
            // exact match
            if(lsdInt==0) {
                return tmpClubKey;
            }
        }
        return clubKey;
    }
}
