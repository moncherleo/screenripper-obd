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
            } else {
                System.out.println("Failed to start recording: " + startRecordResponse.getMessageData());
            }
        });


    }

    @Override
    protected void finalize() throws Throwable {
        try {
            // Call the method prior to object destruction
            controller.disconnect();
        } finally {
            // Call the finalize() method of the superclass
            super.finalize();
        }
    }

    public void stopRecording(String fileName) {
        controller.sendRequest(StopRecordRequest.builder().build(), stopRecordResponse -> {
            if (stopRecordResponse.isSuccessful()) {
                System.out.println("Recording stopped successfully");
            } else {
                System.out.println("Failed to stop recording: " + stopRecordResponse.getMessageData());
            }

            // Wait until recording is finished
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//
//            String messageData = stopRecordResponse.getMessageData().toString();
//            System.out.println(messageData);
//            String filePath = FilePathExtractor.extractFilePath(messageData);
//            System.out.println(filePath);
//
//            System.out.println(FilePathExtractor.renameFile(filePath, fileName));

        });
    }
}

