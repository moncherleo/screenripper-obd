import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.FindFailed;
import utils.*;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ScreenRecorder {
    public static void main(String[] args) {

        ProjectProperties projectProperties = new ProjectProperties();
        VisualTestHelper visualTestHelper;
        Robot robot;

        // Create an instance of the Robot class
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        // Reading properties
        System.out.println("Loading project properties");
        String cookieJSONFilePath = projectProperties.getCookieJSONFilePath();
        String videoFolderPath = projectProperties.getVideoFolderPath();
        String outputDirectoryPath = "src/test/resources/output/";
        List<LecturePOJO> lectures = JsonReader.readLecturesFromFile(projectProperties.getContentJSONFilePath());
        System.out.println("Properties are loaded");

        System.out.println("Setting up Selenium Webdriver with Chrome");
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Create ChromeOptions and set custom User-Agent
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        System.setProperty("webdriver.chrome.verboseLogging", "true");

        // Create a new instance of ChromeDriver with options and maximize the window
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("Selenium Webdriver with Chrome is launched");

        // Open a website
        String websiteURL = FileHelper.extractBaseURL(lectures.get(0).getCurrentURL());
        System.out.println("Opening a website: " + websiteURL);
        driver.get(websiteURL);
        System.out.println("Website is opened");

        // Close Selenium top bar
        try {
            new VisualTestHelper().closeSeleniumBrowserNotification();
        } catch (FindFailed e) {
            throw new RuntimeException(e);
        }

        // Add cookies to browser
        Set<Cookie> cookies = CookieConverter.convertJsonToCookies(cookieJSONFilePath);
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
        System.out.println("Cookies are added to the Chrome browser");

        // Starting content processing in the big cycle
        System.out.println("Starting content processing...");
        for (int j = 0; j < lectures.size(); j++) {
            System.out.println("Current content name: " + lectures.get(j).getLectureName());
            System.out.println("Current content URL: " + lectures.get(j).getCurrentURL());

            driver.get(lectures.get(j).getCurrentURL());
            System.out.println("Opening the content page...");

            // Polling the page to get at least one content element on the page
            System.out.println("Polling the page to get at least one content element on the page");
//            List<WebElement> videoElements = null;
//            while (videoElements == null || videoElements.size() < 1) {
//                videoElements = driver.findElements(By.xpath("//video"));
//            }
//            System.out.println(videoElements.size() + " content elements found");
            long startTime = System.currentTimeMillis();
            long durationInMillis = 60 * 1000; // 60 seconds

            while ((System.currentTimeMillis() - startTime) < durationInMillis) {
                List<WebElement> videoElements = driver.findElements(By.xpath("//video"));

                if (videoElements.size() >= 1) {
                    System.out.println(videoElements.size() + " content elements found");
                    break; // Exit the while loop if at least one content element is found
                }

                // Check if the elapsed time exceeds 60 seconds
                if ((System.currentTimeMillis() - startTime) >= durationInMillis) {
                    System.out.println("No content elements found. Refreshing the page.");
                    driver.navigate().refresh();
                    startTime = System.currentTimeMillis(); // Reset the start time after refreshing the page
                }
            }

            // Pause the content if it is playing now
            ScreenRecorder.pauseContentPlayback(driver);

            // Disable content autoplay
            System.out.println("Disabling content autoplay");
            System.out.println("Opening Settings menu");
            WebElement settingsButton = driver.findElement(By.xpath("//button[@data-purpose='settings-button']"));
            settingsButton.click();
            System.out.println("Settings menu opened");
            WebElement autoplayToggle = driver.findElement(By.xpath("//ul[@data-purpose='settings-menu']/li[3]//button"));
            System.out.println("Checking autoplay status...");
            boolean isAutoplay = Boolean.parseBoolean(autoplayToggle.getAttribute("aria-checked"));
            if (isAutoplay) {
                System.out.println("Content autoplay is enabled. Disabling...");
                autoplayToggle.click();
            }
            settingsButton.click();
            System.out.println("Content autoplay is disabled");

            // Wait until real video duration appears
            String contentDurationText = ScreenRecorder.pollingContentDuration(driver);

            // Play content if it is paused now
            ScreenRecorder.playContent(driver);

            // Get current content speed
            String currentPlaybackSpeedText = ScreenRecorder.getCurrentPlaybackRateSpeed(driver);

            // Get current content time
            String currentContentTimeText = ScreenRecorder.getCurrentContentTimeText(driver);

            // If current content time is more than 10 seconds
            while (TimeConverter.convertToMilliseconds(currentContentTimeText) > 10 * 1000) {
                // Set the video to 2x
                System.out.println("***** WARNING *****\nWe are are quite far on content playback timeline.");
                System.out.println("Speeding content playback speed to 2x to play till the end");

                // Wait until real video duration appears
                contentDurationText = ScreenRecorder.pollingContentDuration(driver);

                // Play content if it is paused now
                ScreenRecorder.playContent(driver);

                // Get current content speed
                currentPlaybackSpeedText = ScreenRecorder.getCurrentPlaybackRateSpeed(driver);

                // Get current content time
                currentContentTimeText = ScreenRecorder.getCurrentContentTimeText(driver);

                if (!currentPlaybackSpeedText.equals("2x")) {
                    WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                    System.out.println("Opening playback menu");
                    playbackRateMenuButton.click();
                    WebElement playbackRateMenuItem2x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='2x']"));
                    System.out.println("Accelerating content playback speed to 2x");
                    playbackRateMenuItem2x.click();
                }

                currentPlaybackSpeedText = ScreenRecorder.getCurrentPlaybackRateSpeed(driver);

                // Calculate residual duration by the speed of 2x, adding 5s just in case
                int timeReserveMs = 10 * 1000;
                int contentDurationMs = TimeConverter.convertToMilliseconds(contentDurationText);
                int currentContentTimeMs = TimeConverter.convertToMilliseconds(currentContentTimeText);
                int remainingVideoTime = contentDurationMs - currentContentTimeMs;
                int remainingVideoTimeAdjusted = remainingVideoTime / 2;
                int requiredDelayMs = remainingVideoTimeAdjusted + timeReserveMs;

                System.out.println("Adjusted remaining content playback time is: " + String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(remainingVideoTime),
                        TimeUnit.MILLISECONDS.toSeconds(remainingVideoTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingVideoTime))));

                // Play the content if it is not playing yet
                ScreenRecorder.playContent(driver);

                // Checking whether the content is playing or not
                ScreenRecorder.isContentPlayback(driver);

                // Setting the timer values
                long startTimeMs = System.currentTimeMillis();
                long endTimeMs = startTimeMs + requiredDelayMs;

                String formattedStartDateTime = ScreenRecorder.millisecondsToDateString(startTimeMs);
                System.out.println("Playback start date and time is: " + formattedStartDateTime);

                String formattedEndDateTime = ScreenRecorder.millisecondsToDateString(startTimeMs);
                System.out.println("Expected end date and time is: " + formattedEndDateTime);

                int pixelIncrement = 0;
                int xPos = 200;
                int yPos = 200;

                // Trying to rewind to the end of the content playback with visual element navigation
                new VisualTestHelper().rewindContent();

                while (System.currentTimeMillis() < endTimeMs) {
                    // Find proceed to next video button in the end of the course and exit loop if found
                    List<WebElement> continueButtons = driver.findElements(By.xpath("//div[@data-purpose='go-to-next-button']"));
                    if (continueButtons.size() > 0) {
                        break;
                    }

                    String formattedDateTime = ScreenRecorder.millisecondsToDateString(startTimeMs);
                    System.out.println("Current date and time in cycle is: " + formattedDateTime);

                    // Perform your desired actions here
                    // Move mouse to absolute position
                    robot.mouseMove(xPos + pixelIncrement, yPos);
                    pixelIncrement++;
                    if (pixelIncrement > 20) {
                        pixelIncrement = 0;
                    }
                    System.out.println("Move the cursor to x: " + (xPos + pixelIncrement) + ", y: " + yPos);

                    // Wait for some time before repeating
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Go to google.com to reset the video settings
                driver.get("https://google.com");

                // Go to the page with video
                driver.get(lectures.get(j).getCurrentURL());

                // Wait 10 sec until content loaded
                try {
                    Thread.sleep(15 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }

            System.out.println("***** Proceeding with capturing of the content *****");


            // Move mouse to absolute position
            robot.mouseMove(198, 198);
            System.out.println("Moved the cursor to x: " + 198 + ", y: " + 198);

            // Wait until real video duration appears
            ScreenRecorder.pollingContentDuration(driver);

            // Move mouse to absolute position
            robot.mouseMove(197, 197);
            System.out.println("Moved the cursor to x: " + 197 + ", y: " + 197);

            // Pause the video if it is playing now
            ScreenRecorder.pauseContentPlayback(driver);

            // Verify that there is less than 5 seconds from the start of the video
            currentContentTimeText = ScreenRecorder.getCurrentContentTimeText(driver);
            if (TimeConverter.convertToMilliseconds(currentContentTimeText) < 10 * 1000) {
                System.out.println("Current content playback time is less than 10 seconds");
            }

            if (!currentPlaybackSpeedText.equals("1.5x")) {
                // Speed up the video to 1.5x
                System.out.println("Speeding video to 1.5x");
                WebElement playbackRateMenuButton = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']"));
                playbackRateMenuButton.click();
                WebElement playbackRateMenuItem15x = driver.findElement(By.xpath("//ul[@data-purpose='playback-rate-menu']//span[text()='1.5x']"));
                playbackRateMenuItem15x.click();
                currentPlaybackSpeedText = ScreenRecorder.getCurrentPlaybackRateSpeed(driver);
                System.out.println("Current video speed is " + currentPlaybackSpeedText);
            }

            // Enable English close captions
            List<WebElement> closeCaptionsMenuButtons = driver.findElements(By.xpath("//button[@data-purpose='captions-dropdown-button']"));
            if (closeCaptionsMenuButtons.size() > 0) {
                closeCaptionsMenuButtons.get(0).click();
                WebElement englishCloseCaptionsMenuItem = driver.findElement(By.xpath("//ul[@data-purpose='captions-dropdown-menu']//div[contains(text(),'English')]"));
                englishCloseCaptionsMenuItem.click();
                closeCaptionsMenuButtons.get(0).click();
            }

            // Play the video if it did not play before
            ScreenRecorder.playContent(driver);

            // Calculate video time
            // Get current time
            currentContentTimeText = ScreenRecorder.getCurrentContentTimeText(driver);

            // Get duration
            contentDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
            System.out.println("Video duration is " + contentDurationText);

            // Calculate real playback duration adjusted by speed multiplier
            int playbackDuration = TimeConverter.convertToMilliseconds(contentDurationText) - TimeConverter.convertToMilliseconds(currentContentTimeText);
            int playbackDurationAdjustedBySpeed = (int) (playbackDuration / 1.5);

            // Minimize browser window to let SikuliX manipulate OBS
            driver.manage().window().minimize();
            System.out.println("Browser window is minimized");

            System.out.println("Starting recording...");
            // Make sure that recording is started
            visualTestHelper = new VisualTestHelper();
            try {
                visualTestHelper.startRecording();
                visualTestHelper.isRecordingStarted();
            } catch (FindFailed e) {
                throw new RuntimeException(e);
            }
            System.out.println("Recording is started");

            // Maximize browser window
            driver.manage().window().maximize();
            System.out.println("Browser window maximized");

            // Set video full-screen
            System.out.println("Entering full-screen mode...");
            WebElement videoPlayer = driver.findElement(By.xpath("//video"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].requestFullscreen();", videoPlayer);

            try {
                int delay = 3 * 1000;
                playbackDurationAdjustedBySpeed = playbackDurationAdjustedBySpeed - delay;
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            VisualTestHelper visualTestHelper1 = new VisualTestHelper();
            if (visualTestHelper1.isFullScreenVisible()) {
                try {
                    visualTestHelper1.setFullScreen();
                    System.out.println("Full screen achieved with visual button click");
                } catch (FindFailed e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Full screen achieved with JS executor");
            }

            if ((boolean) ((JavascriptExecutor) driver).executeScript("return !!document.fullscreenElement")) {
                System.out.println("Content is not in fullscreen mode. Second attempt with JS");
                ((JavascriptExecutor) driver).executeScript("arguments[0].requestFullscreen();", videoPlayer);
            }

            try {
                int delay = 3 * 1000;
                playbackDurationAdjustedBySpeed = playbackDurationAdjustedBySpeed - delay;
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (visualTestHelper1.isFullScreenVisible()) {
                System.out.println("Full screen first attempt with visual button is failed!");
                try {
                    System.out.println("Second attempt of achieving the full screen with visual button click");
                    visualTestHelper1.setFullScreen();
                } catch (FindFailed e) {
                    throw new RuntimeException(e);
                }
            }

//            // Move mouse to absolute position
//            robot.mouseMove(199, 199);
//            robot.mouseMove(200, 200);
//            List<WebElement> fullScreenButtons = driver.findElements(By.xpath("//div[@data-purpose='video-controls']/div[12]/button"));
//            while (fullScreenButtons.size() < 1){
//                // Move mouse to absolute position
//                robot.mouseMove(199, 199);
//                robot.mouseMove(200, 200);
//                fullScreenButtons = driver.findElements(By.xpath("//div[@data-purpose='video-controls']/div[13]/button"));
//            }
//            // Move mouse to absolute position
//            robot.mouseMove(199, 199);
//            robot.mouseMove(200, 200);
//            fullScreenButtons.get(0).click();
            System.out.println("Entered full-screen mode");

            // Wait till the video will play till the end
            try {
                Thread.sleep(playbackDurationAdjustedBySpeed);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Perform action to exit full-screen mode using the JS executor
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("document.exitFullscreen();");
            System.out.println("Exiting full-screen mode with JavaScript");

            // Minimize browser window to let SikuliX manipulate OBS
            driver.manage().window().minimize();

            // Make sure that recording is started
            visualTestHelper = new VisualTestHelper();
            try {
                visualTestHelper.stopRecording();
                visualTestHelper.isRecordingStopped();
            } catch (FindFailed e) {
                throw new RuntimeException(e);
            }

            // Wait until file save is completed
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            FileHelper.renameMostRecentFile(videoFolderPath, FileHelper.normalizeFileName(lectures.get(j).getLectureName()));

            driver.manage().window().maximize();
        }

        // End of the content list
        // Close the browser
        driver.quit();
    }

    public static String pollingContentDuration(WebDriver driver) {
        // Wait until real video duration appears
        System.out.println("Polling webpage for content duration...");
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        String contentDurationText = "";
        while (contentDurationText.equals("") || contentDurationText.equals("0:00")) {
            // Get content duration
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            contentDurationText = driver.findElement(By.xpath("//span[@data-purpose='duration']")).getText();
            System.out.println("Polling... time is " + contentDurationText);
        }
        System.out.println("Content duration received. Duration: " + contentDurationText);

        return contentDurationText;
    }

    public static String getCurrentContentTimeText(WebDriver driver) {
        // Get current content time
        String currentContentTimeText = "";
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 10; i++) {
            robot.mouseMove(200, 200);
            robot.mouseMove(199, 199);
            currentContentTimeText = driver.findElement(By.xpath("//span[@data-purpose='current-time']")).getText();
        }

        System.out.println("Current content playback position time is: " + currentContentTimeText);
        return currentContentTimeText;
    }

    public static String getCurrentPlaybackRateSpeed(WebDriver driver) {
        // Get current content speed
        System.out.println("Getting content playback speed...");
        WebElement currentPlaybackRateSpeed = driver.findElement(By.xpath("//button[@data-purpose='playback-rate-button']/span"));
        String currentPlaybackSpeedText = currentPlaybackRateSpeed.getText();
        System.out.println("Current content speed is: " + currentPlaybackSpeedText);
        return currentPlaybackSpeedText;
    }

    public static void pauseContentPlayback(WebDriver driver) {
        // Pause the content if it is playing now
        List<WebElement> pauseContentPlaybackButtons = driver.findElements(By.xpath("//button[@data-purpose='pause-button']"));
        if (pauseContentPlaybackButtons.size() > 0) {
            System.out.println("Content playback is active");
            pauseContentPlaybackButtons.get(0).click();
            System.out.println("Content playback is paused");
        }
    }

    public static boolean isContentPlayback(WebDriver driver) {
        List<WebElement> pauseContentPlaybackButtons = driver.findElements(By.xpath("//button[@data-purpose='pause-button']"));
        if (pauseContentPlaybackButtons.size() > 0) {
            System.out.println("Content playback is active");
            return true;
        } else {
            System.out.println("Content playback is paused");
            return false;
        }
    }

    public static void playContent(WebDriver driver) {
        // Play the content if it is not playing yet
        List<WebElement> playContentButtons = driver.findElements(By.xpath("//button[@data-purpose='play-button']"));
        if (playContentButtons.size() > 0) {
            System.out.println("Content playback is paused");
            playContentButtons.get(0).click();
            System.out.println("Attempting to start content playback");
        }
    }

    public static String millisecondsToDateString(long milliseconds) {
        // Create a Date object from the current time in milliseconds
        Date currentDate = new Date(milliseconds);
        // Create a SimpleDateFormat object to specify the desired date and time format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Format the date as a string
        String formattedDateTime = dateFormat.format(currentDate);

        return formattedDateTime;
    }

}