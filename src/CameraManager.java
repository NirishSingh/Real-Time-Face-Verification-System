import org.opencv.videoio.VideoCapture;

public class CameraManager {
    private static VideoCapture camera;

    public static VideoCapture getCamera() {
        if (camera == null) {
            camera = new VideoCapture(0); // Open default camera
        }
        return camera;
    }

    public static void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}

