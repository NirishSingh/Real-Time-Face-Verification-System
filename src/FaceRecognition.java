import org.opencv.core.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import java.util.List;

public class FaceRecognition {

    private final CascadeClassifier faceDetector;

    public FaceRecognition() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        faceDetector = new CascadeClassifier("V:\\Internship\\CodeClause\\Face Authentication System\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_default.xml");
        if (faceDetector.empty()) {
            throw new RuntimeException("Failed to load face detector.");
        }
    }


    public boolean recognizeFace(Mat detectedFace, Mat registeredFace) {
        Mat detectedFaceGray = new Mat();
        Mat registeredFaceGray = new Mat();
        Imgproc.cvtColor(detectedFace, detectedFaceGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(registeredFace, registeredFaceGray, Imgproc.COLOR_BGR2GRAY);

        // Compute histograms
        Mat histDetected = calculateHistogram(detectedFaceGray);
        Mat histRegistered = calculateHistogram(registeredFaceGray);

        // Compare histograms using Chi-Square distance
        double chiSquareDistance = Imgproc.compareHist(histDetected, histRegistered, Imgproc.CV_COMP_CHISQR);

        // You can adjust the threshold based on experimentation
        double threshold = 3000;

        return chiSquareDistance < threshold;
    }

    private Mat calculateHistogram(Mat image) {
        Mat hist = new Mat();
        Imgproc.calcHist(List.of(image), new MatOfInt(0), new Mat(), hist, new MatOfInt(256), new MatOfFloat(0, 256));
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        return hist;
    }
}






