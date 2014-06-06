package krut.taras.web.client;

import com.google.common.base.Optional;
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

public class WebBot {


    public static final String PROPERTIES_FILE = "conf/application.conf";
    private static final int DEFAULT_TIMEOUT = 12;

    //HtmlUnitDriver - headless browser
    public static  WebDriver webdriver = new HtmlUnitDriver();
    //You can set also FireFox driver

    public static void main() {

        Properties properties = loadProperties();
        Optional<String> url = Optional.of(properties.getProperty("target.url"));
        Optional<String> user = Optional.of(properties.getProperty("target.user"));
        Optional<String> password = Optional.of(properties.getProperty("target.pass"));

        if(!url.isPresent() || !user.isPresent() || !password.isPresent()) {
            System.out.print("application is not configured");
            return;
        }
        webdriver.get(url.get());
        waitForElement(By.name("name")).sendKeys(user.get());
        webdriver.findElement(By.name("password")).sendKeys(password.get());
        webdriver.findElement(By.className("button-content")).click();
        waitForElement(By.className("playerName"));
        webdriver.findElement(By.className("villageBuildings")).findElement(By.tagName("a")).click();
        waitForElement(By.xpath("//area[@href='build.php?id=39']")).click();
        waitForElement(By.xpath("//a[@href='build.php?tt=99&id=39']")).click();
        WebElement table = waitForElement(By.xpath("//table[@class='list']"));
        List<WebElement> checkboxes = webdriver.findElements(By.xpath("//table[@class='list']//td[1]/input[@type='checkbox']"));

        for(WebElement checkbox : checkboxes) {
            if ( !checkbox.isSelected() )
            {
                checkbox.click();
            }
        }

        WebElement submit = webdriver.findElement(By.xpath("//button[starts-with(@id, 'button')][@type='submit']"));
        submit.click();
        String result = waitForElement(By.xpath("//div[starts-with(@class,'listContent')]/p")).getText();

        System.out.print("Result: " + result);

        webdriver.quit();
    }

    private static WebElement waitForElement(By condition) {
        return waitForElement(condition, DEFAULT_TIMEOUT);
    }

    private static WebElement waitForElement(final By condition, int secondsToWait) {

        WebElement webElement = null;
        try {
            webElement = new WebDriverWait(webdriver, secondsToWait).until(ExpectedConditions.presenceOfElementLocated(condition));
        } catch (org.openqa.selenium.TimeoutException e) {
            return webdriver.findElement(condition);
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
            System.out.print("Cant read file");
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
