//// AI Violation Detection Service
//package com.almubaraksuleiman.cbts.proctor.service.impl;
//
//import com.almubaraksuleiman.cbts.proctor.model.Severity;
//import com.almubaraksuleiman.cbts.proctor.model.ViolationType;
//import lombok.extern.slf4j.Slf4j;
//import org.opencv.core.*;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.objdetect.CascadeClassifier;
//import org.springframework.stereotype.Service;
//import org.tensorflow.SavedModelBundle;
//import org.tensorflow.Tensor;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@Slf4j
//public class AIViolationDetectionService {
//
//    private CascadeClassifier faceCascade;
//    private SavedModelBundle phoneDetectionModel;
//    private boolean modelsLoaded = false;
//
//    public AIViolationDetectionService() {
//        loadModels();
//    }
//
//    private void loadModels() {
//        try {
//            // Load OpenCV face detection
//            faceCascade = new CascadeClassifier();
//            faceCascade.load("haarcascade_frontalface_default.xml");
//
//            // Load TensorFlow model for phone detection
//            phoneDetectionModel = SavedModelBundle.load("models/phone_detection", "serve");
//
//            modelsLoaded = true;
//            log.info("AI models loaded successfully");
//        } catch (Exception e) {
//            log.error("Failed to load AI models: {}", e.getMessage());
//        }
//    }
//
//    public Map<String, Object> analyzeFrame(byte[] frameData, Long sessionId) {
//        Map<String, Object> results = new HashMap<>();
//
//        if (!modelsLoaded) {
//            log.warn("AI models not loaded, skipping analysis");
//            return results;
//        }
//
//        try {
//            // Convert byte array to BufferedImage
//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(frameData));
//
//            // Detect faces
//            int faceCount = detectFaces(image);
//            results.put("faceCount", faceCount);
//
//            // Detect phones
//            boolean phoneDetected = detectPhone(image);
//            results.put("phoneDetected", phoneDetected);
//
//            // Detect gaze direction (simplified)
//            String gazeDirection = analyzeGaze(image);
//            results.put("gazeDirection", gazeDirection);
//
//            log.debug("Frame analysis completed for session {}: {} faces, phone: {}",
//                     sessionId, faceCount, phoneDetected);
//
//        } catch (Exception e) {
//            log.error("Error analyzing frame for session {}: {}", sessionId, e.getMessage());
//        }
//
//        return results;
//    }
//
//    private int detectFaces(BufferedImage image) {
//        try {
//            // Convert BufferedImage to OpenCV Mat
//            Mat mat = bufferedImageToMat(image);
//
//            // Convert to grayscale for face detection
//            Mat gray = new Mat();
//            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
//
//            // Detect faces
//            MatOfRect faces = new MatOfRect();
//            faceCascade.detectMultiScale(gray, faces);
//
//            return faces.toArray().length;
//        } catch (Exception e) {
//            log.error("Face detection error: {}", e.getMessage());
//            return 0;
//        }
//    }
//
//    private boolean detectPhone(BufferedImage image) {
//        try {
//            // Simplified phone detection logic
//            // In production, this would use the TensorFlow model
//            Mat mat = bufferedImageToMat(image);
//
//            // Basic color analysis for phone detection
//            Scalar meanColor = Core.mean(mat);
//            double blue = meanColor.val[0];
//            double green = meanColor.val[1];
//            double red = meanColor.val[2];
//
//            // Simple heuristic for phone detection
//            return (blue > 100 && green > 100 && red > 100) &&
//                   (Math.abs(blue - green) < 30 && Math.abs(green - red) < 30);
//
//        } catch (Exception e) {
//            log.error("Phone detection error: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    private String analyzeGaze(BufferedImage image) {
//        // Simplified gaze analysis
//        // In production, this would use advanced computer vision
//        return "CENTER"; // CENTER, LEFT, RIGHT, UP, DOWN
//    }
//
//    private Mat bufferedImageToMat(BufferedImage image) {
//        // Implementation to convert BufferedImage to OpenCV Mat
//        // This is a simplified version - actual implementation would be more complex
//        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
//        // ... conversion logic
//        return mat;
//    }
//
//    public ViolationType detectViolationType(Map<String, Object> analysisResults) {
//        int faceCount = (Integer) analysisResults.getOrDefault("faceCount", 0);
//        boolean phoneDetected = (Boolean) analysisResults.getOrDefault("phoneDetected", false);
//        String gazeDirection = (String) analysisResults.getOrDefault("gazeDirection", "CENTER");
//
//        if (faceCount == 0) {
//            return ViolationType.NO_FACE_DETECTED;
//        } else if (faceCount > 1) {
//            return ViolationType.MULTIPLE_FACES_DETECTED;
//        } else if (phoneDetected) {
//            return ViolationType.PHONE_DETECTED;
//        } else if (!"CENTER".equals(gazeDirection)) {
//            return ViolationType.EYE_MOVEMENT_SUSPICIOUS;
//        }
//
//        return null; // No violation detected
//    }
//
//    public Severity determineSeverity(ViolationType violationType) {
//        switch (violationType) {
//            case MULTIPLE_FACES_DETECTED:
//            case PHONE_DETECTED:
//                return Severity.HIGH;
//            case NO_FACE_DETECTED:
//                return Severity.MEDIUM;
//            case EYE_MOVEMENT_SUSPICIOUS:
//                return Severity.LOW;
//            default:
//                return Severity.LOW;
//        }
//    }
//}