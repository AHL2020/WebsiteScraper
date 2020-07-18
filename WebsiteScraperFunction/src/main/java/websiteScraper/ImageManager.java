package websiteScraper;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 *  TODO: work in progress
 *
 */
public class ImageManager {

    public static void main(String[] args) throws IOException {

        //process(null, null, null);

        // todo: make method and pass in S3Manager object

        //String homeTeam = "Union Berlin";
        //String homeTeam = "SC Paderborn";
        String homeTeam = "Koeln";
        String awayTeam = "Bayern München";
        String category = "Bundesliga";

        S3Manager s3Manager = S3Manager.getInstance(S3Manager.DeploymentType.LOCAL, Regions.US_EAST_1);
        String s3Bucket = ConfigManager.S3_BUCKET;
        String mappingCfgObjKey = "config/clubs-names-logos.cfg";

        Map<String, String> mappingConfig = s3Manager.loadClubNamesLogosConfig(s3Bucket, mappingCfgObjKey);
        String homeLogo = mappingConfig.get(homeTeam);
        String awayLogo = mappingConfig.get(awayTeam);
        String canvas = mappingConfig.get(category);

        System.out.println("homeLogo: " + homeLogo);
        System.out.println("awayLogo: " + awayLogo);
        System.out.println("canvas: " + canvas);

        if(homeLogo != null && awayLogo != null && canvas != null) {

            // load BufferedImage from S3, for each logo and the canvas
            BufferedImage imHomeLogo = s3Manager.loadBufferedImage(s3Bucket, homeLogo);
            BufferedImage imAwayLogo = s3Manager.loadBufferedImage(s3Bucket, awayLogo);
            BufferedImage imCanvas = s3Manager.loadBufferedImage(s3Bucket, canvas);

            System.out.println("imHomeLogo: " + imHomeLogo);
            System.out.println("imAwayLogo: " + imHomeLogo);
            System.out.println("imCanvas: " + imHomeLogo);

            // pass in the 3 BufferedImage objects to process() and get the processed BufferedImage returned
            BufferedImage articleImage = process(imHomeLogo, imAwayLogo, imCanvas);
            System.out.println("articleImage: " + articleImage);

            // pass the BufferedImage to S3 for storing
        }

        //Graphics2D image = generateArticleImage(homeTeam, awayTeam, category);
        // todo: save Graphics2D image to S3


    }



    private static String matchTeamName(List<String> allNames, String searchName) {
        String matchedName = "";
        int[] similarity = new int[allNames.size()];
        int maxSim = -1;
        int maxPos = -1;
        for(int i = 0; i < allNames.size(); i++) {
            int sim = 0; // todo: calculate the Levenshtein distance
            similarity[i] = sim;
            if(sim > maxSim) {
                maxSim = sim;
                maxPos = i;
            }
        }
        if(maxPos != -1) {
            matchedName = allNames.get(maxPos);
        } else {
            //
        }
        return matchedName;
    }

    // todo: return the BufferedImage to the completed image, then make a method in S3Manager to store it to S3
    private static BufferedImage process(BufferedImage imLogo1, BufferedImage imLogo2, BufferedImage imCanvas) {

        //display(imLogo1);
        //display(imLogo2);
        //display(imCanvas);

        BufferedImage processedImage = null;

        try {
            double logoW = 150.0;
            double logoH = 150.0;
            int logoOffsetX = 100;
            int logoOffsetY = 100;

            //imCanvas = ImageIO.read(new File("canvas02.png"));
            //URL url = new URL("http://sstatic.net/so/img/logo.png");
            //BufferedImage im = ImageIO.read(url);
            //imLogo1 = ImageIO.read(new File("logo01.png"));
            //URL url2 = new URL("http://sstatic.net/sf/img/logo.png");
            //BufferedImage im2 = ImageIO.read(url2);
            //imLogo2 = ImageIO.read(new File("logo02.png"));

            logoW = 150.0;
            logoH = 150.0;
            if(imLogo1.getWidth() > imLogo1.getHeight()) {
                logoW = logoW * imLogo1.getWidth() / imLogo1.getHeight();
            }
            if(imLogo1.getWidth() < imLogo1.getHeight()) {
                logoH = logoH * imLogo1.getHeight() / imLogo1.getWidth();
            }
            BufferedImage imLogo1resized = resize(imLogo1, logoW, logoH);

            logoW = 150.0;
            logoH = 150.0;
            if(imLogo2.getWidth() > imLogo2.getHeight()) {
                logoW = logoW * imLogo2.getWidth() / imLogo2.getHeight();
            }
            if(imLogo2.getWidth() < imLogo2.getHeight()) {
                logoH = logoH * imLogo2.getHeight() / imLogo2.getWidth();
            }
            BufferedImage imLogo2resized = resize(imLogo2, logoW, logoH);

            Graphics2D imLogo1alpha = imLogo1resized.createGraphics();
            imLogo1alpha.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

            //Graphics2D g = im.createGraphics();
            Graphics2D g = imCanvas.createGraphics();
            //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            int logo2pos = imCanvas.getWidth() - (int) logoW - logoOffsetX;
            g.drawImage(imLogo1resized, logoOffsetX, logoOffsetY, null);
            g.drawImage(imLogo2resized, logo2pos, logoOffsetY, null);
            //g.drawImage(im2, (im.getWidth()-im2.getWidth())/2, (im.getHeight()-im2.getHeight())/2, null);
            g.dispose();

            display(imCanvas);
            ImageIO.write(imCanvas, "png", new File("sample_output.png"));

            processedImage = imCanvas;

        } catch(Exception e) {
            System.out.println("[error] ImageManager.process");
            e.printStackTrace();
            processedImage = null;
        }

        return processedImage;
    }

    public static void display(BufferedImage image) {
        JFrame f = new JFrame("WaterMark");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new JLabel(new ImageIcon(image)));
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static BufferedImage resize(BufferedImage image, double width, double height) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        double scaleX = width/w;
        double scaleY = height/h;
        at.scale(scaleX, scaleY);
        AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        resized = scaleOp.filter(image, resized);
        return resized;
    }
}
