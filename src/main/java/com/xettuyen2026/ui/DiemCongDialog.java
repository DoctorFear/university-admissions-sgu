package com.xettuyen2026.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.xettuyen2026.entity.DiemCongXetTuyen;
import com.xettuyen2026.ui.common.MessageHelper;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

public class DiemCongDialog extends JDialog {
private boolean saved = false;
    private DiemCongXetTuyen entity;

    private JTextField txtCccd, txtManganh, txtMatohop, txtPhuongthuc;
    private JTextField txtDiemCC, txtDiemUtxt, txtGhichu;

    public DiemCongDialog(Window owner, DiemCongXetTuyen existing) {
        super(owner, existing == null ? "Thêm điểm cộng" : "Sửa điểm cộng", ModalityType.APPLICATION_MODAL);
        this.entity = existing;

        setSize(420, 500);
        setLocationRelativeTo(owner);
        setResizable(false);

        initUI();
        if (existing != null) populateData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        main.setBackground(Color.WHITE);

        JLabel title = new JLabel(entity == null ? "Thêm điểm cộng" : "Sửa điểm cộng");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        
        txtCccd = addField(form, gbc, row++, "CCCD *");
        txtManganh = addField(form, gbc, row++, "Mã ngành");
        txtMatohop = addField(form, gbc, row++, "Mã tổ hợp");
        txtPhuongthuc = addField(form, gbc, row++, "Phương thức");
        txtDiemCC = addField(form, gbc, row++, "Điểm CC");
        txtDiemUtxt = addField(form, gbc, row++, "Điểm ưu tiên");
        txtGhichu = addField(form, gbc, row++, "Ghi chú");
        gbc.gridy = row;
        gbc.gridx = 1;

        main.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", Color.GRAY);
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private JTextField addField(JPanel form, GridBagConstraints gbc, int row, String label) {
        JTextField txt = new JTextField();
        addComponent(form, gbc, row, label, txt);
        return txt;
    }

    private void addComponent(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row;
        gbc.gridx = 0;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        comp.setPreferredSize(new Dimension(0, 30));
        form.add(comp, gbc);
    }

    private void populateData() {
        txtCccd.setText(entity.getTsCccd());
        txtManganh.setText(entity.getManganh());
        txtMatohop.setText(entity.getMatohop());
        txtPhuongthuc.setText(entity.getPhuongthuc());
        txtDiemCC.setText(entity.getDiemCC() != null ? entity.getDiemCC().toString() : "");
        txtDiemUtxt.setText(entity.getDiemUtxt() != null ? entity.getDiemUtxt().toString() : "");
        txtGhichu.setText(entity.getGhichu());
    }
    
    private void doSave() {
        if (entity == null) entity = new DiemCongXetTuyen();

        if (txtCccd.getText().isBlank()) {
            MessageHelper.showError(this, "CCCD bắt buộc");
            return;
        }

        try {
            entity.setTsCccd(txtCccd.getText().trim());
            entity.setManganh(txtManganh.getText().trim());
            entity.setMatohop(txtMatohop.getText().trim());
            entity.setPhuongthuc(txtPhuongthuc.getText().trim());

            entity.setDiemCC(parse(txtDiemCC.getText()));
            entity.setDiemUtxt(parse(txtDiemUtxt.getText()));

            entity.setGhichu(txtGhichu.getText());

            saved = true;
            dispose();

        } catch (Exception e) {
            MessageHelper.showError(this, "Dữ liệu không hợp lệ");
        }
    }

    private java.math.BigDecimal parse(String s) {
        if (s == null || s.isBlank()) return java.math.BigDecimal.ZERO;
        return new java.math.BigDecimal(s.trim());
    }

    public boolean isSaved() { return saved; }
    public DiemCongXetTuyen getEntity() { return entity; }
}
