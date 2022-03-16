package petclinic.api.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import petclinic.api.client.owner.OwnerApiClient;


@Slf4j
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test-functional/test-cucumber/resources/features/owner", glue = "petclinic.api.cucumber.step",
    plugin = {"io.qameta.allure.cucumber5jvm.AllureCucumber5Jvm", "pretty", "summary"})
public class CucumberTest {

    private static String apiUrl;

    @BeforeClass
    public static void getApiUrl() {
        apiUrl = System.getProperty("apiUrl");
    }

    @AfterClass
    public static void cleanTestData() {
        log.info("Cleanup - Deleting owners created during test execution");
        OwnerApiClient client = new OwnerApiClient(apiUrl);
        TestContext.ownerIdList.forEach(client::deleteOwner);
    }
}
