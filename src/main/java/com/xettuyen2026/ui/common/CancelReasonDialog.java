package com.xettuyen2026.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog yêu cầu nhập lý do hủy nguyện vọng.
 * Chỉ cho phép xác nhận khi đã nhập lý do và tích checkbox "Đã chắc chắn".
 */
public class CancelReasonDialog extends JDialog {

    private boolean confirmed = false;
    private RoundedButton btnSubmit;

    public CancelReasonDialog(Window owner, String ten, String cccd, String maNganh) {
        super(owner, "Xác nhận xóa thí sinh", ModalityType.APPLICATION_MODAL);
        setSize(480, 220);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI(ten, cccd, maNganh);
    }

    private void initUI(String ten, String cccd, String maNganh) {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        main.setBackground(Color.WHITE);

        // ── Header: icon + title ──
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("<html><b>Xác nhận xóa thí sinh</b><br>"
                + "<span style='color:#666'>Tên thí sinh: " + (ten != null ? ten : "") + "</span><br>"
                + "<span style='color:#666'>CCCD: " + cccd + "</span><br>"
                + "<span style='color:#666'>Mã ngành: " + maNganh + "</span></html>");
        titleLabel.setFont(UIConstants.FONT_REGULAR);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        main.add(headerPanel, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Đóng", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        btnSubmit = new RoundedButton(UIConstants.ICON_DELETE + " Xác nhận xóa", UIConstants.DANGER);
        btnSubmit.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static boolean showConfirm(Component parent, String ten, String cccd, String maNganh) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        CancelReasonDialog dlg = new CancelReasonDialog(owner, ten, cccd, maNganh);
        dlg.setVisible(true);
        return dlg.isConfirmed();
    }
}
