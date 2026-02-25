import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private RegistrationPage registrationPage;
    private AttendancePage attendancePage;

    public MainApp() {
        super("Face Authentication System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Create CardLayout and JPanel to hold the cards
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add content of pages to cardPanel
        cardPanel.add(createLandingPage(), "landing");
        registrationPage = new RegistrationPage(this);
        attendancePage = new AttendancePage(this);
        cardPanel.add(registrationPage.getContentPane(), "registration");
        cardPanel.add(attendancePage, "attendance");

        // Add cardPanel to JFrame
        add(cardPanel);

        // Show landing page initially
        showLandingPage();
    }

    private JPanel createLandingPage() {
        JPanel landingPanel = new JPanel(new GridBagLayout()) {
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 80, 0);  // Adjust insets to move the logo up

        // Logo at the top
        JLabel logoLabel = new JLabel(scaleImageIcon("V:\\Internship\\CodeClause\\Face Authentication System\\FaceLogo.png", 150, 150));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        landingPanel.add(logoLabel, gbc);

        // Title label
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 50, 0);  // Adjust insets for title label if necessary
        JLabel titleLabel = new JLabel("Login by Face", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Courier New", Font.BOLD, 32));
        landingPanel.add(titleLabel, gbc);

        // Button panel
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 20, 20, 20);
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        buttonPanel.setOpaque(false);

        JButton registerButton = createRoundButton("Register");
        JButton attendanceButton = createRoundButton("Mark Attendance");

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrationPage();
            }
        });

        attendanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAttendancePage();
            }
        });

        buttonPanel.add(registerButton);
        buttonPanel.add(attendanceButton);

        gbc.gridx = 0;
        gbc.gridy++;
        landingPanel.add(registerButton, gbc);
        gbc.gridx++;
        landingPanel.add(attendanceButton, gbc);

        return landingPanel;
    }

    private JButton createRoundButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(new Color(60, 110, 180));
                } else {
                    g.setColor(getBackground());
                }
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Fill the round rectangle
                super.paintComponent(g); // Paint the text and border
            }

            @Override
            protected void paintBorder(Graphics g) {
                g.setColor(getBackground().darker());
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(180, 40); // Smaller size
            }

            @Override
            public void setContentAreaFilled(boolean b) {
                // Do not paint the default button background
            }
        };
        button.setFont(new Font("Courier New", Font.BOLD, 16)); // Smaller font
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }


    private ImageIcon scaleImageIcon(String path, int width, int height) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void showLandingPage() {
        cardLayout.show(cardPanel, "landing");
    }

    public void showRegistrationPage() {
        cardLayout.show(cardPanel, "registration");
        registrationPage.resetFields();
        registrationPage.registerationCamera();
    }

    public void showAttendancePage() {
        cardLayout.show(cardPanel, "attendance");
        attendancePage.resetFields();
        attendancePage.attendanceCamera();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainApp mainApp = new MainApp();
            mainApp.setVisible(true);
        });
    }
}


