package util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * UITheme Utility Class
 * Provides consistent, high-contrast, modern styles for buttons and components.
 * Solves the Windows Look-and-Feel button background rendering issue.
 * BCA 5th Semester PBL Project.
 */
public class UITheme {

    // Common Color Palette
    public static final Color PRIMARY = new Color(24, 90, 219);     // Modern Royal Blue (Login, Book)
    public static final Color SUCCESS = new Color(22, 138, 63);     // Forest Green (Register, Add)
    public static final Color DANGER  = new Color(220, 38, 38);     // Crimson Red (Logout, Delete)
    public static final Color DARK    = new Color(30, 41, 59);      // Slate Dark
    public static final Color LIGHT_BG = new Color(248, 250, 252);  // Light background

    /**
     * Styles a primary action button (e.g., Login, Book Now)
     */
    public static void stylePrimaryButton(JButton button) {
        styleSolidButton(button, PRIMARY, Color.WHITE, 14);
    }

    /**
     * Styles a success action button (e.g., Register Now, Save)
     */
    public static void styleSuccessButton(JButton button) {
        styleSolidButton(button, SUCCESS, Color.WHITE, 13);
    }

    /**
     * Styles a danger action button (e.g., Logout, Delete)
     */
    public static void styleDangerButton(JButton button) {
        styleSolidButton(button, DANGER, Color.WHITE, 12);
    }

    /**
     * Styles a secondary / neutral button (e.g., Back, Cancel)
     */
    public static void styleSecondaryButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(241, 245, 249));
        button.setForeground(new Color(30, 41, 59));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(7, 14, 7, 14)
        ));
    }

    /**
     * Internal helper to apply solid background and crisp text with BasicButtonUI
     */
    private static void styleSolidButton(JButton button, Color bgColor, Color fgColor, int fontSize) {
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
    }
}
