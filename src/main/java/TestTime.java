import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTime {

    public static void main(String[] args) {
        Robot robot;

        int requiredDelayMs = 5 * 1000;
        long startTimeMs = System.currentTimeMillis();
        long endTimeMs = startTimeMs + requiredDelayMs;


//        // Create an instance of the Robot class
//        try {
//            robot = new Robot();
//        } catch (AWTException e) {
//            throw new RuntimeException(e);
//        }

        // Create a Date object from the current time in milliseconds
        Date currentDate = new Date(System.currentTimeMillis());
        // Create a SimpleDateFormat object to specify the desired date and time format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Format the date as a string
        String formattedDateTime = dateFormat.format(currentDate);
        System.out.println("Current date and time is: " + formattedDateTime);

        Date expectedDate = new Date(endTimeMs);
        // Create a SimpleDateFormat object to specify the desired date and time format
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Format the date as a string
        formattedDateTime = dateFormat.format(expectedDate);
        System.out.println("Expected end date and time is: " + formattedDateTime);

        // visualTestHelper = new VisualTestHelper();
        while (System.currentTimeMillis() < endTimeMs) {
            // Create a Date object from the current time in milliseconds
            currentDate = new Date(System.currentTimeMillis());
            // Create a SimpleDateFormat object to specify the desired date and time format
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Format the date as a string
            formattedDateTime = dateFormat.format(currentDate);
            System.out.println("Current date and time in cycle is: " + formattedDateTime);


            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Perform your desired actions here
            // This loop will repeat until the current time reaches the target end time
            // Move mouse to absolute position
//            for (int i = 0; i < 5; i++) {
//                //visualTestHelper.moveMouseToLocation(20 + i, 200);
////                robot.mouseMove(200 + i, 200);
////                if (i == 4) {
////                    i = 0;
////                }
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
        }
    }
}
