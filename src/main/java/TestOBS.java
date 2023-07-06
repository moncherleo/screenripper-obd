import utils.OBScontroller;

public class TestOBS {
    public static void main(String[] args) {
        OBScontroller obScontroller = new OBScontroller();

        for (int i = 0; i < 4; i++){
            obScontroller.startRecording();

            try {
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            obScontroller.stopRecording("Test_" + i);
        }
    }
}
