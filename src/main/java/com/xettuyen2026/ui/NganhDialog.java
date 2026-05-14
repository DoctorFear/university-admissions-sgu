package com.xettuyen2026.ui;

import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.ui.common.RoundedButton;
import com.xettuyen2026.ui.common.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class NganhDialog extends JDialog {

    private boolean saved = false;
    private Nganh entity;

    // Thong tin co ban
    private JTextField txtMaNganh;
    private JTextField txtTenNganh;
    private JTextField txtTohopGoc;
    private JTextField txtChiTieu;
    private JTextField txtDiemSan;
    private JTextField txtDiemTrungTuyen;
    private JCheckBox cbTuyenThang;
    private JCheckBox cbDgnl;
    private JCheckBox cbThpt;
    private JCheckBox cbVsat;
    private java.util.List<String> validTohop;

    public NganhDialog(Window owner, Nganh existing, java.util.List<String> validTohop) {
        super(owner,
              existing == null ? "Thêm mới ngành tuyển sinh" : "Sửa thông tin ngành",
              ModalityType.APPLICATION_MODAL);
        this.entity = existing;
        this.validTohop = validTohop;
        setSize(560, 560);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
        if (existing != null) populateData();
    }

    // Giao Dien
    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        // Title
        JLabel title = new JLabel(entity == null ? "Thêm mới ngành" : "Sửa ngành");
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
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Ma nganh — khong cho sua khi edit (unique key)
        txtMaNganh = addTextField(form, gbc, row++, "Mã ngành *");
        if (entity != null) txtMaNganh.setEnabled(false);

        // Ten nganh
        txtTenNganh = addTextField(form, gbc, row++, "Tên ngành *");

        // To hop goc
        txtTohopGoc = addTextField(form, gbc, row++, "Tổ hợp gốc");

        // Chi tieu
        txtChiTieu = addTextField(form, gbc, row++, "Chỉ tiêu *");

        // Điem san
        txtDiemSan = addTextField(form, gbc, row++, "Điểm sàn");

        // Điem trung tuyen
        txtDiemTrungTuyen = addTextField(form, gbc, row++, "Điểm trúng tuyển");
        txtDiemTrungTuyen.setEnabled(false);

        // Chọn phương thức xét tuyển của ngành
        addSectionLabel(form, gbc, row++, "Phương thức xét tuyển");
        JPanel methodPanel = new JPanel(new GridLayout(2, 2, 8, 6));
        methodPanel.setOpaque(false);
        cbTuyenThang = createMethodCheckBox("Tuyển thẳng");
        cbDgnl = createMethodCheckBox("ĐGNL");
        cbThpt = createMethodCheckBox("THPT");
        cbVsat = createMethodCheckBox("V-SAT");
        methodPanel.add(cbTuyenThang);
        methodPanel.add(cbDgnl);
        methodPanel.add(cbThpt);
        methodPanel.add(cbVsat);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        form.add(methodPanel, gbc);
        gbc.gridwidth = 1;
        return form;
    }


    // Them label + textfield vao form, tra ve JTextField.
    private JTextField addTextField(JPanel form, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setPreferredSize(new Dimension(150, 30));
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JTextField field = new JTextField();
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(0, 34));
        form.add(field, gbc);
        return field;
    }

    private void addSectionLabel(JPanel form, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(UIConstants.PRIMARY);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT));
        form.add(lbl, gbc);
        gbc.gridwidth = 1;
    }

    // Tạo checkbox chọn phương thức xét tuyển
    private JCheckBox createMethodCheckBox(String text) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setOpaque(false);
        checkbox.setFont(UIConstants.FONT_REGULAR);
        checkbox.setSelected(true);
        return checkbox;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Hủy", new Color(0x757575));
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnSave = new RoundedButton("Lưu", UIConstants.PRIMARY);
        btnSave.addActionListener(e -> doSave());

        panel.add(btnCancel);
        panel.add(btnSave);
        return panel;
    }

    // DATA
    /** Dien du lieu vao form khi o che do sua. */
    private void populateData() {
        txtMaNganh.setText(entity.getManganh());
        txtTenNganh.setText(entity.getTennganh());
        txtTohopGoc.setText(nvlStr(entity.getnTohopgoc()));
        txtChiTieu.setText(entity.getnChitieu() != null ? String.valueOf(entity.getnChitieu()) : "");
        txtDiemSan.setText(entity.getnDiemsan() != null ? entity.getnDiemsan().toPlainString() : "");
        txtDiemTrungTuyen.setText(entity.getnDiemtrungtuyen() != null
                ? entity.getnDiemtrungtuyen().toPlainString() : "");
        cbTuyenThang.setSelected(isMethodEnabled(entity.getnTuyenthang()));
        cbDgnl.setSelected(isMethodEnabled(entity.getnDgnl()));
        cbThpt.setSelected(isMethodEnabled(entity.getnThpt()));
        cbVsat.setSelected(isMethodEnabled(entity.getnVsat()));
        lockMethodIfHasNguyenVong(cbTuyenThang, countInt(entity.getSlXtt()));
        lockMethodIfHasNguyenVong(cbDgnl, countInt(entity.getSlDgnl()));
        lockMethodIfHasNguyenVong(cbThpt, countInt(entity.getSlThpt()));
        lockMethodIfHasNguyenVong(cbVsat, countInt(entity.getSlVsat()));
    }

    /** Validate va luu entity tu form. */
    private void doSave() {
        String maNganh    = txtMaNganh.getText().trim().toUpperCase();
        String tenNganh   = txtTenNganh.getText().trim();
        String tohopGoc   = txtTohopGoc.getText().trim();
        String chiTieuStr = txtChiTieu.getText().trim();

        // Validate bat buoc
        if (maNganh.isEmpty()) {
            warn("Mã ngành không được để trống!");
            txtMaNganh.requestFocus();
            return;
        }
        if (tenNganh.isEmpty()) {
            warn("Tên ngành không được để trống!");
            txtTenNganh.requestFocus();
            return;
        }

        // Parse chi tieu
        Integer chiTieu = null;
        if (!chiTieuStr.isEmpty()) {
        	try {
        	    chiTieu = Integer.parseInt(chiTieuStr);
        	    if (chiTieu <= 0) {
        	        warn("Chỉ tiêu phải là số nguyên dương!");
        	        txtChiTieu.requestFocus();
        	        return;
        	    }
        	    if (chiTieu > 100000) {
        	        warn("Chỉ tiêu không được vượt quá 100.000!");
        	        txtChiTieu.requestFocus();
        	        return;
        	    }
        	} catch (NumberFormatException ex) {
        	    warn("Chỉ tiêu phải là số nguyên dương (không nhập chữ hoặc ký tự đặc biệt)!");
        	    txtChiTieu.requestFocus();
        	    return;
        	}
        } else {
            warn("Chỉ tiêu không được để trống!");
            txtChiTieu.requestFocus();
            return;
        }

        // Parse diem san
        BigDecimal diemSan = null;
        String diemSanStr = txtDiemSan.getText().trim();
        if (!diemSanStr.isEmpty()) {
            try { diemSan = new BigDecimal(diemSanStr); }
            catch (NumberFormatException ex) {
                warn("Điểm sàn không hợp lệ!");
                txtDiemSan.requestFocus();
                return;
            }
        }

        // Parse diem trung tuyen
        BigDecimal diemTrungTuyen = null;
        String diemTTStr = txtDiemTrungTuyen.getText().trim();
        if (!diemTTStr.isEmpty()) {
            try { diemTrungTuyen = new BigDecimal(diemTTStr); }
            catch (NumberFormatException ex) {
                warn("Điểm trúng tuyển không hợp lệ!");
                txtDiemTrungTuyen.requestFocus();
                return;
            }
        }

        // Validate To hop goc
        if (!tohopGoc.isEmpty()) {
            String[] tohops = tohopGoc.split("[,;\\s]+");
            java.util.List<String> invalid = new java.util.ArrayList<>();
            for (String th : tohops) {
                String thTrim = th.trim().toUpperCase();
                if (!thTrim.isEmpty() && !validTohop.contains(thTrim)) {
                    invalid.add(thTrim);
                }
            }
            if (!invalid.isEmpty()) {
                warn("Tổ hợp không hợp lệ: " + String.join(", ", invalid)
                   + "\nVui lòng chỉ nhập các tổ hợp đã được quản lý!");
                txtTohopGoc.requestFocus();
                return;
            }
        }

        if (!cbTuyenThang.isSelected() && !cbDgnl.isSelected()
                && !cbThpt.isSelected() && !cbVsat.isSelected()) {
            warn("Vui lòng chọn ít nhất một phương thức xét tuyển!");
            return;
        }

        if (entity != null && !canChangeMethods()) {
            return;
        }

        // Build entity
        Nganh result = (entity != null) ? entity : new Nganh();
        result.setManganh(maNganh);
        result.setTennganh(tenNganh);
        result.setnTohopgoc(tohopGoc.isEmpty() ? null : tohopGoc);
        result.setnChitieu(chiTieu);
        result.setnDiemsan(diemSan);
        result.setnDiemtrungtuyen(diemTrungTuyen);
        result.setnTuyenthang(methodFlag(cbTuyenThang.isSelected()));
        result.setnDgnl(methodFlag(cbDgnl.isSelected()));
        result.setnThpt(methodFlag(cbThpt.isSelected()));
        result.setnVsat(methodFlag(cbVsat.isSelected()));

        // Neu them moi, cac truong sl de null (chua co gia tri)
        if (entity == null) {
            result.setSlThpt(null);
            result.setSlDgnl(null);
            result.setSlVsat(null);
            result.setSlXtt(null);
        }
        
        this.entity = result;
        this.saved  = true;
        dispose();
    }

    private String nvlStr(String s) { return s != null ? s : ""; }

    // Kiểm tra phương thức còn được xét tuyển hay không
    private boolean isMethodEnabled(String value) {
        return value == null || !"-".equals(value.trim());
    }

    // Chuyển trạng thái checkbox thành cờ lưu trong database
    private String methodFlag(boolean selected) {
        return selected ? "1" : "-";
    }

    // Khóa phương thức đã có nguyện vọng đăng ký
    private void lockMethodIfHasNguyenVong(JCheckBox checkbox, int count) {
        if (count > 0) {
            checkbox.setSelected(true);
            checkbox.setEnabled(false);
        }
    }

    // Kiểm tra không bỏ phương thức đã có nguyện vọng
    private boolean canChangeMethods() {
        if (!cbTuyenThang.isSelected() && countInt(entity.getSlXtt()) > 0) {
            warn("Không thể bỏ phương thức Tuyển thẳng vì đã có nguyện vọng đăng ký!");
            return false;
        }
        if (!cbDgnl.isSelected() && countInt(entity.getSlDgnl()) > 0) {
            warn("Không thể bỏ phương thức ĐGNL vì đã có nguyện vọng đăng ký!");
            return false;
        }
        if (!cbThpt.isSelected() && countInt(entity.getSlThpt()) > 0) {
            warn("Không thể bỏ phương thức THPT vì đã có nguyện vọng đăng ký!");
            return false;
        }
        if (!cbVsat.isSelected() && countInt(entity.getSlVsat()) > 0) {
            warn("Không thể bỏ phương thức V-SAT vì đã có nguyện vọng đăng ký!");
            return false;
        }
        return true;
    }

    // Chuyển số lượng nguyện vọng sang số nguyên để kiểm tra
    private int countInt(Integer value) {
        return value != null ? value : 0;
    }

    // Chuyển số lượng nguyện vọng dạng chuỗi sang số nguyên để kiểm tra
    private int countInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Loi nhap lieu", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() { return saved; }
    public Nganh getEntity() { return entity; }
}
