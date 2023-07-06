package utils;

import io.obswebsocket.community.client.*;
import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;

public class OBScontroller {
    private final OBSRemoteController controller;

    public OBScontroller() {
        controller = OBSRemoteController.builder()
                .autoConnect(false)
                .host("localhost")
                .port(4455)
                .password("FX2MWJMEZXJlbtyD")
                .connectionTimeout(3)
                .build();

        controller.connect();
    }

    public void startRecording() {
        controller.sendRequest(StartRecordRequest.builder().build(), startRecordResponse -> {
            if (startRecordResponse.isSuccessful()) {
                System.out.println("Recording started successfully");
                // Sleep for 10 seconds to allow OBS to start recording
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Failed to start recording: " + startRecordResponse.getClass());
            }
        });
    }

    public void stopRecording(String fileName) {
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

            System.out.println(FilePathExtractor.renameFile(filePath, fileName));

            // Disconnect from OBS
            controller.disconnect();
        });
    }
}

