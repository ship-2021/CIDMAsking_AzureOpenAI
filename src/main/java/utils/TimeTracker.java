package utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TimeTracker {
    public static long getPageLoadTime(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long navigationStart = (Long) js.executeScript("return window.performance.timing.navigationStart;");
        long loadEventEnd = (Long) js.executeScript("return window.performance.timing.loadEventEnd;");
        return loadEventEnd - navigationStart;
    }

    public static long getDomInjectionTime(WebDriver driver, String xpath) {
        long start = System.currentTimeMillis();
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(org.openqa.selenium.By.xpath(xpath)));
        return System.currentTimeMillis() - start;
    }
}