package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * RevenueChartPanel Class
 * Custom Java Swing component that renders visual bar charts using Graphics2D.
 * Final Phase: Provides visual data reporting and analytics without heavy external libraries.
 * Demonstrates: Java 2D Graphics, Custom Component painting, Responsive rendering.
 */
public class RevenueChartPanel extends JPanel {

    private Map<String, Double> revenueByType;
    private Map<String, Integer> bookingsByType;

    private static final Color[] BAR_COLORS = {
            new Color(59, 130, 246),  // Vibrant Blue (Car)
            new Color(139, 92, 246),  // Royal Purple (SUV)
            new Color(16, 185, 129),  // Emerald Green (Bike)
            new Color(245, 158, 11),  // Amber (Other)
            new Color(239, 68, 68)    // Crimson Red
    };

    public RevenueChartPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    public void setData(Map<String, Double> revenueByType, Map<String, Integer> bookingsByType) {
        this.revenueByType = revenueByType;
        this.bookingsByType = bookingsByType;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Chart Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(new Color(30, 41, 59));
        g2.drawString("Revenue & Rental Distribution by Vehicle Category (₹)", 20, 25);

        if (revenueByType == null || revenueByType.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.setColor(Color.GRAY);
            g2.drawString("No rental transactions recorded yet.", width / 2 - 100, height / 2);
            return;
        }

        // Calculate max revenue for proportional scaling
        double maxRev = 1000.0;
        for (double val : revenueByType.values()) {
            if (val > maxRev) maxRev = val;
        }

        int paddingLeft = 60;
        int paddingRight = 40;
        int paddingTop = 50;
        int paddingBottom = 55;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;
        int baseLineY = height - paddingBottom;

        // Draw horizontal grid lines & Y-Axis values
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(226, 232, 240));
        for (int i = 0; i <= 4; i++) {
            int gridY = baseLineY - (chartHeight * i / 4);
            g2.drawLine(paddingLeft, gridY, width - paddingRight, gridY);

            double gridVal = maxRev * i / 4.0;
            g2.setColor(new Color(100, 116, 139));
            g2.drawString("₹" + Math.round(gridVal), 10, gridY + 4);
            g2.setColor(new Color(226, 232, 240));
        }

        // Draw baseline
        g2.setColor(new Color(148, 163, 184));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(paddingLeft, baseLineY, width - paddingRight, baseLineY);

        int categoryCount = revenueByType.size();
        int barSlotWidth = chartWidth / categoryCount;
        int barWidth = Math.min(70, (int) (barSlotWidth * 0.55));

        int idx = 0;
        for (Map.Entry<String, Double> entry : revenueByType.entrySet()) {
            String type = entry.getKey();
            double rev = entry.getValue();
            int bCount = (bookingsByType != null && bookingsByType.containsKey(type)) ? bookingsByType.get(type) : 0;

            int barHeight = (int) ((rev / maxRev) * chartHeight);
            int barX = paddingLeft + (idx * barSlotWidth) + ((barSlotWidth - barWidth) / 2);
            int barY = baseLineY - barHeight;

            Color barColor = BAR_COLORS[idx % BAR_COLORS.length];

            // Render Bar with soft gradient
            GradientPaint gradient = new GradientPaint(
                    barX, barY, barColor.brighter(),
                    barX, baseLineY, barColor
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(barX, barY, barWidth, Math.max(4, barHeight), 8, 8);

            // Bar outline
            g2.setColor(barColor.darker());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(barX, barY, barWidth, Math.max(4, barHeight), 8, 8);

            // Revenue label above bar
            g2.setColor(new Color(15, 23, 42));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String revStr = "₹" + Math.round(rev);
            int strW = g2.getFontMetrics().stringWidth(revStr);
            g2.drawString(revStr, barX + (barWidth - strW) / 2, barY - 6);

            // Category name & booking count under bar
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(51, 65, 85));
            int typeW = g2.getFontMetrics().stringWidth(type);
            g2.drawString(type, barX + (barWidth - typeW) / 2, baseLineY + 18);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(100, 116, 139));
            String countStr = bCount + " booking(s)";
            int countW = g2.getFontMetrics().stringWidth(countStr);
            g2.drawString(countStr, barX + (barWidth - countW) / 2, baseLineY + 32);

            idx++;
        }
    }
}
