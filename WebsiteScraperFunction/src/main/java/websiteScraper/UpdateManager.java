package websiteScraper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateManager {
    //
    private static UpdateManager instance = null;
    //
    private final Map<String, String> updateLog;
    //
    private Map<String, String> existingArticle = null;
    private Map<String, String> newArticle = null;
    //
    private UpdateManager() {
        this.updateLog = new HashMap<>();
    }
    //
    public static UpdateManager getInstance() {
        if(UpdateManager.instance == null) {
            UpdateManager.instance = new UpdateManager();
        }
        return UpdateManager.instance;
    }
    //
    public Map<String, String> getUpdateLog() {
        return this.updateLog;
    }
    //
    public String getUpdateLogString() {
        return this.updateLog.toString();
    }
    // the existing article to be updated
    public UpdateManager setExistingArticle(Map<String, String> existingArticle) {
        this.existingArticle = existingArticle;
        return this;
    }
    // the new article
    public UpdateManager setNewArticle(Map<String, String> newArticle) {
        this.newArticle = newArticle;
        return this;
    }
    //
    /**
     * Updates the existingArticle, with additional/new details from newArticle
     * @return existingArticle, with updated details
     */
    public Map<String, String> updateArticle() {
        if(existingArticle == null || newArticle == null) {
            return new HashMap<>();
        }
        if(existingArticle.size() == 0 || newArticle.size() == 0) {
            return new HashMap<>();
        }
        mergeVideoLinks();
        mergeMatchTags();

        // removing the 'createdDate' flag forces the website builder
        // to re-build the page(s) for this article.
        // But: only force the page re-build IF there were any updates
        // to the article!
        if(updateLog.size() > 0) {
            existingArticle.put("createdDate", null);
        }

        //String createdDate =  existingArticle.get("createdDate");
        //System.out.println("[UpdateManager][updateArticle] createdDate: " + createdDate);

        // todo: set the pageCreatedDate in the article to "" or null, so that the builder
        // todo: knows it must be re-created

        return existingArticle;
    }
    //
    private void mergeVideoLinks() {
        // todo: if merging highlights and full match replay -> update the title
        // todo: e.g. from 'Highlights' to 'Full Match & Highlights'
        String videoLinksExisting = existingArticle.get("matchVideoLinks");
        String videoLinksNew = newArticle.get("matchVideoLinks");
        String[] videoLinksExistingArr = videoLinksExisting.split(",");
        String[] videoLinksNewArr = videoLinksNew.split(",");
        List<String> videoLinksLL = new LinkedList<>();
        for(int i = 0; i < videoLinksNewArr.length; i = i + 2) {
            boolean exists = false;
            String newLink = "";
            String newTag = "";
            String existingLink;
            for(int j = 0; j < videoLinksExistingArr.length; j = j + 2) {
                newLink = videoLinksNewArr[i].trim();
                newTag = videoLinksNewArr[i+1].trim();
                existingLink = videoLinksExistingArr[j].trim();
                if(newLink.equalsIgnoreCase(existingLink)) {
                    exists = true;
                }
            }
            if(!exists) {
                videoLinksLL.add(newLink);
                videoLinksLL.add(newTag);
            }
        }
        int newVideoLinks = videoLinksLL.size() / 2;
        if(newVideoLinks > 0) {
            for(int i = 0; i < videoLinksLL.size(); i = i + 2) {
                videoLinksExisting = videoLinksExisting + "," + videoLinksLL.get(i) + "," + videoLinksLL.get(i+1);
            }
            updateLog.put("updated-videolinks", videoLinksLL.toString());
        }
        existingArticle.put("matchVideoLinks", videoLinksExisting);
    }
    //
    private void mergeMatchTags() {
        Map<String, String> combinedTags = new HashMap<>();
        String tagsExistingStr = existingArticle.get("matchTags");
        if(tagsExistingStr != null) {
            //System.out.println("tagsExistingStr: " + tagsExistingStr);
            //System.out.println("tagsExistingStr.length: " + tagsExistingStr.split(",").length);
            String[] tagsArr = tagsExistingStr.split(",");
            for(int i = 0; i < tagsArr.length; i++) {
                String s = tagsArr[i].trim();
                if(s.length() > 0) {
                    combinedTags.put(s, s);
                }
            }
        }
        String tagsNewStr = newArticle.get("matchTags");
        if(tagsNewStr != null) {
            //System.out.println("tagsNewStr: " + tagsNewStr);
            //System.out.println("tagsNewStr.length: " + tagsNewStr.split(",").length);
            String[] tagsArr = tagsNewStr.split(",");
            for(int i = 0; i < tagsArr.length; i++) {
                String s = tagsArr[i].trim();
                if(s.length() > 0) {
                    combinedTags.put(s, s);
                }
            }
        }
        //for(String s: combinedTags.keySet()) {
        //    System.out.print(s + ",");
        //}
        //System.out.println();
        if(combinedTags.size() > 0) {
            String combinedTagsStr = combinedTags.keySet().toString().replaceAll("\\[", "").replaceAll("\\]", "");
            existingArticle.put("matchTags", combinedTagsStr);
            updateLog.put("updated-matchTags", combinedTags.keySet().toString());
        }
    }
}