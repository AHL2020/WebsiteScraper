package websiteScraper;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class S3Manager {
    public enum DeploymentType {LOCAL, CLOUD};
    private DeploymentType deploy = DeploymentType.LOCAL;
    private static S3Manager instance = null;
    private AmazonS3 s3 = null;
    private Regions region = Regions.US_EAST_1;

    // for testing
    public static void main(String[] args) {

        S3Manager s3Manager = S3Manager.getInstance(DeploymentType.LOCAL, Regions.US_EAST_1);

        String s3Bucket = ConfigManager.S3_BUCKET;

        /*

        boolean success = false;

        String objectUrl1 = "https://i1.wp.com/fullmatchesandshows.com/wp-content/uploads/2020/05/Euro96-relieved-324x160.png";
        String objectKey1 = "TEST_test-image-1.png";
        String contentType1 = "image/png";

        success = s3Manager.saveObjectFromUrl(objectUrl1, contentType1, s3Bucket, objectKey1);
        System.out.println("s3 test 1 success: " + success);

        String objectUrl2 = "https://i1.wp.com/footballfullmatch.com/wp-content/uploads/2020/02/full-match-liverpool-vs-southampton.jpg";
        String objectKey2 = "TEST_test-image-2.jpg";
        String contentType2 = "image/jpg";

        success = s3Manager.saveObjectFromUrl(objectUrl2, contentType2, s3Bucket, objectKey2);
        System.out.println("s3 test 2 success: " + success);

        */
    }

    private S3Manager(DeploymentType type, Regions region) {
        this.deploy = type;
        s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
    }

    public static S3Manager getInstance(DeploymentType type, Regions region) {
        if(S3Manager.instance == null) {
            S3Manager.instance = new S3Manager(type, region);
        }
        return instance;
    }

    public BufferedImage loadBufferedImage(String s3Bucket, String objectKey) {
        System.out.println("[S3Manager][loadBufferedImage] s3Bucket: " + s3Bucket);
        System.out.println("[S3Manager][loadBufferedImage] objectKey: " + objectKey);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(s3.getObject(
                new GetObjectRequest(s3Bucket, objectKey)).getObjectContent());
        } catch(Exception e) {
            e.printStackTrace();
            bufferedImage = null;
        }
        return bufferedImage;
    }

    // returns the following keys:
    // (1) each club name variation is a key and maps to an image name:
    // e.g. Bayern -> clublogos/Bundesliga/bayern.png
    //      FC Bayern -> clublogos/Bundesliga/bayern.png
    // (2) also each category maps to a canvas file
    // e.g. Bundesliga -> canvas01.png
    // it is assumed that all paths are relative to /images on S3
    public Map<String, String> loadClubNamesLogosConfig(String s3Bucket, String objectKey) {
        Map<String, String> config = new HashMap<String, String>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3.getObject(
                    new GetObjectRequest(s3Bucket, objectKey)).getObjectContent()));
            String line = "";
            String currentCategory = "";
            String currentCanvas = "";
            String currentImageName = "";
            String currentClubName = "";
            while((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                boolean emptyLine = line.length() == 0;
                boolean comment = false;
                boolean valid = false;
                if(!emptyLine) {
                    if(line.charAt(0) == '#') {
                        comment = true;
                    }
                    if(line.contains("=")) {
                        String[] parts = line.split("=");
                        if(parts.length == 2) {
                            valid = true;
                        }
                    }
                }
                if(!emptyLine && !comment && valid) {
                    //System.out.println("[S3Manager][loadClubNamesLogosConfig] Parsing: " + line);
                    String[] parts = line.split("=");
                    if(line.contains("category=")) {
                        currentCategory = parts[1].trim();
                        //System.out.println("[S3Manager][loadClubNamesLogosConfig] Found category: " + currentCategory);
                    }
                    if(line.contains("canvas_image_name=")) {
                        currentCanvas = parts[1].trim();
                        //System.out.println("[S3Manager][loadClubNamesLogosConfig] Found/put canvas: " + currentCanvas);
                        config.put(currentCategory, currentCanvas);
                    }
                    if(line.contains("logo_image_name=")) {
                        currentImageName = parts[1].trim();
                        //System.out.println("[S3Manager][loadClubNamesLogosConfig] Found imageName: " + currentImageName);
                    }
                    if(line.contains("club_name=")) {
                        currentClubName = parts[1].trim();
                        //System.out.println("[S3Manager][loadClubNamesLogosConfig] Found/put clubName: " + currentClubName);
                        config.put(currentClubName, currentImageName);
                    }

                } else {
                    //System.out.println("[S3Manager][loadClubNamesLogosConfig] Empty or comment or invalid: " + line);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            config = null;
        }
        return config;
    }

    public boolean saveObjectFromUrl(String objectUrl, String contentType, String s3Bucket, String objectKey) {
        File fileToPut = null;
        URLConnection conn = null;
        try {

            // set file location and create file
            if (deploy == DeploymentType.LOCAL) {
                fileToPut = new File(objectKey);
            } else {
                fileToPut = new File(("/tmp/" + objectKey).replaceAll("//", "/"));
            }

            // open connection and copy file
            System.out.println("[S3Manager][saveObjectFromUrl] objectUrl: " + objectUrl);
            conn = new URL(objectUrl).openConnection();
            conn.connect();
            InputStream inputStream = conn.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, fileToPut);

            //String contentType = "";
            //String fileExtension = objectKey.substring(objectKey.lastIndexOf(".") + 1);
            //if (fileExtension.equals("jpg")) contentType = "image/jpeg";
            //if (fileExtension.equals("jpeg")) contentType = "image/jpeg";
            //if (fileExtension.equals("png")) contentType = "image/png";

            // save object to S3
            ObjectMetadata metaData = new ObjectMetadata();
            metaData.setContentType(contentType);
            PutObjectRequest putObjReq =
                    new PutObjectRequest(s3Bucket, objectKey, fileToPut)
                            .withCannedAcl(CannedAccessControlList.PublicRead);
            putObjReq.setMetadata(metaData);
            if (!s3.doesObjectExist(s3Bucket, objectKey)) {
                s3.putObject(putObjReq);
            }
            fileToPut.delete(); // delete the files that are stored locally
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            fileToPut.delete();
        }
    }
    //
    public InputStream loadObject(String s3Bucket, String objectKey) {
        InputStream inputStream = null;
        try {
            S3Object s3Object = s3.getObject(new GetObjectRequest(s3Bucket, objectKey));
            inputStream = s3Object.getObjectContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }
}