import io.obswebsocket.community.client.*;
import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;
import utils.FilePathExtractor;

public class OBSRecordingExample {
    public static void main(String[] args) {
        // Create an OBSRemoteController instance
        OBSRemoteController controller = OBSRemoteController.builder()
                .autoConnect(false)
                .host("localhost")
                .port(4455)
                .password("FX2MWJMEZXJlbtyD")
                .connectionTimeout(3)
                .build();

        controller.connect();

        // Send the startRecord request
        controller.sendRequest(StartRecordRequest.builder().build(), startRecordResponse -> {
            if (startRecordResponse.isSuccessful()) {
                System.out.println("Recording started successfully");

                // Sleep for 10 seconds to allow OBS to start recording
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Send the stopRecord request
                controller.sendRequest(StopRecordRequest.builder().build(), stopRecordResponse -> {

                    if (stopRecordResponse.isSuccessful()) {
                        System.out.println("Recording stopped successfully");
                    } else {
                        System.out.println("Failed to stop recording: " + stopRecordResponse.getClass());
                    }

                    String messageData = stopRecordResponse.getMessageData().toString();
                    System.out.println(messageData);
                    String filePath = FilePathExtractor.extractFilePath(messageData);
                    System.out.println(filePath);

                    // Sleep for 10 seconds to allow OBS to stop recording
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(FilePathExtractor.renameFile(filePath, "123"));

                    // Disconnect from OBS
                    controller.disconnect();
                });
            } else {
                System.out.println("Failed to start recording: " + startRecordResponse.getClass());

                // Disconnect from OBS
                controller.disconnect();
            }

        });
    }
}
