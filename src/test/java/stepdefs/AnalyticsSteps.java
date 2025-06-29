package stepdefs;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DevToolsInterceptor;
import utils.TimeTracker;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class AnalyticsSteps {
    private ChromeDriver driver;
    private DevTools devTools;
    private DevToolsInterceptor interceptor;

    // AtomicLong for thread-safe last API capture timestamp
    private final AtomicLong lastApiReceivedAt = new AtomicLong(0);
    private static final int DEBOUNCE_TIMEOUT_MS = 2000;

    private TimeTracker timeTracker;

    @Before
    public void setup() {
        driver = new ChromeDriver();
        devTools = driver.getDevTools();
        devTools.createSession();
        interceptor = new DevToolsInterceptor(devTools);

    }

    @After
    public void teardown() {
        //if (driver != null) driver.quit();
    }


    @Given("I launch the UBS contact form page")
    public void openPage() throws InterruptedException {


        driver.get("https://www.ubs.com/global/en/wealthmanagement/connect-with-us.html");

        Thread.sleep(1000);
        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");

        long pageLoad = TimeTracker.getPageLoadTime(driver);
        System.out.println("Page Load Time (ms): " + pageLoad);

        long formLoad = TimeTracker.getDomInjectionTime(driver, "//form[@class=\"form__action secureform secureform__base\"]");  // Replace with actual form selector
        System.out.println("Form DOM Load Time (ms): " + formLoad);

    }

    @And("Accept the Cookies")
    public void acceptCookies() throws IOException, InterruptedException {

        driver.findElement(By.xpath("//button[3]")).click();
        waitForApiBurstCompletion();
        driver.get("https://www.ubs.com/global/en/wealthmanagement/connect-with-us.html");

        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");


    }

    @And("select sfCollection")
    public void sfCollection() throws InterruptedException, IOException {
        WebElement radio = driver.findElement(By.xpath("//*[@id=\"title_sfcollection\"]/li[1]/label"));
        radio.click();
        waitForApiBurstCompletion();

        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");

    }

    @And("Enter FirstName as {string}")
    public void firstName(String FName) throws IOException, InterruptedException {
        WebElement firstName = driver.findElement(By.id("firstName"));
        firstName.sendKeys(FName);
        waitForApiBurstCompletion();

        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");
        Thread.sleep(1000);

    }

    @And("Enter LastName")
    public void lastName() throws IOException, InterruptedException {
        WebElement lastName = driver.findElement(By.id("lastName"));
        lastName.sendKeys("test");
        waitForApiBurstCompletion();

        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");

    }

    @And("Enter Telephone")
    public void enterTelephone() throws InterruptedException, IOException {
        WebElement telephone = driver.findElement(By.xpath("//*[@id=\"phoneNumber_number\"]"));
        telephone.sendKeys("79000897657");
        Thread.sleep(1000);
        waitForApiBurstCompletion();
        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");
    }

    @And("Enter email")
    public void enterEmail() throws InterruptedException, IOException {
        WebElement telephone = driver.findElement(By.xpath("//*[@id=\"email\"]"));
        telephone.sendKeys("test@test.com");
        waitForApiBurstCompletion();
        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");
    }

    @And("Select region")
    public void selectRegion() throws InterruptedException {
        WebElement dp = driver.findElement(By.id("domicile_sfcollection"));

        Select drp = new Select(dp);
        drp.selectByIndex(10);
       // drp.selectByValue("Aruba");
        Thread.sleep(1000);
        waitForApiBurstCompletion();
        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");

    }

    @And("Select phonePrefix")
    public void phonePrefix() throws InterruptedException {
        WebElement dp1 = driver.findElement(By.id("phoneNumber_prefix"));

        Select drp1 = new Select(dp1);
        drp1.selectByIndex(1);
        Thread.sleep(1000);
        waitForApiBurstCompletion();
        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");
        Thread.sleep(1000);

    }

    @And("Select consent")
    public void consent() throws InterruptedException {
        WebElement con = driver.findElement(By.xpath("//input[@class=\"newcheckbox__input\"][1]"));
        con.click();
        Thread.sleep(1000);
        waitForApiBurstCompletion();
        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");
        Thread.sleep(1000);


    }

    @And("Click on Submit Button")
    public void submitButton() throws InterruptedException {
        WebElement con = driver.findElement(By.xpath("//div[10]/button"));
        con.click();
        Thread.sleep(1000);
        waitForApiBurstCompletion();
        interceptor.exportCapturedPayloadsToJson("target/pageLoad_apis.json");
        Thread.sleep(1000);

    }



    private void waitForApiBurstCompletion() throws InterruptedException {
        long now;
        do {
            Thread.sleep(500);
            now = System.currentTimeMillis();
        } while (now - interceptor.getLastApiReceivedAt() < DEBOUNCE_TIMEOUT_MS);
    }

    @Then("I should see expected Adobe Analytics events {string}")
    public void validateAnalytics(String expectedValue) {
        interceptor.validateAdobeEvents(expectedValue);
    }

    @Then("generateReport")
    public void generateReport()
    {
        DevToolsInterceptor.generateChart( "target/api_time_chart.png");

    }


//    @Then("I should see expected Adobe Analytics events")
//    public void validateAnalytics() {
//        interceptor.validateAdobeEvents(new int[]{193, 13, 133, 15, 14, 134});
//    }
//
//    @And("I should log all triggered APIs to a JSON file")
//    public void logAPIs() {
//        interceptor.exportToJson("target/api_logs.json");

}
