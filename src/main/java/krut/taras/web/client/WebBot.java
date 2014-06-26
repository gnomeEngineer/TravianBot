package krut.taras.web.client;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;

public class WebBot {

	public static final String PROPERTIES_FILE = "conf/application.conf";
	private static final int DEFAULT_TIMEOUT = 12;
	//HtmlUnitDriver - headless browser
	private static WebDriver webdriver = new HtmlUnitDriver();
	//You can set also FireFox driver

	public static void main(String[] args) {
		Properties properties = loadProperties();
		url = properties.getProperty("target.url");
		user = properties.getProperty("target.user");
		password = properties.getProperty("target.pass");
		village = properties.getProperty("target.village");
		repeatTime = Integer.parseInt(properties.getProperty("target.repeatTime")) * 60000;
		maxRandomTime = Integer.parseInt(properties.getProperty("target.maxRandomTime")) * 60000;
		firstDelayTime = Integer.parseInt(properties.getProperty("target.firstDelayTime")) * 60000;
		timer = new Timer();
		timer.schedule(new WebBot().new Task(), firstDelayTime);

		webdriver.get(url);
		waitForElement(By.name("name")).sendKeys(user);
		findElement(By.name("password")).sendKeys(password);
		findElement(By.className("button-content")).click();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				webdriver.close();
				timer.cancel();
			}
		});
	}

	private static void startFarm() {
		LogManager.getLogManager().reset();

		waitForElement(By.xpath("//a[@href='dorf2.php']")).click();

		if (isEmpty(url) || isEmpty(user) || isEmpty(password)) {
			System.out.print("Application is not configured properly");
			return;
		}

		waitForElement(By.className("playerName"));
		String region = findElement(By.id("sidebarBoxVillagelist")).findElement(By.className("active")).findElement(By.className("name")).getText();
		System.out.println("Current regoin: " + region);
		if (!region.equals(village)) {
			findElement(By.xpath("//a[contains(.,'" + village + "')]")).click();
			region = findElement(By.id("sidebarBoxVillagelist")).findElement(By.className("active")).findElement(By.className("name")).getText();
			System.out.println("Regoin changed to: " + region);
		}

		findElement(By.className("villageBuildings")).findElement(By.tagName("a")).click();

		waitForElement(By.xpath("//area[@href='build.php?id=39']")).click();

		waitForElement(By.xpath("//a[@href='build.php?tt=99&id=39']")).click();
		try {
			waitForElement(By.xpath("//table[@class='list']"));
		} catch (Exception ex) {
			System.out.println("Farm list was not found");
			return;
		}
		List<WebElement> checkboxes = findElements(By.xpath("//table[@class='list']//td[1]/input[@type='checkbox']"));

		for (WebElement checkbox : checkboxes) {
			if (!checkbox.isSelected()) {
				checkbox.click();
			}
		}

		WebElement submit = findElement(By.xpath("//button[starts-with(@id, 'button')][@type='submit']"));
		submit.click();
		String result = waitForElement(By.xpath("//div[starts-with(@class,'listContent')]/p")).getText();

		System.out.println("Result: " + result);

		waitForElement(By.xpath("//a[@href='dorf" + (new Random().nextInt(1) + 1) + ".php']")).click();
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

	class Task extends TimerTask {

		@Override
		public void run() {
			System.out.println("---------------------------------");
			System.out.println(Calendar.getInstance().getTime());
			startFarm();
			double randomRange = new Random().nextDouble() * maxRandomTime;
			System.out.println((int) ((double) (repeatTime + randomRange) / 60000) + " min to next raide");
			timer.schedule(new Task(), repeatTime + (int) randomRange);
		}
	}

	private static String url;
	private static String user;
	private static String password;
	private static String village;

	private static int repeatTime;
	private static int maxRandomTime;
	private static int firstDelayTime;

	private static Timer timer;
}
