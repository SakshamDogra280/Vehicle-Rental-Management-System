package ui;

import model.Booking;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * InvoiceDialog Class
 * Generates and prints/exports official, beautifully formatted rental tax invoices.
 * Final Phase: Implements Automated Billing & Printable Invoice Generation.
 * Demonstrates: HTML rendering in Swing, File I/O, Java Print API integration.
 */
public class InvoiceDialog extends JDialog {

    private final Booking booking;
    private JEditorPane editorInvoice;

    public InvoiceDialog(JFrame parent, Booking booking) {
        super(parent, "DriveEasy - Rental Tax Invoice #" + booking.getBookingId(), true);
        this.booking = booking;

        setSize(650, 720);
        setLocationRelativeTo(parent);
        setResizable(true);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        mainPanel.setBackground(new Color(241, 245, 249));

        // Invoice display pane (renders HTML)
        editorInvoice = new JEditorPane();
        editorInvoice.setContentType("text/html");
        editorInvoice.setEditable(false);
        editorInvoice.setText(generateInvoiceHtml());
        editorInvoice.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorInvoice);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Action Toolbar
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.setOpaque(false);

        JButton btnClose = new JButton("Close");
        UITheme.styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        JButton btnSaveFile = new JButton("Save / Export Receipt");
        UITheme.stylePrimaryButton(btnSaveFile);
        btnSaveFile.addActionListener(e -> handleSaveToFile());

        JButton btnPrint = new JButton("Print Invoice");
        UITheme.styleSuccessButton(btnPrint);
        btnPrint.addActionListener(e -> handlePrint());

