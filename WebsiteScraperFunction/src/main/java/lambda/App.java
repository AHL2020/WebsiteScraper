package lambda;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import websiteScraper.GatewayResponse;
import websiteScraper.LegacyScraperManager;
import websiteScraper.S3Manager;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Object, Object> {

    public Object handleRequest(final Object input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        try {
            // config
            S3Manager.DeploymentType type = S3Manager.DeploymentType.CLOUD;
            Regions region = Regions.US_EAST_1;
            String bucketName = "my-sports-website";
            String databaseKey = "data/database.csv";
            // run
            LegacyScraperManager.run(type, region, bucketName, databaseKey);
            boolean success = true;
            String output = String.format("{ \"message\": \"LegacyScraperManager\", \"Run\": \"%b\" }", success);
            return new GatewayResponse(output, headers, 200);
        } catch (Exception e) {
            return new GatewayResponse("{}", headers, 500);
        }
    }
}
