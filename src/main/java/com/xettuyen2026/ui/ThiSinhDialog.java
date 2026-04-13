package com.xettuyen2026.ui;

import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Dialog thêm/sửa thí sinh - form 2 cột.
 */
public class ThiSinhDialog extends JDialog {

    private boolean saved = false;
    private ThiSinh thiSinh;

    private JTextField txtCccd;
    private JTextField txtSbd;
    private JTextField txtHo;
    private JTextField txtTen;
    private JTextField txtNgaySinh;
    private JTextField txtDienThoai;
    private JTextField txtEmail;
    private JTextField txtNoiSinh;
    private JComboBox<String> cboGioiTinh;
    private JComboBox<String> cboKhuVuc;
    private JComboBox<String> cboDoiTuong;

    public ThiSinhDialog(Window owner, ThiSinh thiSinh) {
        super(owner, thiSinh == null ? "Thêm mới thí sinh" : "Sửa thông tin thí sinh",
                ModalityType.APPLICATION_MODAL);
        this.thiSinh = thiSinh;
        setSize(600, 520);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (thiSinh != null) {
            populateData();
        }
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel(thiSinh == null ? "Thêm mới thí sinh" : "Sửa thông tin thí sinh");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.PRIMARY);
        main.add(title, BorderLayout.NORTH);

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

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lblGt = new JLabel("Giới tính");
        lblGt.setFont(UIConstants.FONT_BOLD);
        form.add(lblGt, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cboGioiTinh.setFont(UIConstants.FONT_REGULAR);
        cboGioiTinh.setPreferredSize(new Dimension(0, 34));
        form.add(cboGioiTinh, gbc);
        row++;

        txtDienThoai = addField(form, gbc, row++, "Điện thoại");
        txtEmail = addField(form, gbc, row++, "Email");
        txtNoiSinh = addField(form, gbc, row++, "Nơi sinh");

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lblKv = new JLabel("Khu vực");
        lblKv.setFont(UIConstants.FONT_BOLD);
        form.add(lblKv, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        cboKhuVuc = new JComboBox<>(new String[]{"KV1", "KV2NT", "KV2", "KV3"});
        cboKhuVuc.setFont(UIConstants.FONT_REGULAR);
        cboKhuVuc.setPreferredSize(new Dimension(0, 34));
        form.add(cboKhuVuc, gbc);
        row++;

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel lblDt = new JLabel("Đối tượng");
        lblDt.setFont(UIConstants.FONT_BOLD);
        form.add(lblDt, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        cboDoiTuong = new JComboBox<>(new String[]{"00", "01", "02", "03", "04", "05", "06", "07"});
        cboDoiTuong.setFont(UIConstants.FONT_REGULAR);
        cboDoiTuong.setPreferredSize(new Dimension(0, 34));
        form.add(cboDoiTuong, gbc);

        main.add(new JScrollPane(form), BorderLayout.CENTER);

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
        txtCccd.setText(thiSinh.getCccd());
        txtCccd.setEnabled(false);
        txtSbd.setText(thiSinh.getSobaodanh());
        txtHo.setText(thiSinh.getHo());
        txtTen.setText(thiSinh.getTen());
        txtNgaySinh.setText(thiSinh.getNgaySinh());
        cboGioiTinh.setSelectedItem(thiSinh.getGioiTinh());
        txtDienThoai.setText(thiSinh.getDienThoai());
        txtEmail.setText(thiSinh.getEmail());
        txtNoiSinh.setText(thiSinh.getNoiSinh());
        if (thiSinh.getKhuVuc() != null) {
            cboKhuVuc.setSelectedItem(thiSinh.getKhuVuc());
        }
        if (thiSinh.getDoiTuong() != null) {
            cboDoiTuong.setSelectedItem(thiSinh.getDoiTuong());
        }
    }

    private void doSave() {
        String cccd = txtCccd.getText().trim();
        String ho = txtHo.getText().trim();
        String ten = txtTen.getText().trim();
        if (cccd.isEmpty() || ho.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD, Họ, Tên!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ThiSinh result = (thiSinh != null) ? thiSinh : new ThiSinh();
        result.setCccd(cccd);
        result.setSobaodanh(txtSbd.getText().trim());
        result.setHo(ho);
        result.setTen(ten);
        result.setNgaySinh(txtNgaySinh.getText().trim());
        result.setGioiTinh((String) cboGioiTinh.getSelectedItem());
        result.setDienThoai(txtDienThoai.getText().trim());
        result.setEmail(txtEmail.getText().trim());
        result.setNoiSinh(txtNoiSinh.getText().trim());
        result.setKhuVuc((String) cboKhuVuc.getSelectedItem());
        result.setDoiTuong((String) cboDoiTuong.getSelectedItem());

        this.thiSinh = result;
        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public ThiSinh getEntity() {
        return thiSinh;
    }
}
