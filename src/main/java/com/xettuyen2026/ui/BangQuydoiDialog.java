package com.xettuyen2026.ui;

import com.xettuyen2026.entity.BangQuydoi;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;
import com.xettuyen2026.ui.common.MessageHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class BangQuydoiDialog extends JDialog {

    private boolean saved = false;
    private BangQuydoi entity;

    // Form fields
    private JTextField txtPhuongThuc;
    private JTextField txtTohop;
    private JTextField txtMon;
    private JTextField txtDiemA;
    private JTextField txtDiemB;
    private JTextField txtDiemC;
    private JTextField txtDiemD;
    private JTextField txtMaQuydoi;
    private JTextField txtPhanvi;

    public BangQuydoiDialog(Window owner, BangQuydoi existing) {
        super(owner,
              existing == null ? "Thêm mới quy đổi" : "Sửa thông tin quy đổi",
              ModalityType.APPLICATION_MODAL);

        this.entity = existing;
        setSize(540, 520);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (existing != null) populateData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        // Title
        JLabel title = new JLabel(entity == null ? "➕ Thêm mới quy đổi" : "✏️ Sửa quy đổi");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Form
        main.add(createForm(), BorderLayout.CENTER);

        // Buttons
        main.add(createButtonPanel(), BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Phương thức
        txtPhuongThuc = addTextField(form, gbc, row++, "Phương thức *");
        if (entity != null) txtPhuongThuc.setEnabled(false);

        // Tổ hợp (dùng cho DGNL)
        txtTohop = addTextField(form, gbc, row++, "Tổ hợp");

        // Môn thi (dùng cho V-SAT)
        txtMon = addTextField(form, gbc, row++, "Môn thi");

        // Mã quy đổi
        txtMaQuydoi = addTextField(form, gbc, row++, "Mã quy đổi *");
        if (entity != null) txtMaQuydoi.setEnabled(false);

        // Khoảng điểm
        txtDiemA = addTextField(form, gbc, row++, "Điểm a (thấp) *");
        txtDiemB = addTextField(form, gbc, row++, "Điểm b (cao) *");
        txtDiemC = addTextField(form, gbc, row++, "Điểm c THPT (thấp) *");
        txtDiemD = addTextField(form, gbc, row++, "Điểm d THPT (cao) *");

        txtPhanvi = addTextField(form, gbc, row++, "Phân vị / Khoảng");

        return form;
    }

    private JTextField addTextField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(160, 30));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, 36));
        form.add(field, gbc);
        return field;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        panel.add(btnCancel);
        panel.add(btnSave);
        return panel;
    }

    private void populateData() {
        txtPhuongThuc.setText(entity.getdPhuongthuc());
        txtTohop.setText(entity.getdTohop() != null ? entity.getdTohop() : "");
        txtMon.setText(entity.getdMon() != null ? entity.getdMon() : "");
        txtMaQuydoi.setText(entity.getdMaquydoi());
        txtDiemA.setText(entity.getdDiema() != null ? entity.getdDiema().toPlainString() : "");
        txtDiemB.setText(entity.getdDiemb() != null ? entity.getdDiemb().toPlainString() : "");
        txtDiemC.setText(entity.getdDiemc() != null ? entity.getdDiemc().toPlainString() : "");
        txtDiemD.setText(entity.getdDiemd() != null ? entity.getdDiemd().toPlainString() : "");
        txtPhanvi.setText(entity.getdPhanvi() != null ? entity.getdPhanvi() : "");
    }

    private void doSave() {
        try {
            String phuongThuc = txtPhuongThuc.getText().trim().toUpperCase();
            String tohop = txtTohop.getText().trim();
            String mon = txtMon.getText().trim();
            String maQuydoi = txtMaQuydoi.getText().trim().toUpperCase();

            if (phuongThuc.isEmpty()) {
                throw new RuntimeException("Phương thức không được để trống!");
            }
            if (maQuydoi.isEmpty()) {
                throw new RuntimeException("Mã quy đổi không được để trống!");
            }

            BangQuydoi result = (entity != null) ? entity : new BangQuydoi();

            result.setdPhuongthuc(phuongThuc);
            result.setdTohop(tohop.isEmpty() ? null : tohop);
            result.setdMon(mon.isEmpty() ? null : mon);
            result.setdMaquydoi(maQuydoi);
            result.setdPhanvi(txtPhanvi.getText().trim());

            // Parse điểm
            result.setdDiema(parseBigDecimal(txtDiemA, "Điểm a"));
            result.setdDiemb(parseBigDecimal(txtDiemB, "Điểm b"));
            result.setdDiemc(parseBigDecimal(txtDiemC, "Điểm c"));
            result.setdDiemd(parseBigDecimal(txtDiemD, "Điểm d"));

            this.entity = result;
            this.saved = true;
            dispose();

        } catch (Exception ex) {
            MessageHelper.showWarning(this, ex.getMessage());
        }
    }

    private BigDecimal parseBigDecimal(JTextField field, String fieldName) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new RuntimeException(fieldName + " không được để trống!");
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new RuntimeException(fieldName + " phải là số hợp lệ (ví dụ: 132.5)!");
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public BangQuydoi getEntity() {
        return entity;
    }
}