        actionPanel.add(btnClose);
        actionPanel.add(btnSaveFile);
        actionPanel.add(btnPrint);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private String generateInvoiceHtml() {
        double baseFare = booking.getTotalAmount();
        double lateFee = booking.getLateFee();
        double grandTotal = booking.getGrandTotal();

        // 18% GST calculation (split into 9% CGST + 9% SGST)
        double taxableValue = grandTotal / 1.18;
        double totalGst = grandTotal - taxableValue;
        double cgst = totalGst / 2.0;
        double sgst = totalGst / 2.0;

        String txn = booking.getTransactionId() != null ? booking.getTransactionId() : "N/A";
        String payMethod = booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "Cash";
        String payStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "PAID";
        String returnStr = booking.getActualReturnDate() != null ? booking.getActualReturnDate().toString() : "Pending Return";

        return "<html><body style='font-family: Segoe UI, sans-serif; margin: 15px; color: #1e293b;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0'>" +
                "  <tr>" +
                "    <td>" +
                "      <h1 style='color: #1e3a8a; margin: 0; font-size: 24px;'>DriveEasy Rentals Pvt Ltd</h1>" +
                "      <p style='color: #64748b; margin: 3px 0;'>Official Vehicle Rental & Fleet Logistics</p>" +
                "      <p style='color: #64748b; font-size: 11px; margin: 2px 0;'>GSTIN: 05AAACD1234E1Z8 | Contact: support@driveeasy.com</p>" +
                "    </td>" +
                "    <td align='right'>" +
                "      <h2 style='color: #475569; margin: 0;'>RENTAL INVOICE</h2>" +
                "      <p style='margin: 4px 0;'><b>Invoice No:</b> INV-" + String.format("%05d", booking.getBookingId()) + "</p>" +
                "      <p style='margin: 2px 0; color: #64748b;'><b>Date:</b> " + (booking.getBookingDate() != null ? booking.getBookingDate().substring(0, 10) : "2026-11-04") + "</p>" +
                "    </td>" +
                "  </tr>" +
                "</table>" +
                "<hr style='border: 1px solid #cbd5e1; margin: 12px 0;'>" +

                // Customer & Vehicle summary tables
                "<table width='100%' cellpadding='6' cellspacing='0' style='margin-bottom: 12px; background: #f8fafc; border: 1px solid #e2e8f0;'>" +
                "  <tr>" +
                "    <td width='50%' valign='top'>" +
                "      <b style='color: #1e40af;'>CUSTOMER INFORMATION:</b><br>" +
                "      Name: <b>" + (booking.getCustomerName() != null ? booking.getCustomerName() : "Valued Customer") + "</b><br>" +
                "      Customer ID: #" + booking.getCustomerId() + "<br>" +
                "      Phone: " + (booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "N/A") + "<br>" +
                "      Email: " + (booking.getCustomerEmail() != null ? booking.getCustomerEmail() : "N/A") + "" +
                "    </td>" +
                "    <td width='50%' valign='top'>" +
                "      <b style='color: #1e40af;'>VEHICLE & SCHEDULE:</b><br>" +
                "      Model: <b>" + booking.getVehicleName() + " (" + booking.getVehicleNumber() + ")</b><br>" +
                "      Vehicle Type: " + (booking.getVehicleType() != null ? booking.getVehicleType() : "Standard") + "<br>" +
                "      Period: " + booking.getStartDate() + " to " + booking.getEndDate() + " (" + booking.getTotalDays() + " days)<br>" +
                "      Actual Return: <b>" + returnStr + "</b>" +
                "    </td>" +
                "  </tr>" +
                "</table>" +

                // Charges itemized breakdown table
                "<table width='100%' cellpadding='8' cellspacing='0' style='border-collapse: collapse; border: 1px solid #cbd5e1;'>" +
                "  <tr style='background: #1e293b; color: #ffffff;'>" +
                "    <th align='left'>Description</th>" +
                "    <th align='center'>Rate / Day</th>" +
                "    <th align='center'>Duration</th>" +
                "    <th align='right'>Amount (₹)</th>" +
                "  </tr>" +
                "  <tr style='border-bottom: 1px solid #e2e8f0;'>" +
                "    <td>Base Vehicle Rental (" + booking.getVehicleName() + ")</td>" +
                "    <td align='center'>₹" + String.format("%.2f", (baseFare / Math.max(1, booking.getTotalDays()))) + "</td>" +
                "    <td align='center'>" + booking.getTotalDays() + " day(s)</td>" +
                "    <td align='right'>₹" + String.format("%.2f", baseFare) + "</td>" +
                "  </tr>" +
                (lateFee > 0 ?
                "  <tr style='border-bottom: 1px solid #e2e8f0; background: #fff1f2; color: #be123c;'>" +
                "    <td>⚠️ Late Return Penalty (Overdue Fee)</td>" +
                "    <td align='center'>₹500.00 / day</td>" +
                "    <td align='center'>" + Math.round(lateFee / 500.0) + " day(s)</td>" +
                "    <td align='right'>₹" + String.format("%.2f", lateFee) + "</td>" +
                "  </tr>" : "") +
                "  <tr style='border-bottom: 1px solid #e2e8f0;'>" +
                "    <td colspan='3' align='right'><b>Taxable Base Amount:</b></td>" +
                "    <td align='right'>₹" + String.format("%.2f", taxableValue) + "</td>" +
                "  </tr>" +
                "  <tr style='border-bottom: 1px solid #e2e8f0; font-size: 11px; color: #64748b;'>" +
                "    <td colspan='3' align='right'>Central GST (CGST @ 9%):</td>" +
                "    <td align='right'>₹" + String.format("%.2f", cgst) + "</td>" +
                "  </tr>" +
                "  <tr style='border-bottom: 1px solid #e2e8f0; font-size: 11px; color: #64748b;'>" +
                "    <td colspan='3' align='right'>State GST (SGST @ 9%):</td>" +
                "    <td align='right'>₹" + String.format("%.2f", sgst) + "</td>" +
                "  </tr>" +
                "  <tr style='background: #f1f5f9; font-size: 15px;'>" +
                "    <td colspan='3' align='right'><b style='color: #0f172a;'>GRAND TOTAL (Incl. All Taxes):</b></td>" +
                "    <td align='right'><b style='color: #16a34a;'>₹" + String.format("%.2f", grandTotal) + "</b></td>" +
                "  </tr>" +
                "</table>" +

                // Payment and Authorization footer
                "<table width='100%' cellpadding='8' cellspacing='0' style='margin-top: 15px;'>" +
                "  <tr>" +
                "    <td width='60%' style='font-size: 11px; color: #64748b; line-height: 1.4;'>" +
                "      <b>Payment Method:</b> " + payMethod + "<br>" +
                "      <b>Payment Status:</b> <span style='color: #15803d;'><b>" + payStatus + "</b></span><br>" +
                "      <b>Transaction ID:</b> " + txn + "<br>" +
                "      <b>Booking Status:</b> " + booking.getBookingStatus() + "<br>" +
                "      <i>Thank you for choosing DriveEasy. Have a safe journey!</i>" +
                "    </td>" +
                "    <td width='40%' align='center' style='border: 1px dashed #cbd5e1; background: #fafafa;'>" +
                "      <br><br>" +
                "      <p style='margin: 0; font-size: 12px; font-weight: bold;'>Authorized Signatory</p>" +
                "      <p style='margin: 0; font-size: 10px; color: #64748b;'>DriveEasy Digital Invoice</p>" +
                "    </td>" +
                "  </tr>" +
                "</table>" +
                "</body></html>";
    }

    private void handleSaveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Rental Invoice");
        fileChooser.setSelectedFile(new File("DriveEasy_Invoice_" + booking.getBookingId() + ".html"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(generateInvoiceHtml());
                JOptionPane.showMessageDialog(this,
                        "Invoice saved successfully!\n" + fileToSave.getAbsolutePath(),
                        "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save invoice: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handlePrint() {
        try {
            boolean complete = editorInvoice.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Print Job Completed Successfully!", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Printing Error: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
