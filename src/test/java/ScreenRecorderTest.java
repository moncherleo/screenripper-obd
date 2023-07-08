import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.FindFailed;
import utils.*;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class ScreenRecorderTest {
    public static void main(String[] args) {

        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Create ChromeOptions and set custom User-Agent, disable specific properties, and enable headless mode
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");

        System.setProperty("webdriver.chrome.verboseLogging", "true");

        // Create a new instance of ChromeDriver with options and maximize the window
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        VisualTestHelper visualTestHelper = null;

        //OBScontroller obsController = new OBScontroller();

        String cookieJSONpath = "src/test/resources/cookies.json";
        String coursePageURLPath = "src/test/resources/page.txt";
        String videoFolderPath = "//VBOXSVR/_recordings/Selenium_WebDriver_with_Java_-Basics_to_Advanced+Frameworks";

        // Specify the output directory path
        String outputDirectoryPath = "src/test/resources/output/";

        // Create a list to hold the lecture data
        List<Map<String, String>> lectureDataList = new ArrayList<>();

        String currentURL = "";

        // Open a website
        System.out.println("Opening a website");
        String websiteURL = FileHelper.extractBaseURL(FileHelper.readFirstLineFromFile(coursePageURLPath));
        driver.get(websiteURL);
        System.out.println("Opened a website: " + websiteURL);

        // Add cookies to browser
        Set<Cookie> cookies = CookieConverter.convertJsonToCookies(cookieJSONpath);
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
        System.out.println("Cookies are added to the browser.");

        List<LecturePOJO> lectures = JsonReader.readLecturesFromFile("src/test/resources/output/Selenium_WebDriver_with_Java_-Basics_to_Advanced+Frameworks.json");

        for (int j = 0; j < lectures.size(); j++) {
            System.out.println("Lecture Name: " + lectures.get(j).getLectureName());
            System.out.println("Current URL: " + lectures.get(j).getCurrentURL());

            driver.get(lectures.get(j).getCurrentURL());
            System.out.println("Open lecture URL: " + lectures.get(j).getCurrentURL());

            // Polling the page to get at least one video element on the page
            List<WebElement> videoElements = null;
            while (videoElements == null || videoElements.size() < 1) {
                videoElements = driver.findElements(By.xpath("//video"));
            }
            System.out.println("Video elements are found. Qty: " + videoElements.size());

//            // Move the mouse over the video element
//            Actions actions = new Actions(driver);
//            actions.moveToElement(videoElements.get(0)).perform();
//            System.out.println("Moved mouse over video element");
//
//            // Move the mouse 10 pixels left and right repeatedly for 5 times
//            int moveCount = 5;
//            int xOffsetPix = 10;
//            int delayMs = 500;
//
//            for (int i = 0; i < moveCount; i++) {
//                // Move left
//                actions.moveByOffset(-xOffsetPix, 0).perform();
//                System.out.println("Moved left");
//
//                // Wait for a short duration
//                try {
//                    Thread.sleep(delayMs);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                // Move right
//                actions.moveByOffset(xOffsetPix, 0).perform();
//                System.out.println("Moved right");
//
//                // Wait for a short duration
//                try {
//                    Thread.sleep(delayMs);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            // Pause the video if it is playing now
            List<WebElement> pauseVideoButtons = driver.findElements(By.xpath("//button[@data-purpose='pause-button']"));
            if (pauseVideoButtons.size() > 0) {
                System.out.println("Video is playing");
                pauseVideoButtons.get(0).click();
                System.out.println("Pausing the video");
            }

            // Disable video autoplay
            WebElement settingsButton = driver.findElement(By.xpath("//button[@data-purpose='settings-button']"));
            settingsButton.click();
            WebElement autoplayToggle = driver.findElement(By.xpath("//ul[@data-purpose='settings-menu']/li[3]//button"));
            boolean isAutoplay = Boolean.parseBoolean(autoplayToggle.getAttribute("aria-checked"));
            if (isAutoplay) {
                autoplayToggle.click();
            }
            settingsButton.click();

//            // Find the progress bar element
//            WebElement progressBar = driver.findElement(By.xpath("//div[@data-purpose='video-progress-bar']"));
//
//            // Wait for the progress bar to be clickable
//            wait.until(ExpectedConditions.elementToBeClickable(progressBar));
//
//            // Calculate the offset by subtracting 1 pixel from the left boundary
//            int offset = 5;
//
//            // Move the mouse to the calculated offset position within the progress bar element and perform the click
//            actions.moveToElement(progressBar, offset, 0).click().build().perform();
//            System.out.println("Clicked on the progress bar at offset " + offset + " pixel");
//
//            driver.findElement(By.xpath("//video")).click();
//            System.out.println("Click on //video");

            // Wait until real video duration appears
            String videoDurationText = "";
            while (videoDurationText.equals("") || videoDurationText.equals("0:00")) {
                // Get video duration
                videoDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
                System.out.println("Video duration is " + videoDurationText);
            }

            // Get current video time
            String currentVideoTimeText = driver.findElement(By.xpath("//span[@data-purpose='current-time']")).getText();
            System.out.println("Current video time is " + currentVideoTimeText);

            // Get current video speed
            WebElement currentPlaybackRateSpeed = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']/span"));
            String currentPlaybackSpeedText = currentPlaybackRateSpeed.getText();
            System.out.println("Current video speed is " + currentPlaybackSpeedText);

            // If current video time is more than 5 seconds
            if (TimeConverter.convertToMilliseconds(currentVideoTimeText) > 5 * 1000) {
                // Set the video to 2x
                System.out.println("Speeding video to 2x");
                WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                playbackRateMenuButton.click();
                WebElement playbackRateMenuItem2x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='2x']"));
                playbackRateMenuItem2x.click();
                currentPlaybackRateSpeed = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']/span"));
                currentPlaybackSpeedText = currentPlaybackRateSpeed.getText();
                System.out.println("Current video speed is " + currentPlaybackSpeedText);

                // Calculate residual duration by the speed of 2x, adding 5s just in case
                int remainingVideoTime = TimeConverter.convertToMilliseconds(videoDurationText) - TimeConverter.convertToMilliseconds(currentVideoTimeText);
                int requiredDelay = (int) (remainingVideoTime / 2) + 5 * 1000;

                // Play the video if it is not playing
                List<WebElement> playVideoButtons = driver.findElements(By.xpath("//button[@data-purpose='play-button']"));
                if (playVideoButtons.size() > 0) {
                    System.out.println("Video is paused");
                    playVideoButtons.get(0).click();
                    System.out.println("Start playing video");
                }

                try {
                    Thread.sleep(3*1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                long startTime = System.currentTimeMillis();
                long endTime = startTime + requiredDelay;

                Robot robot = null;
                // Create an instance of the Robot class
                try {
                    robot = new Robot();
                } catch (AWTException e) {
                    throw new RuntimeException(e);
                }

                // visualTestHelper = new VisualTestHelper();
                while (System.currentTimeMillis() < endTime) {
                    // Perform your desired actions here
                    // This loop will repeat until the current time reaches the target end time
                    // Move mouse to absolute position
                    for (int i = 0; i < 5; i++) {
                        //visualTestHelper.moveMouseToLocation(20 + i, 200);
                        robot.mouseMove(20 + i, 200);
                        if (i == 4) {
                            i = 0;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

//                try {
//                    Thread.sleep(requiredDelay);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }

//                // Polling the video timings until it will play to the end, 1 second refresh rate
//                while (!currentPlaybackSpeedText.equals(videoDurationText)) {
////                    try {
////                        Thread.sleep(1000);
////                    } catch (InterruptedException e) {
////                        throw new RuntimeException(e);
////                    }
//
//                    // Create an instance of Random
//                    Random random = new Random();
//
//                    // Generate a random integer between 2 and 6
//                    int randomNumber = random.nextInt(5) + 2;
//
//                    int xOffset = randomNumber;
//                    int yOffset = 200;
//
//                    // Create an instance of the Robot class
//                    Robot robot = null;
//                    try {
//                        robot = new Robot();
//                    } catch (AWTException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    // Move the cursor to specific coordinates (e.g., x=100, y=200)
//                    robot.mouseMove(xOffset, yOffset);
//
//                    // Find the next video cancel link and click on it
//                    List<WebElement> cancelAutoplay = driver.findElements(By.xpath("//span[text()='Cancel']"));
//                    if (cancelAutoplay.size() > 0) {
//                        cancelAutoplay.get(0).click();
//                    }
//
//                    // Get current time
//                    currentVideoTimeText = driver.findElement(By.xpath("//span[@data-purpose='current-time']")).getText();
//                    System.out.println("Current video time is " + currentVideoTimeText);
//
//                    // Get duration
//                    videoDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
//                    System.out.println("Video duration is " + videoDurationText);
//                }

                // Go to google.com to reset the video settings
                driver.get("https://google.com");

                // Go to the page with video
                driver.get(lectures.get(j).getCurrentURL());

            }

            // Move mouse to absolute position
            visualTestHelper = new VisualTestHelper();
            for (int i = 0; i < 5; i++) {
                visualTestHelper.moveMouseToLocation(20 + i, 200);
            }


            // Wait until real video duration appears
            videoDurationText = "";
            while (videoDurationText.equals("") || videoDurationText.equals("0:00")) {
                // Get video duration
                videoDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
                System.out.println("Video duration is " + videoDurationText);
            }

            // Pause the video if it is playing now
            pauseVideoButtons = driver.findElements(By.xpath("//button[@data-purpose='pause-button']"));
            if (pauseVideoButtons.size() > 0) {
                System.out.println("Video is playing");
                pauseVideoButtons.get(0).click();
                System.out.println("Pausing the video");
            }

            // Verify that there is less than 5 seconds from the start of the video
            currentVideoTimeText = driver.findElement(By.xpath("//span[@data-purpose='current-time']")).getText();
            System.out.println("Current video time is " + currentVideoTimeText);
            if (TimeConverter.convertToMilliseconds(currentVideoTimeText) < 5 * 1000) {
                System.out.println("Current video time is less than 5 seconds");
            }

            if (!currentPlaybackSpeedText.equals("1.5x")) {
                // Speed up the video to 1.5x
                System.out.println("Speeding video to 1.5x");
                WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                playbackRateMenuButton.click();
                WebElement playbackRateMenuItem15x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='1.5x']"));
                playbackRateMenuItem15x.click();
                currentPlaybackRateSpeed = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']/span"));
                currentPlaybackSpeedText = currentPlaybackRateSpeed.getText();
                System.out.println("Current video speed is " + currentPlaybackSpeedText);
            }

            // Enable English close captions
            WebElement closeCaptionsMenuButton = driver.findElement(By.xpath("//button[@data-purpose='captions-dropdown-button']"));
            closeCaptionsMenuButton.click();
            WebElement englishCloseCaptionsMenuItem = driver.findElement(By.xpath("//ul[@data-purpose='captions-dropdown-menu']//div[contains(text(),'English')]"));
            englishCloseCaptionsMenuItem.click();
            closeCaptionsMenuButton.click();

            // Play the video if it did not play before
            List<WebElement> playVideoButtons = driver.findElements(By.xpath("//button[@data-purpose='play-button']"));
            if (playVideoButtons.size() > 0) {
                System.out.println("Video is paused");
                playVideoButtons.get(0).click();
                System.out.println("Start playing video");
            }

            int playbackDurationAdjustedBySpeed = 0;

            // Calculate video time
            // Get current time
            currentVideoTimeText = driver.findElement(By.xpath("//span[@data-purpose='current-time']")).getText();
            System.out.println("Current video time is " + currentVideoTimeText);

            // Get duration
            videoDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
            System.out.println("Video duration is " + videoDurationText);

            // Calculate real playback duration adjusted by speed multiplier
            int playbackDuration = TimeConverter.convertToMilliseconds(videoDurationText) - TimeConverter.convertToMilliseconds(currentVideoTimeText);
            playbackDurationAdjustedBySpeed = (int) (playbackDuration / 1.5);

            // Minimize browser window to let SikuliX manipulate OBS
            driver.manage().window().minimize();
            //Create SikuliX helper
            visualTestHelper = new VisualTestHelper();
            // Make sure that recording is started
            try {
                visualTestHelper.startRecording();
                visualTestHelper.isRecordingStarted();
            } catch (FindFailed e) {
                throw new RuntimeException(e);
            }

            // Maximize browser window
            driver.manage().window().maximize();

            // Set video full-screen
            WebElement fullScreenButton = driver.findElement(By.xpath("//div[@data-purpose='video-controls']/div[12]/button"));
            fullScreenButton.click();
            System.out.println("Entered full-screen mode");

            try {
                new VisualTestHelper().closeSeleniumBrowserNotification();
            } catch (FindFailed e) {
                throw new RuntimeException(e);
            }

            // Start video recording
            // obsController.startRecording();

//            // Wait till the video will play till the end
//            try {
//                Thread.sleep(playbackDurationAdjustedBySpeed);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            try {
                Thread.sleep(3*1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            long startTime = System.currentTimeMillis();
            long endTime = startTime + playbackDurationAdjustedBySpeed;

            Robot robot = null;
            // Create an instance of the Robot class
            try {
                robot = new Robot();
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }

            long now = System.currentTimeMillis();;
            int xIncrement = 0;

            // visualTestHelper = new VisualTestHelper();
            while (System.currentTimeMillis() < endTime) {
                // Perform your desired actions here
                // This loop will repeat until the current time reaches the target end time
                // Move mouse to absolute position
                if (System.currentTimeMillis() > now+5*60*1000){
                    now = System.currentTimeMillis();
                    robot.mouseMove(20 + xIncrement, 200);
                    xIncrement++;
                    if (xIncrement > 10) {
                        xIncrement = 0;
                    }
                }
//                for (int i = 0; i < 5; i++) {
//                    //visualTestHelper.moveMouseToLocation(20 + i, 200);
//                    robot.mouseMove(20 + i, 200);
//                    if (i == 4) {
//                        i = 0;
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
            }

            // Perform action to exit full-screen mode using the JS executor
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("document.exitFullscreen();");
            System.out.println("Exiting full-screen mode with JavaScript");

            // Minimize browser window to let SikuliX manipulate OBS
            driver.manage().window().minimize();
            //Create SikuliX helper
            visualTestHelper = new VisualTestHelper();
            // Make sure that recording is started
            try {
                visualTestHelper.stopRecording();
                visualTestHelper.isRecordingStopped();
            } catch (FindFailed e) {
                throw new RuntimeException(e);
            }

            // Stop video recording with custom filename
            // obsController.stopRecording(FileHelper.normalizeFileName(lectures.get(j).getLectureName()));

//            // Perform action to exit full-screen mode using the ESC key
//            Actions actions = new Actions(driver);
//            actions.sendKeys(Keys.ESCAPE).perform();
//            actions.sendKeys(Keys.ESCAPE).perform();
//            System.out.println("Exiting full-screen mode with ESC key");

            // Wait until file save is completed
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            FileHelper.renameMostRecentFile(videoFolderPath, FileHelper.normalizeFileName(lectures.get(j).getLectureName()));

            driver.manage().window().maximize();

            // TODO remove this code
//            System.out.println("Thread sleep... ");
//            try {
//                Thread.sleep(1000 * 1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }

        // End of the video list
        // Close the browser
        driver.quit();
    }
}