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
    private String reason = "";
    private JTextArea txtReason;
    private JCheckBox chkConfirm;
    private RoundedButton btnSubmit;

    public CancelReasonDialog(Window owner) {
        super(owner, "Hủy nguyện vọng", ModalityType.APPLICATION_MODAL);
        setSize(480, 320);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    private void initUI() {
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

        JLabel titleLabel = new JLabel("<html><b>Xác nhận hủy nguyện vọng</b><br>"
                + "<span style='color:#666'>Vui lòng nhập lý do hủy bên dưới:</span></html>");
        titleLabel.setFont(UIConstants.FONT_REGULAR);
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        main.add(headerPanel, BorderLayout.NORTH);

        // ── Center: reason text area + checkbox ──
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        txtReason = new JTextArea(4, 30);
        txtReason.setFont(UIConstants.FONT_REGULAR);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        txtReason.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSubmitState();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSubmitState();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSubmitState();
            }
        });

        JScrollPane scrollPane = new JScrollPane(txtReason);
        scrollPane.setBorder(null);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        chkConfirm = new JCheckBox("Đã chắc chắn với lý do trên");
        chkConfirm.setFont(UIConstants.FONT_REGULAR);
        chkConfirm.setOpaque(false);
        chkConfirm.setForeground(UIConstants.TEXT_PRIMARY);
        chkConfirm.addActionListener(e -> updateSubmitState());
        centerPanel.add(chkConfirm, BorderLayout.SOUTH);

        main.add(centerPanel, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Đóng", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        btnSubmit = new RoundedButton(UIConstants.ICON_DELETE + " Xác nhận hủy", UIConstants.DANGER);
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> {
            confirmed = true;
            reason = txtReason.getText().trim();
            dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void updateSubmitState() {
        boolean hasReason = txtReason.getText() != null && !txtReason.getText().trim().isEmpty();
        boolean isChecked = chkConfirm.isSelected();
        btnSubmit.setEnabled(hasReason && isChecked);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getReason() {
        return reason;
    }

    /**
     * Tiện ích: hiển thị dialog và trả về lý do nếu user xác nhận, hoặc null nếu
     * hủy.
     */
    public static String showAndGetReason(Component parent) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        CancelReasonDialog dlg = new CancelReasonDialog(owner);
        dlg.setVisible(true);
        return dlg.isConfirmed() ? dlg.getReason() : null;
    }
}
