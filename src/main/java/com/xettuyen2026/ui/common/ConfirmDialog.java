package com.xettuyen2026.ui.common;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog xác nhận xóa với icon cảnh báo, button [Xóa] đỏ + [Hủy].
 */
public class ConfirmDialog extends JDialog {

    private boolean confirmed = false;

    public ConfirmDialog(Window owner, String message) {
        super(owner, "Xác nhận", ModalityType.APPLICATION_MODAL);
        setSize(400, 200);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI(message);
    }

    private void initUI(String message) {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        main.setBackground(Color.WHITE);

        // Icon + Message
        JPanel msgPanel = new JPanel(new BorderLayout(12, 0));
        msgPanel.setOpaque(false);

        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        msgPanel.add(iconLabel, BorderLayout.WEST);

        JLabel msgLabel = new JLabel("<html><body style='width:250px'>" + message + "</body></html>");
        msgLabel.setFont(UIConstants.FONT_REGULAR);
        msgLabel.setForeground(UIConstants.TEXT_PRIMARY);
        msgPanel.add(msgLabel, BorderLayout.CENTER);

        main.add(msgPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnDelete = new RoundedButton(UIConstants.ICON_DELETE + " Xóa", UIConstants.DANGER);
        btnDelete.addActionListener(e -> { confirmed = true; dispose(); });

        btnPanel.add(btnCancel);
        btnPanel.add(btnDelete);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    public boolean isConfirmed() { return confirmed; }

    /**
     * Tiện ích: hiển thị confirm dialog và trả về true nếu user xác nhận.
     */
    public static boolean show(Component parent, String message) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        ConfirmDialog dlg = new ConfirmDialog(owner, message);
        dlg.setVisible(true);
        return dlg.isConfirmed();
    }
}
