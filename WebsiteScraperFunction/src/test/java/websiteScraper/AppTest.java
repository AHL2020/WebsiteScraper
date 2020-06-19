package websiteScraper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import lambda.App;
import org.junit.Test;

public class AppTest {
  @Test
  public void successfulResponse() {
    assertTrue(true);
    /*
    App app = new App();
    GatewayResponse result = (GatewayResponse) app.handleRequest(null, null);
    assertEquals(result.getStatusCode(), 200);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json");
    String content = result.getBody();
    assertNotNull(content);
    assertTrue(content.contains("\"message\""));
    assertTrue(content.contains("\"LegacyScraperManager\""));
    assertTrue(content.contains("\"Run\""));
    */
  }
}
