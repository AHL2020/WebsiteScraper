package websiteScraper;

import com.amazonaws.regions.Regions;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    public static final String S3_BUCKET = "my-sports-website";
    public static final String DB_KEY = "data/database.csv";
    public static final String CONFIG_FILE_LOCAL = "C:\\Users\\Anders\\dev\\IdeaProjects\\WebsiteScraper\\WebsiteScraperFunction\\src\\main\\resources\\config.xml";
    public static final String CONFIG_FILE_CLOUD = "config/config.xml";
    public static final String LANG_EN = "en";
    public static final String LANG_CN = "cn";
    private static ConfigManager instance = null;
    private List<String> log = null;
    Map<String, String> clubsNotFound = null;
    private Document xmlDocument = null;
    private XPath xPath = null;

    public static void main(String[] args) {

        testGetArticleCategoryNames();
        /*
        testGetDefaultArticleCategory();
        testLoadConfigS3();
        testGetClubIdByName();
        testGetS3bucket();
        testGetDatabaseKey();
        testGetArticleTypeNames();
        testGetDefaultArticleType();
        testGetArticleTypeIdByName();
         */
    }

    public static void testGetArticleCategoryNames() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        System.out.println("[ConfigManager][testGetArticleCategoryNames] loadConfig(): [" + success + "]");
        List<String> articleCategoryNames = configManager.getArticleCategoryNames();
        System.out.println("[ConfigManager][testGetArticleCategoryNames] articleCategoryNames: [" + articleCategoryNames + "]");
    }

    public static void testGetDefaultArticleCategory() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        System.out.println("[ConfigManager][testGetDefaultArticleCategory] loadConfig(): [" + success + "]");
        String defaultArticleCategory = configManager.getDefaultArticleCategory();
        System.out.println("[ConfigManager][testGetDefaultArticleCategory] defaultArticleCategory: [" + defaultArticleCategory + "]");
    }

    public static void testLoadConfigS3() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig(S3Manager.getInstance(S3Manager.DeploymentType.LOCAL, Regions.US_EAST_1));
        System.out.println("[ConfigManager][testLoadConfigS3] loadConfigS3(): [" + success + "]");
    }

    public static void testGetClubIdByName() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        System.out.println("[ConfigManager][testGetClubIdByName] loadConfig(): [" + success + "]");
        String[] testClubNames = {"FC Bayern", "fc bAYERn", "Brighton & Hove Albion", "dsvfdsfd", "", null};
        for(int i = 0; i < testClubNames.length; i++) {
            System.out.printf("[ConfigManager][testGetClubIdByName] testClubNames[%d]: [%s], clubId: [%s] \n", i, testClubNames[i], configManager.getClubIdByName(testClubNames[i]));
        }
    }

    public static void testGetS3bucket() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        System.out.println("[ConfigManager][testGetClubIdByName] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetS3bucket] s3bucket: [%s] \n", configManager.getS3bucket());
    }

    public static void testGetDatabaseKey() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        System.out.println("[ConfigManager][testGetDatabaseKey] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetDatabaseKey] database-key: [%s] \n", configManager.getDatabaseKey());
    }

    public static void testGetArticleTypeNames() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        System.out.println("[ConfigManager][getArticleTypeNames] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][getArticleTypeNames] article-type-names: [%s] \n", configManager.getArticleTypeNames());
    }

    public static void testGetDefaultArticleType() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        System.out.println("[ConfigManager][testGetDefaultArticleType] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetDefaultArticleType] default article-type: [%s] \n", configManager.getDefaultArticleType());
    }

    public static void testGetArticleTypeIdByName() {
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.loadConfig();
        String articleType = "Full Match";
        System.out.println("[ConfigManager][testGetArticleTypeIdByName] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetArticleTypeIdByName] article-type id: [%s] \n", configManager.getArticleTypeIdByName(articleType));
        articleType = "Highlights";
        System.out.println("[ConfigManager][testGetArticleTypeIdByName] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetArticleTypeIdByName] article-type id: [%s] \n", configManager.getArticleTypeIdByName(articleType));
        articleType = "Show";
        System.out.println("[ConfigManager][testGetArticleTypeIdByName] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetArticleTypeIdByName] article-type id: [%s] \n", configManager.getArticleTypeIdByName(articleType));
        articleType = "Pre Match Press Conference";
        System.out.println("[ConfigManager][testGetArticleTypeIdByName] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetArticleTypeIdByName] article-type id: [%s] \n", configManager.getArticleTypeIdByName(articleType));
        articleType = "sfdhds ds h";
        System.out.println("[ConfigManager][testGetArticleTypeIdByName] loadConfig(): [" + success + "]");
        System.out.printf("[ConfigManager][testGetArticleTypeIdByName] article-type id: [%s] \n", configManager.getArticleTypeIdByName(articleType));
    }

    private ConfigManager() {
        this.log = new LinkedList<>();
        this.clubsNotFound = new HashMap<>();
    }

    public static ConfigManager getInstance() {
        if(ConfigManager.instance == null) {
            ConfigManager.instance = new ConfigManager();
        }
        return ConfigManager.instance;
    }

    public boolean loadConfig() {
        if(xPath != null) return true; // check if the config is already loaded
        try {
            return loadConfig(new FileInputStream(ConfigManager.CONFIG_FILE_LOCAL));
        } catch(Exception e) {
            log.add("Error loading/parsing LOCAL config file: " + e.toString());
            return false;
        }
    }

    public boolean loadConfig(S3Manager s3Manager) {
        if(xPath != null) return true; // check if the config is already loaded
        return loadConfig(s3Manager.loadObject(ConfigManager.S3_BUCKET, ConfigManager.CONFIG_FILE_CLOUD));
    }

    private boolean loadConfig(InputStream inputStream) {
        try {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        xmlDocument = builder.parse(inputStream);
        XPathFactory xpathFactory = new net.sf.saxon.xpath.XPathFactoryImpl();
        xPath = xpathFactory.newXPath();
        } catch(Exception e) {
            log.add("Error loading/parsing config file: " + e.toString());
            return false;
        }
        return true;
    }

    public boolean isConfigLoaded() {
        return xPath != null;
    }

    public List<String> getLog() {
        return this.log;
    }

    public Map<String, String> getClubsNotFound() {
        return this.clubsNotFound;
    }

    // performs a 'equalsIgnoreCase' search
    public String getClubIdByName(String clubName) {
        if(clubName == null) return "";
        if(clubName.trim().equalsIgnoreCase("")) return "";
        //
        String clubNameSearch = clubName
                .replaceAll("&amp;", "&")
                .replaceAll("\'", "")
                .replaceAll("’", "")
                .replaceAll("\\p{M}", "");
        //
        String attributeName = "id";
        String clubId = getAttributeValueByXPath(attributeName, "./config/reference-data/clubs/club[./names/name[matches(., '" + clubNameSearch + "', 'i')]]");
        if(clubId.trim().length() == 0) {
            clubsNotFound.put(clubNameSearch, "");
        }
        return clubId;
    }

    public String getClubDefaultNameById(String clubId) {
        return getElementTextValueByXPath("./config/reference-data/clubs/club[@id='" + clubId + "']/names/name[1]");
    }

    private String getAttributeValueByXPath(String attributeName, String xPathQuery) {
        String attrVal;
        try {
            NodeList nodeList = (NodeList) xPath.compile(xPathQuery).evaluate(xmlDocument, XPathConstants.NODESET);
            Node node = nodeList.item(0);
            NamedNodeMap attr;
            attr = node.getAttributes();
            attrVal = attr.getNamedItem(attributeName).getTextContent();
        } catch (Exception e) {
            attrVal = "";
        }
        return attrVal;
    }

    public String getS3bucket() {
        return getElementTextValueByXPath("./config/app/s3bucket");
    }

    public String getDatabaseKey() {
        return getElementTextValueByXPath("./config/app/database-key");
    }

    private String getElementTextValueByXPath(String xPathQuery) {
        String elementTextValue = "";
        try {
            NodeList nodeList = (NodeList) xPath.compile(xPathQuery).evaluate(xmlDocument, XPathConstants.NODESET);
            Node node = nodeList.item(0);
            elementTextValue = node.getTextContent();
        } catch (Exception e) {
            elementTextValue = "";
        }
        return elementTextValue;
    }

    private List<String> getElementTextValuesByXPath(String xPathQuery) {
        List<String> elementTextValues = new LinkedList<>();
        try {
            NodeList nodeList = (NodeList) xPath.compile(xPathQuery).evaluate(xmlDocument, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                elementTextValues.add(node.getTextContent());
            }
        } catch (Exception e) {
            elementTextValues = new LinkedList<>();
        }
        return elementTextValues;
    }

    public List<String> getArticleTypeNames() {
        return getElementTextValuesByXPath("./config/app/article-types/article-type/name");
    }

    public String getArticleTypeIdByName(String articleType) {
        return getAttributeValueByXPath("id", "./config/app/article-types/article-type[./name='"+articleType+"']");
    }

    public String getDefaultArticleType() {
        return getElementTextValueByXPath("./config/app/article-types/article-type[@id='default']/name");
    }

    public String getDefaultArticleCategory() {
        return getElementTextValueByXPath("./config/app/categories/category[@id='default']/name");
    }

    public List<String> getArticleCategoryNames() {
        return getElementTextValuesByXPath("./config/app/categories/category/name");
    }

    public String getCategoryLogoByNameAndLanguage(String categoryName, String language) {
        return getElementTextValueByXPath("./config/app/categories/category[./name='"+categoryName+"']/logo/"+language+"/text()");
    }

    public String getImageCategoriesDir() {
        return getElementTextValueByXPath("./config/app/image-categories-dir");
    }
}
