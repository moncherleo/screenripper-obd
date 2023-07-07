package utils;

import org.sikuli.script.*;

import java.util.Iterator;


public class VisualTestHelper {
    private String imagePath = "src/test/resources/images/";
    private Screen screen;

    public static void main(String[] args) {
        VisualTestHelper visualTestHelper = new VisualTestHelper();
        try {
            visualTestHelper.startRecording();
            visualTestHelper.stopRecording();
        } catch (FindFailed e) {
            throw new RuntimeException(e);
        }

    }

    public VisualTestHelper() {
        this.screen = new Screen();
        this.screen.setAutoWaitTimeout(240);
        ImagePath.add(imagePath);
    }

    public void startRecording() throws FindFailed {
        this.screen.find("start_recording.png");
        this.screen.click("start_recording.png");
    }

    public void stopRecording() throws FindFailed {
        this.screen.find("stop_recording.png");
        this.screen.click("stop_recording.png");
    }

    public boolean isRecordingStarted() {
        Iterator<Match> elements = null;
        try {
            elements = this.screen.findAll("start_recording.png");
        } catch (FindFailed e) {
            throw new RuntimeException(e);
        }
        return elements.hasNext();
    }

    public boolean isRecordingStopped() {
        Iterator<Match> elements = null;
        try {
            elements = this.screen.findAll("stop_recording.png");
        } catch (FindFailed e) {
            throw new RuntimeException(e);
        }
        return elements.hasNext();
    }

    public void closeSeleniumBrowserNotification() throws FindFailed {
        Iterator<Match> elements = null;
        try {
            elements = this.screen.findAll("selenium_close_button.png");
        } catch (FindFailed e) {
            throw new RuntimeException("Failed to close Selenium browser notification. ", e);
        }
        if (elements.hasNext()){
            elements.next().click();
        }
    }

    public void moveMouseToLocation(int posX, int posY){
        Mouse.move(new Location(posX, posX));
    }
}
