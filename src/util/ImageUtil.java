package util;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ImageUtil Class
 * Helper utility to load, scale, and map vehicle images dynamically.
 * BCA 5th Semester PBL Project.
 */
public class ImageUtil {

    private static final String BASE_IMAGE_DIR = "assets/images/";

    /**
     * Resolves the appropriate image file for a vehicle based on its model name or type.
     */
    public static ImageIcon getVehicleIcon(String modelName, int width, int height) {
        String filename = "default_vehicle.jpg";

        if (modelName != null) {
            String lower = modelName.toLowerCase();
            if (lower.contains("honda") || lower.contains("city")) {
                filename = "honda_city.jpg";
            } else if (lower.contains("innova")) {
                filename = "toyota_innova.jpg";
            } else if (lower.contains("creta")) {
                filename = "hyundai_creta.jpg";
            } else if (lower.contains("enfield") || lower.contains("classic") || lower.contains("bike")) {
                filename = "royal_enfield.jpg";
            } else if (lower.contains("thar")) {
                filename = "mahindra_thar.jpg";
            }
        }

        File imgFile = new File(BASE_IMAGE_DIR + filename);
        if (imgFile.exists()) {
            try {
                ImageIcon rawIcon = new ImageIcon(imgFile.getAbsolutePath());
                Image scaled = rawIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } catch (Exception e) {
                System.err.println("Could not scale vehicle image: " + e.getMessage());
            }
        }

        // Fallback placeholder icon if image file not found
        return null;
    }

    /**
     * Loads the DriveEasy application logo.
     */
    public static ImageIcon getAppLogo(int width, int height) {
        File imgFile = new File(BASE_IMAGE_DIR + "drive_easy_logo.jpg");
        if (imgFile.exists()) {
            try {
                ImageIcon rawIcon = new ImageIcon(imgFile.getAbsolutePath());
                Image scaled = rawIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } catch (Exception e) {
                System.err.println("Could not load app logo: " + e.getMessage());
            }
        }
        return null;
    }
}
