package com.xettuyen2026.ui;

import com.xettuyen2026.dao.DiemThiDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog thêm/sửa nguyện vọng.
 */
public class NguyenVongDialog extends JDialog {

    private boolean saved = false;
    private NguyenVongXetTuyen nv;
    private NguyenVongDAO dao = new NguyenVongDAO();
    private NganhDAO nganhDAO = new NganhDAO();
    private NganhTohopDAO nganhTohopDAO = new NganhTohopDAO();
    private DiemThiDAO diemThiDAO = new DiemThiDAO();

    private JTextField txtCccd, txtNvTt, txtNl1;
    private JComboBox<String> cboNganh, cboPhuongthuc, cboTohop;
    private JLabel lblTohop, lblNl1;

    public NguyenVongDialog(Window owner, NguyenVongXetTuyen nv) {
        super(owner, nv == null ? "Thêm nguyện vọng" : "Sửa nguyện vọng",
                ModalityType.APPLICATION_MODAL);
        this.nv = nv;
        setSize(500, 420);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (nv != null)
            populateData();
        else
            updateMethodFields(); // Initialize state if new
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
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(createLabel("CCCD *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtCccd = createField();
        txtCccd.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                loadDiemDGNL();
            }
        });
        form.add(txtCccd, gbc);
        row++;

