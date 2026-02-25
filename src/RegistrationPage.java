import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class RegistrationPage extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private MainApp mainApp;
    private final CascadeClassifier faceDetector;
    private final VideoCapture camera;
    private final JLabel cameraFeedLabel;
    private final String userDataFilePath = "V:\\Internship\\CodeClause\\Face Authentication System\\registered_users.txt";
    private final String registeredFacesDirectory = "V:\\Internship\\CodeClause\\Face Authentication System\\registered_faces/";
    private Thread cameraThread;

    public RegistrationPage(MainApp mainApp) {
        super("Attendance System");
        this.mainApp = mainApp;

        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Initialize face detector and camera
        faceDetector = new CascadeClassifier("V:\\Internship\\CodeClause\\Face Authentication System\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_default.xml");
        if (faceDetector.empty()) {
            throw new RuntimeException("Failed to load face detector.");
        }
        camera = CameraManager.getCamera();

        // Set up the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Create a JPanel for displaying the camera feed
        cameraFeedLabel = new JLabel();
        cameraFeedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(cameraFeedLabel, BorderLayout.CENTER);

        // Panel for user registration
        JPanel registrationPanel = new JPanel(new GridBagLayout()) {
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

        // Setting layout for registrationPanel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;

        JLabel userIdLabel = new JLabel("User ID: ");
        userIdLabel.setFont(new Font("Courier New", Font.BOLD, 18));
        userIdLabel.setForeground(Color.BLACK);
        registrationPanel.add(userIdLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        userIdField = new JTextField(20);
        userIdField.setFont(new Font("Courier New", Font.PLAIN, 18));
        registrationPanel.add(userIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;

        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setFont(new Font("Courier New", Font.BOLD, 18));
        passwordLabel.setForeground(Color.BLACK);
        registrationPanel.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Courier New", Font.PLAIN, 18));
        registrationPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Register");
        registrationPanel.add(registerButton);
        registrationPanel.add(registerButton, gbc);

        // Adding registrationPanel to the frame
        add(registrationPanel, BorderLayout.SOUTH);


        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText();
                if (isUserAlreadyRegistered(userId)) {
                    JOptionPane.showMessageDialog(null, "User already registered");
                    resetFields();
                } else {
                    registerUser();
                }
            }
        });
    }

    public void registerationCamera() {
        cameraThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Mat frame = new Mat();
                if (camera.read(frame)) {
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(frame, faces, 1.1, 3, 0, new Size(30, 30), new Size());
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

    private boolean isUserAlreadyRegistered(String userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userDetails = line.split(",");
                if (userDetails[0].equals(userId)) {
                    return true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void registerUser() {
        String userId = userIdField.getText();
        String password = new String(passwordField.getPassword());
        String hashedPassword = hashPassword(password);

        Mat facialFeatures = captureFacialFeatures();
        if (facialFeatures.empty()) {
            JOptionPane.showMessageDialog(this, "Failed to capture face. Please try again.");
            return;
        }

        storeUserInfoAndFacialFeatures(userId, hashedPassword, facialFeatures);

        System.out.println("User registered: " + userId);
        registerButton.setEnabled(false);
        mainApp.showAttendancePage();

    }

    private Mat captureFacialFeatures() {
        Mat frame = new Mat();
        Mat facialFeatures = new Mat();

        if (camera.read(frame)) {
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(frame, faces, 1.1, 3, 0, new Size(30, 30), new Size());
            if (!faces.empty()) {
                Rect faceRect = faces.toArray()[0];
                facialFeatures = new Mat(frame, faceRect);
                Imgproc.resize(facialFeatures, facialFeatures, frame.size());
                Imgproc.rectangle(frame, faceRect.tl(), faceRect.br(), new Scalar(0, 255, 0), 3);
            }
        } else {
            System.out.println("Failed to capture frame from camera.");
        }

        return facialFeatures;
    }

    private void storeUserInfoAndFacialFeatures(String userId, String hashedPassword, Mat facialFeatures) {
        String filePath = registeredFacesDirectory + userId + ".jpg";
        Imgcodecs.imwrite(filePath, facialFeatures);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDataFilePath, true))) {
            writer.write(userId + "," + hashedPassword + "," + filePath);
            writer.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private BufferedImage MatToBufferedImage(Mat frame) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (frame.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = frame.channels() * frame.cols() * frame.rows();
        byte[] buffer = new byte[bufferSize];
        frame.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(frame.cols(), frame.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public void close() {
        CameraManager.releaseCamera();
    }


public void resetFields() {
        userIdField.setText("");
        passwordField.setText("");
        registerButton.setEnabled(true);
    }


}