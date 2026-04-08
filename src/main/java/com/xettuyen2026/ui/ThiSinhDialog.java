package com.xettuyen2026.ui;

import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog thêm/sửa thí sinh - form 2 cột.
 */
public class ThiSinhDialog extends JDialog {

    private boolean saved = false;
    private ThiSinh thiSinh;

    private JTextField txtCccd, txtSbd, txtHo, txtTen, txtNgaySinh;
    private JTextField txtDienThoai, txtEmail, txtNoiSinh;
    private JComboBox<String> cboGioiTinh, cboKhuVuc, cboDoiTuong;

    public ThiSinhDialog(Window owner, ThiSinh thiSinh) {
        super(owner, thiSinh == null ? "Thêm mới thí sinh" : "Sửa thông tin thí sinh",
                ModalityType.APPLICATION_MODAL);
        this.thiSinh = thiSinh;
        setSize(600, 520);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (thiSinh != null) populateData();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        // Title
        JLabel title = new JLabel(thiSinh == null ? "➕ Thêm mới thí sinh" : "✏️ Sửa thông tin thí sinh");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        txtCccd = addField(form, gbc, row++, "CCCD *");
        txtSbd = addField(form, gbc, row++, "Số báo danh");
        txtHo = addField(form, gbc, row++, "Họ *");
        txtTen = addField(form, gbc, row++, "Tên *");
        txtNgaySinh = addField(form, gbc, row++, "Ngày sinh (dd/MM/yyyy)");

        // Gender combobox
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        JLabel lblGt = new JLabel("Giới tính");
        lblGt.setFont(UIConstants.FONT_BOLD);
        form.add(lblGt, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cboGioiTinh.setFont(UIConstants.FONT_REGULAR);
        cboGioiTinh.setPreferredSize(new Dimension(0, 34));
        form.add(cboGioiTinh, gbc);
        row++;

        txtDienThoai = addField(form, gbc, row++, "Điện thoại");
        txtEmail = addField(form, gbc, row++, "Email");
        txtNoiSinh = addField(form, gbc, row++, "Nơi sinh");

        // Khu vuc
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        JLabel lblKv = new JLabel("Khu vực");
        lblKv.setFont(UIConstants.FONT_BOLD);
        form.add(lblKv, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboKhuVuc = new JComboBox<>(new String[]{"KV1", "KV2NT", "KV2", "KV3"});
        cboKhuVuc.setFont(UIConstants.FONT_REGULAR);
        cboKhuVuc.setPreferredSize(new Dimension(0, 34));
        form.add(cboKhuVuc, gbc);
        row++;

        // Doi tuong
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0;
        JLabel lblDt = new JLabel("Đối tượng");
        lblDt.setFont(UIConstants.FONT_BOLD);
        form.add(lblDt, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboDoiTuong = new JComboBox<>(new String[]{"00", "01", "02", "03", "04", "05", "06", "07"});
        cboDoiTuong.setFont(UIConstants.FONT_REGULAR);
        cboDoiTuong.setPreferredSize(new Dimension(0, 34));
        form.add(cboDoiTuong, gbc);

        main.add(new JScrollPane(form), BorderLayout.CENTER);

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
        lbl.setPreferredSize(new Dimension(180, 30));
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
        if (thiSinh == null) return;
        txtCccd.setText(thiSinh.getCccd());
        txtCccd.setEnabled(false); // PK - don't edit
        txtSbd.setText(thiSinh.getSobaodanh());
        txtHo.setText(thiSinh.getHo());
        txtTen.setText(thiSinh.getTen());
        txtNgaySinh.setText(thiSinh.getNgaySinh());
        cboGioiTinh.setSelectedItem(thiSinh.getGioiTinh());
        txtDienThoai.setText(thiSinh.getDienThoai());
        txtEmail.setText(thiSinh.getEmail());
        txtNoiSinh.setText(thiSinh.getNoiSinh());
        if (thiSinh.getKhuVuc() != null) cboKhuVuc.setSelectedItem(thiSinh.getKhuVuc());
        if (thiSinh.getDoiTuong() != null) cboDoiTuong.setSelectedItem(thiSinh.getDoiTuong());
    }

    private void doSave() {
        String cccd = txtCccd.getText().trim();
        String ho   = txtHo.getText().trim();
        String ten  = txtTen.getText().trim();
        if (cccd.isEmpty() || ho.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD, Họ, Tên!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            ThiSinhDAO dao = new ThiSinhDAO();
            boolean isNew = (thiSinh == null);
            if (isNew) thiSinh = new ThiSinh();

            thiSinh.setCccd(cccd);
            thiSinh.setHo(ho);
            thiSinh.setTen(ten);
            thiSinh.setSobaodanh(txtSbd.getText().trim().isEmpty() ? null : txtSbd.getText().trim());
            thiSinh.setNgaySinh(txtNgaySinh.getText().trim().isEmpty() ? null : txtNgaySinh.getText().trim());
            thiSinh.setGioiTinh((String) cboGioiTinh.getSelectedItem());
            thiSinh.setDienThoai(txtDienThoai.getText().trim().isEmpty() ? null : txtDienThoai.getText().trim());
            thiSinh.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
            thiSinh.setNoiSinh(txtNoiSinh.getText().trim().isEmpty() ? null : txtNoiSinh.getText().trim());
            thiSinh.setKhuVuc((String) cboKhuVuc.getSelectedItem());
            thiSinh.setDoiTuong((String) cboDoiTuong.getSelectedItem());
            thiSinh.setUpdatedAt(java.time.LocalDate.now());

            if (isNew) dao.save(thiSinh);
            else       dao.update(thiSinh);

            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu thí sinh:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