        // Ngành
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(createLabel("Ngành *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
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
        cboNganh.addActionListener(e -> updateTohopList());
        form.add(cboNganh, gbc);
        row++;

        // Thứ tự NV
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(createLabel("Thứ tự NV *"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtNvTt = createField();
        txtNvTt.setText("1");
        form.add(txtNvTt, gbc);
        row++;

        // Phương thức
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(createLabel("Phương thức"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        cboPhuongthuc = new JComboBox<>(new String[] { "PT2", "PT3", "PT4" });
        cboPhuongthuc.setFont(UIConstants.FONT_REGULAR);
        cboPhuongthuc.setPreferredSize(new Dimension(0, 34));
        cboPhuongthuc.addActionListener(e -> updateMethodFields());
        form.add(cboPhuongthuc, gbc);
        row++;

        // Tổ hợp xét tuyển
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        lblTohop = createLabel("Tổ hợp XT");
        form.add(lblTohop, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        cboTohop = new JComboBox<>();
        cboTohop.setFont(UIConstants.FONT_REGULAR);
        cboTohop.setPreferredSize(new Dimension(0, 34));
        form.add(cboTohop, gbc);
        row++;

        // Điểm ĐGNL
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        lblNl1 = createLabel("Điểm ĐGNL");
        form.add(lblNl1, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtNl1 = createField();
        txtNl1.setEditable(false);
        txtNl1.setBackground(new Color(245, 245, 245));
        form.add(txtNl1, gbc);
        row++;

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

        // Initial setup
        updateTohopList();
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

    private void updateTohopList() {
        cboTohop.removeAllItems();
        String nganhItem = (String) cboNganh.getSelectedItem();
        if (nganhItem != null && !nganhItem.startsWith("(Không")) {
            String maNganh = nganhItem.split(" - ")[0].trim();
            List<NganhTohop> tohopList = nganhTohopDAO.findByMaNganh(maNganh);
            if (tohopList != null) {
                for (NganhTohop nt : tohopList) {
                    cboTohop.addItem(nt.getMatohop());
                }
            }
        }
    }

    private void updateMethodFields() {
        String pt = (String) cboPhuongthuc.getSelectedItem();
        if (pt == null)
            return;
        if (pt.startsWith("PT4") || pt.startsWith("DGNL")) {
            lblTohop.setVisible(false);
            cboTohop.setVisible(false);
            lblNl1.setVisible(true);
            txtNl1.setVisible(true);
            loadDiemDGNL();
        } else {
            lblTohop.setVisible(true);
            cboTohop.setVisible(true);
            lblNl1.setVisible(false);
            txtNl1.setVisible(false);
        }
    }

    private void loadDiemDGNL() {
        if (!txtNl1.isVisible())
            return;
        String cccd = txtCccd.getText().trim();
        if (!cccd.isEmpty()) {
            DiemThiXetTuyen diem = diemThiDAO.findByCccdAndPhuongThuc(cccd, "PT4");
            if (diem == null)
                diem = diemThiDAO.findByCccdAndPhuongThuc(cccd, "4");
            if (diem != null && diem.getNl1() != null) {
                txtNl1.setText(diem.getNl1().toString());
            } else {
                txtNl1.setText("Chưa có điểm");
            }
        }
    }

    private void populateData() {
        if (nv == null)
            return;
        txtCccd.setText(nv.getNnCccd());
        txtCccd.setEnabled(false);
        txtNvTt.setText(nv.getNvTt() != null ? nv.getNvTt().toString() : "");

        // Select ngành
        String ma = nv.getNvManganh();
        for (int i = 0; i < cboNganh.getItemCount(); i++) {
            if (cboNganh.getItemAt(i).startsWith(ma)) {
                cboNganh.setSelectedIndex(i);
                break;
            }
        }

        // update tohop before setting it
        updateTohopList();

        if (nv.getTtPhuongthuc() != null) {
            String pt = nv.getTtPhuongthuc();
            if (pt.equalsIgnoreCase("PT4") || pt.equalsIgnoreCase("DGNL"))
                cboPhuongthuc.setSelectedItem("PT4");
            else if (pt.equalsIgnoreCase("PT3") || pt.equalsIgnoreCase("VSAT"))
                cboPhuongthuc.setSelectedItem("PT3");
            else
                cboPhuongthuc.setSelectedItem("PT2");
        }

        updateMethodFields();

        if (nv.getTtThm() != null && cboTohop.isVisible()) {
            cboTohop.setSelectedItem(nv.getTtThm());
        }
    }

    private void doSave() {
        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nganhItem = (String) cboNganh.getSelectedItem();
        if (nganhItem == null)
            return;
        String maNganh = nganhItem.split(" - ")[0].trim();

        int nvTt;
        try {
            nvTt = Integer.parseInt(txtNvTt.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Thứ tự NV phải là số!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String phuongthuc = (String) cboPhuongthuc.getSelectedItem();

        // Validation check for PT3 and PT4 presence in DB
        if ("PT3".equals(phuongthuc)) {
            DiemThiXetTuyen diem = diemThiDAO.findByCccdAndPhuongThuc(cccd, "PT3");
            if (diem == null)
                diem = diemThiDAO.findByCccdAndPhuongThuc(cccd, "5");
            if (diem == null) {
                JOptionPane.showMessageDialog(this, "Sinh viên này chưa có điểm thi V-SAT (Phương thức 3)!",
                        "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if ("PT4".equals(phuongthuc)) {
            DiemThiXetTuyen diem = diemThiDAO.findByCccdAndPhuongThuc(cccd, "PT4");
            if (diem == null)
                diem = diemThiDAO.findByCccdAndPhuongThuc(cccd, "4");
            if (diem == null || diem.getNl1() == null) {
                JOptionPane.showMessageDialog(this, "Sinh viên này chưa cập nhật điểm ĐGNL (Phương thức 4)!",
                        "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String thm = cboTohop.isVisible() ? (String) cboTohop.getSelectedItem() : null;

        try {
            if (nv == null) {
                // Thêm mới
                nv = new NguyenVongXetTuyen();
                nv.setNnCccd(cccd);
                nv.setNvManganh(maNganh);
                nv.setNvTt(nvTt);
                nv.setTtPhuongthuc(phuongthuc);
                nv.setTtThm(thm);
                nv.setNvKeys(cccd + "_" + maNganh + "_" + phuongthuc);
                dao.save(nv);
            } else {
                // Sửa
                nv.setNvManganh(maNganh);
                nv.setNvTt(nvTt);
                nv.setTtPhuongthuc(phuongthuc);
                nv.setTtThm(thm);
                nv.setNvKeys(nv.getNnCccd() + "_" + maNganh + "_" + phuongthuc);
                dao.update(nv);
            }
            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
