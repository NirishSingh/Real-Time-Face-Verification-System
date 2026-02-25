# Secure Face Authentication System

## Overview
This project implements a cutting-edge face authentication system using Java, OpenCV for face detection and recognition, Java Swing for UI, and File I/O for storing facial features. The system allows users to register by enrolling their facial features, develops algorithms for capturing, storing, and recognizing facial patterns, and implements a secure login system that verifies the user's identity through facial recognition.

## Features
- User registration and face enrollment
- Facial feature capturing, storage, and recognition
- Secure login system with facial recognition authentication
- Java Swing for intuitive user interface
- File I/O for secure data management

## Technologies Used
- Java
- OpenCV
- Java Swing/JavaFX
- File I/O

## Installation
1. Clone the repository.
2. Ensure Java and OpenCV are installed on your system.
3. Build and run the project using your preferred Java IDE.

## Usage
1. Register by enrolling your facial features.
2. Log in securely using facial recognition.

## Main Classes and Logic
### MainApp.java
- Acts as the entry point of the application.
- Initializes the UI components and sets up the main interface for user interaction.
- Manages the flow of the application and handles events such as registration and authentication.

### CameraManager.java
- Handles camera initialization and captures facial features for registration and authentication.
- Utilizes OpenCV for face detection and recognition.
- Implements methods for starting and stopping the camera.

### RegistrationPage.java
- Provides the UI for user registration.
- Collects facial features and sends them for enrollment.
- Communicates with CameraManager for capturing facial data.

### LoginPage.java
- Displays the login interface.
- Initiates the facial recognition process for authentication.
- Communicates with CameraManager for capturing and matching facial features.
- Implements secure login with hashed passwords for additional encryption.

### FaceRecognition.java
- Implements face recognition using Haar cascades ML algorithm.
- Matches the stored face patterns with detected facial features.
- Utilizes thresholding techniques to verify facial recognition.
- Implements advanced image processing algorithms for accurate matching.
- Ensures high-security standards in facial authentication.

### File Handling 

## Attendance
Attendance data is marked and stored in `attendance.csv` for record-keeping and tracking.

