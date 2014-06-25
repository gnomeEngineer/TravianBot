package krut.taras.web.client;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class WebBot {


    public static final String PROPERTIES_FILE = "conf/application.conf";
    private static final int DEFAULT_TIMEOUT = 12;
    //HtmlUnitDriver - headless browser
    private static  WebDriver webdriver = new HtmlUnitDriver();
    //You can set also FireFox driver

    public static void main(String[] args) {
        LogManager.getLogManager().reset();

        Properties properties = loadProperties();
        String url = properties.getProperty("target.url");
        String user = properties.getProperty("target.user");
        String password = properties.getProperty("target.pass");

        if(isEmpty(url) || isEmpty(user) || isEmpty(password)) {
            System.out.print("Application is not configured properly");
            webdriver.close();
            return;
        }
        webdriver.get(url);
        waitForElement(By.name("name")).sendKeys(user);
        findElement(By.name("password")).sendKeys(password);
        findElement(By.className("button-content")).click();

        waitForElement(By.className("playerName"));
        String region = findElement(By.id("sidebarBoxVillagelist"))
                .findElement(By.className("active"))
                .findElement(By.className("name"))
                .getText();
        System.out.println("Current regoin: " + region);
        findElement(By.className("villageBuildings")).findElement(By.tagName("a")).click();

        waitForElement(By.xpath("//area[@href='build.php?id=39']")).click();

        waitForElement(By.xpath("//a[@href='build.php?tt=99&id=39']")).click();
        try {
            waitForElement(By.xpath("//table[@class='list']"));
        }catch (Exception ex) {
            System.out.println("Farm list was not found");
            return;
        }
        List<WebElement> checkboxes = findElements(By.xpath("//table[@class='list']//td[1]/input[@type='checkbox']"));

        for(WebElement checkbox : checkboxes) {
            if ( !checkbox.isSelected() )
            {
                checkbox.click();
            }
        }

        WebElement submit = findElement(By.xpath("//button[starts-with(@id, 'button')][@type='submit']"));
        submit.click();
        String result = waitForElement(By.xpath("//div[starts-with(@class,'listContent')]/p")).getText();

        System.out.println("Result: " + result);

        webdriver.quit();
    }


    private static WebElement findElement(By matcher) {
        return webdriver.findElement(matcher);
    }

    private static List<WebElement> findElements(By matcher) {
        return webdriver.findElements(matcher);
    }

    private static WebElement waitForElement(By condition) {
        return waitForElement(condition, DEFAULT_TIMEOUT);
    }

    private static WebElement waitForElement(final By condition, int secondsToWait) {

        WebElement webElement = null;
        try {
            webElement = new WebDriverWait(webdriver, secondsToWait).until(ExpectedConditions.presenceOfElementLocated(condition));
        } catch (org.openqa.selenium.TimeoutException e) {
            return findElement(condition);
        }
        return webElement;
    }

    private static Properties loadProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(PROPERTIES_FILE);
            prop.load(input);

        } catch (IOException ex) {
            System.out.print("CAN NOT READ PROPRIETIES FILE");
            ex.printStackTrace();

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }
}
