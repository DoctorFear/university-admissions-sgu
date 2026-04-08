package com.xettuyen2026.ui;

import com.xettuyen2026.entity.TohopMonthi;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog thêm/sửa tổ hợp môn xét tuyển.
 */
public class TohopDialog extends JDialog {

    private boolean saved = false;
    private TohopMonthi entity;

    private JTextField txtMaTohop, txtMon1, txtMon2, txtMon3, txtTenTohop;

    public TohopDialog(Window owner, TohopMonthi existing) {
        super(owner, existing == null ? "Thêm mới tổ hợp" : "Sửa tổ hợp",
                ModalityType.APPLICATION_MODAL);
        this.entity = existing;
        setSize(500, 380);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (existing != null) populateData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        // Title
        JLabel title = new JLabel(entity == null ? "➕ Thêm mới tổ hợp" : "✏️ Sửa tổ hợp");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        txtMaTohop = addField(form, gbc, row++, "Mã tổ hợp *");
        txtMon1 = addField(form, gbc, row++, "Môn 1 *");
        txtMon2 = addField(form, gbc, row++, "Môn 2 *");
        txtMon3 = addField(form, gbc, row++, "Môn 3 *");
        txtTenTohop = addField(form, gbc, row++, "Tên tổ hợp");

        main.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(140, 30));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, 34));
        form.add(field, gbc);
        return field;
    }

    private void populateData() {
        if (entity == null) return;
        txtMaTohop.setText(entity.getMatohop());
        txtMaTohop.setEnabled(false); // unique key - don't edit
        txtMon1.setText(entity.getMon1());
        txtMon2.setText(entity.getMon2());
        txtMon3.setText(entity.getMon3());
        txtTenTohop.setText(entity.getTentohop());
    }

    private void doSave() {
        String maTohop = txtMaTohop.getText().trim();
        String mon1 = txtMon1.getText().trim().toUpperCase();
        String mon2 = txtMon2.getText().trim().toUpperCase();
        String mon3 = txtMon3.getText().trim().toUpperCase();
        String tenTohop = txtTenTohop.getText().trim();

        if (maTohop.isEmpty() || mon1.isEmpty() || mon2.isEmpty() || mon3.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập Mã tổ hợp, Môn 1, Môn 2, Môn 3!",
                "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TohopMonthi result = new TohopMonthi();
        result.setMatohop(maTohop);
        result.setMon1(mon1);
        result.setMon2(mon2);
        result.setMon3(mon3);
        result.setTentohop(tenTohop);

        this.entity = result;
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public TohopMonthi getEntity() { return entity; }
}
