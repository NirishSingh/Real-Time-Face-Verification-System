import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


public class AttendancePage extends JPanel {


    private JButton markAttendanceButton;
    private MainApp mainApp;
    private final CascadeClassifier faceDetector;
    private final VideoCapture camera;
    private final JLabel cameraFeedLabel;
    private final FaceRecognition faceRecognition;
    private final JTextField userIdField;
    private final JPasswordField passwordField;
    private final List<User> registeredUsers;
    private final String userDataFilePath = "V:\\Internship\\CodeClause\\Face Authentication System\\registered_users.txt";
    private final String registeredFacesDirectory = "V:\\Internship\\CodeClause\\Face Authentication System\\registered_faces/";
    private Thread cameraThread;


    public AttendancePage(MainApp mainApp) {
        super();
        this.mainApp = mainApp;

        // Load registered users
        registeredUsers = loadRegisteredUsers();

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Initialize face detector, camera, and face recognition
        faceDetector = new CascadeClassifier("V:\\Internship\\CodeClause\\Face Authentication System\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_default.xml");
        if (faceDetector.empty()) {
            throw new RuntimeException("Failed to load face detector.");
        }
       // camera = new VideoCapture(0); // Open default camera
        camera = CameraManager.getCamera();
        faceRecognition = new FaceRecognition();

        // Set up the JFrame
        mainApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Create a JPanel for displaying the camera feed
        cameraFeedLabel = new JLabel();
        cameraFeedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(cameraFeedLabel, BorderLayout.CENTER);

        // Panel for user mark attendance
        JPanel attendancePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background image
                Image backgroundImage;
                try {
                    backgroundImage = ImageIO.read(new File("V:\\Internship\\CodeClause\\Face Authentication System\\MainBackground.png"));
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // Setting layout for attendancePanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;

        JLabel userIdLabel = new JLabel("User ID: ");
        userIdLabel.setFont(new Font("Courier New", Font.BOLD, 18));
        userIdLabel.setForeground(Color.BLACK);
        attendancePanel.add(userIdLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        userIdField = new JTextField(20);
        userIdField.setFont(new Font("Courier New", Font.PLAIN, 18));
        attendancePanel.add(userIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;

        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setFont(new Font("Courier New", Font.BOLD, 18));
        passwordLabel.setForeground(Color.BLACK);
        attendancePanel.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Courier New", Font.PLAIN, 18));
        attendancePanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        markAttendanceButton = new JButton("Mark Attendance");
        attendancePanel.add(markAttendanceButton);
        attendancePanel.add(markAttendanceButton, gbc);

        // Adding attendancePanel to the frame
        add(attendancePanel, BorderLayout.SOUTH);

        markAttendanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call method to capture frame and mark attendance
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    captureAndDetectFaces(frame);
                } else {
                    System.out.println("Failed to capture frame from camera.");
                }
            }
        });

    }

    public void attendanceCamera() {
        cameraThread = new Thread(() -> {
            while (true) {
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(frame, faces);
                    for (Rect rect : faces.toArray()) {
                        Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0), 2);
                        Imgproc.putText(frame, "Face", new Point(rect.x, rect.y - 5), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 0, 0), 2);
                    }
                    BufferedImage bufferedImage = MatToBufferedImage(frame);
                    cameraFeedLabel.setIcon(new ImageIcon(bufferedImage));
                    cameraFeedLabel.repaint();
                } else {
                    System.out.println("Failed to capture frame from camera.");
                }
            }
        });
        cameraThread.start();
    }


    private List<User> loadRegisteredUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    users.add(new User(parts[0], parts[1],parts[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle file reading errors
        }
        return users;
    }


    private void captureAndDetectFaces(Mat frame) {

        BufferedImage bufferedImage;

        // Capture a single frame
        if (camera.read(frame)) {
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(frame, faces);

            boolean faceMatched = false; // Flag to track if any match is found

            // Draw rectangles around detected faces
            // Loop through each detected face
            for (Rect rect : faces.toArray()) {
                // Extract the detected face
                Mat detectedFace = new Mat(frame, rect);

                // Get the user ID from the text field
                String detectedUserId = userIdField.getText().trim(); // Extract detected user ID from text field

                // Load the registered face images from the directory
                File registeredFacesDir = new File(registeredFacesDirectory);
                File[] registeredFaceFiles = registeredFacesDir.listFiles();

                // Check if any registered face files exist
                if (registeredFaceFiles != null) {
                    // Loop through each registered face file
                    for (File file : registeredFaceFiles) {
                        // Load the registered face image
                        Mat registeredFace = Imgcodecs.imread(file.getAbsolutePath());

                        // Compare the detected face with the registered face
                        if (faceRecognition.recognizeFace(detectedFace, registeredFace)) {
                            // If a match is found, mark attendance for the recognized user
                            String fileName = file.getName();
                            String userId = fileName.substring(0, fileName.lastIndexOf('.')).trim(); // Assuming the file name is the user ID
                            userId = userId.trim(); // Trim any leading/trailing whitespace
                            if (userId.equals(detectedUserId)) {
                                markAttendance(detectedUserId, detectedFace); // Pass detected face and user ID to mark attendance
                                faceMatched = true;
                                return; // Exit the loop once attendance is marked
                            }
                        }
                        }
                    }
                }


            // If no match is found for any detected face, display an alert message
            if (!faceMatched) {
                int option = JOptionPane.showConfirmDialog(this, "User not found. Do you want to register?", "User Not Found", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    mainApp.showRegistrationPage(); // Assuming you have a method to navigate to the registration page
                } else {
                    resetFields(); // Reset the fields if the user chooses not to register
                }
            }

            // Convert Mat to BufferedImage for displaying
            bufferedImage = MatToBufferedImage(frame);

            // Display the camera feed
            cameraFeedLabel.setIcon(new ImageIcon(bufferedImage));
            cameraFeedLabel.repaint();

        } else {
            System.out.println("Failed to capture frame from camera.");
            // Optionally, display an error message or take alternative actions
        }
    }


    private BufferedImage MatToBufferedImage(Mat frame) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (frame.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = frame.channels() * frame.cols() * frame.rows();
        byte[] buffer = new byte[bufferSize];
        frame.get(0, 0, buffer); // Get all the pixels
        BufferedImage image = new BufferedImage(frame.cols(), frame.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    private void markAttendance(String userId, Mat detectedFace) {
        String attendanceFileName = "V:\\Internship\\CodeClause\\Face Authentication System\\Attendance.csv"; // Use the same file for attendance

        try (FileWriter writer = new FileWriter(attendanceFileName, true)) { // Append mode
            // Get current date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String dateStamp = dateFormat.format(new Date());
            String timeStamp = timeFormat.format(new Date());

            // Write attendance entry
            writer.append(userId)
                    .append(",")
                    .append(dateStamp)
                    .append(",")
                    .append(timeStamp)
                    .append("\n");
            writer.flush(); // Flush the writer to ensure data is written immediately
            System.out.println("Attendance marked for: " + userId);
            JOptionPane.showMessageDialog(this, "Attendance marked for userId: " + userId, "Attendance Marked", JOptionPane.INFORMATION_MESSAGE);
            // Close the application after marking attendance
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Error writing attendance file: " + e.getMessage());
        }
    }

    public void close() {
        CameraManager.releaseCamera();
    }


    public void resetFields() {
        userIdField.setText("");
        passwordField.setText("");
        markAttendanceButton.setEnabled(true);
    }


}