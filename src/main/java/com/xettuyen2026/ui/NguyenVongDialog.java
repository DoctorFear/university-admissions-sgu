package com.xettuyen2026.ui;

import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Dialog thêm/sửa nguyện vọng.
 */
public class NguyenVongDialog extends JDialog {

    private boolean saved = false;
    private NguyenVongXetTuyen nv;
    private NguyenVongDAO dao = new NguyenVongDAO();
    private NganhDAO nganhDAO = new NganhDAO();

    private JTextField txtCccd, txtNvTt;
    private JComboBox<String> cboNganh, cboPhuongthuc;

    public NguyenVongDialog(Window owner, NguyenVongXetTuyen nv) {
        super(owner, nv == null ? "Thêm nguyện vọng" : "Sửa nguyện vọng",
                ModalityType.APPLICATION_MODAL);
        this.nv = nv;
        setSize(500, 350);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (nv != null) populateData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel(nv == null ? "➕ Thêm nguyện vọng" : "✏️ Sửa nguyện vọng");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // CCCD
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("CCCD *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtCccd = createField();
        form.add(txtCccd, gbc);
        row++;

        // Ngành
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Ngành *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboNganh = new JComboBox<>();
        cboNganh.setFont(UIConstants.FONT_REGULAR);
        cboNganh.setPreferredSize(new Dimension(0, 34));
        try {
            List<Nganh> list = nganhDAO.findAll();
            for (Nganh n : list) {
                cboNganh.addItem(n.getManganh() + " - " + n.getTennganh());
            }
        } catch (Exception e) {
            cboNganh.addItem("(Không tải được)");
        }
        form.add(cboNganh, gbc);
        row++;

        // Thứ tự NV
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Thứ tự NV *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNvTt = createField();
        txtNvTt.setText("1");
        form.add(txtNvTt, gbc);
        row++;

        // Phương thức
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        form.add(createLabel("Phương thức"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboPhuongthuc = new JComboBox<>(new String[]{"PT2", "PT4", "DGNL", "VSAT"});
        cboPhuongthuc.setFont(UIConstants.FONT_REGULAR);
        cboPhuongthuc.setPreferredSize(new Dimension(0, 34));
        form.add(cboPhuongthuc, gbc);

        main.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());
        RoundedButton btnSave = new RoundedButton("💾 Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        main.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(120, 30));
        return lbl;
    }

    private JTextField createField() {
        JTextField f = new JTextField();
        f.setFont(UIConstants.FONT_REGULAR);
        f.setPreferredSize(new Dimension(0, 34));
        return f;
    }

    private void populateData() {
        if (nv == null) return;
        txtCccd.setText(nv.getNnCccd());
        txtCccd.setEnabled(false);
        txtNvTt.setText(nv.getNvTt() != null ? nv.getNvTt().toString() : "");
        if (nv.getTtPhuongthuc() != null) cboPhuongthuc.setSelectedItem(nv.getTtPhuongthuc());

        // Select ngành
        String ma = nv.getNvManganh();
        for (int i = 0; i < cboNganh.getItemCount(); i++) {
            if (cboNganh.getItemAt(i).startsWith(ma)) {
                cboNganh.setSelectedIndex(i);
                break;
            }
        }
    }

    private void doSave() {
        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nganhItem = (String) cboNganh.getSelectedItem();
        if (nganhItem == null) return;
        String maNganh = nganhItem.split(" - ")[0].trim();

        int nvTt;
        try { nvTt = Integer.parseInt(txtNvTt.getText().trim()); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Thứ tự NV phải là số!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String phuongthuc = (String) cboPhuongthuc.getSelectedItem();

        try {
            if (nv == null) {
                // Thêm mới
                nv = new NguyenVongXetTuyen();
                nv.setNnCccd(cccd);
                nv.setNvManganh(maNganh);
                nv.setNvTt(nvTt);
                nv.setTtPhuongthuc(phuongthuc);
                nv.setNvKeys(cccd + "_" + maNganh + "_" + phuongthuc);
                dao.save(nv);
            } else {
                // Sửa
                nv.setNvManganh(maNganh);
                nv.setNvTt(nvTt);
                nv.setTtPhuongthuc(phuongthuc);
                nv.setNvKeys(nv.getNnCccd() + "_" + maNganh + "_" + phuongthuc);
                dao.update(nv);
            }
            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
