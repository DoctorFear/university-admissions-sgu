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
 
    // Phuong thuc xet tuyen: checkbox + o sl đi kem
    private JCheckBox  chkThpt;
    private JTextField txtSlThpt;
 
    private JCheckBox  chkDgnl;
    private JTextField txtSlDgnl;
 
    private JCheckBox  chkVsat;
    private JTextField txtSlVsat;
 
    private JCheckBox  chkTuyenThang;
    private JTextField txtSlXtt;
    
    public NganhDialog(Window owner, Nganh existing) {
        super(owner,
              existing == null ? "Thêm mới ngành tuyển sinh" : "Sửa thông tin ngành",
              ModalityType.APPLICATION_MODAL);
        this.entity = existing;
        setSize(560, 580);
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
        JLabel title = new JLabel(entity == null ? "➕ Thêm mới ngành" : "✏️ Sửa ngành");
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
 
     // Phuong thuc xet tuyen
        addSectionLabel(form, gbc, row++, "Phương thức xét tuyển: ");
 
        chkThpt       = new JCheckBox("THPT");
        txtSlThpt     = new JTextField();
        addPhuongThucRow(form, gbc, row++, chkThpt, txtSlThpt);
 
        chkDgnl       = new JCheckBox("ĐGNL");
        txtSlDgnl     = new JTextField();
        addPhuongThucRow(form, gbc, row++, chkDgnl, txtSlDgnl);
 
        chkVsat       = new JCheckBox("V-SAT");
        txtSlVsat     = new JTextField();
        addPhuongThucRow(form, gbc, row++, chkVsat, txtSlVsat);
 
        chkTuyenThang = new JCheckBox("Tuyển thẳng");
        txtSlXtt      = new JTextField();
        addPhuongThucRow(form, gbc, row++, chkTuyenThang, txtSlXtt);
 
        return form;
    }
    
    // Them phuong thuc xet tuyen
    private void addPhuongThucRow(JPanel form, GridBagConstraints gbc,
            int row, JCheckBox chk, JTextField txtSl) {
    	chk.setFont(UIConstants.FONT_REGULAR);
    	chk.setOpaque(false);
    	chk.setPreferredSize(new Dimension(130, 30));

    	txtSl.setFont(UIConstants.FONT_REGULAR);
    	txtSl.setPreferredSize(new Dimension(80, 30));
    	txtSl.setEnabled(false);

    	JLabel lblSl = new JLabel("Chỉ tiêu:");
    	lblSl.setFont(UIConstants.FONT_REGULAR);
    	lblSl.setForeground(UIConstants.TEXT_SECONDARY);
    	lblSl.setEnabled(false);

    	// Tick → bat o nhap | Bo tick → tat + xoa
    	chk.addActionListener(e -> {
    		boolean on = chk.isSelected();
    		txtSl.setEnabled(on);
    		lblSl.setEnabled(on);
    		if (!on) txtSl.setText("");
    	});

    	gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
    	form.add(chk, gbc);

    	JPanel slPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    	slPanel.setOpaque(false);
    	slPanel.add(lblSl);
    	slPanel.add(txtSl);

    	gbc.gridx = 1; gbc.weightx = 1;
    	form.add(slPanel, gbc);
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
    /** Điền dữ liệu vào form khi ở chế độ sửa. */
    private void populateData() {
        txtMaNganh.setText(entity.getManganh());
        txtTenNganh.setText(entity.getTennganh());
        txtTohopGoc.setText(nvlStr(entity.getnTohopgoc()));
        txtChiTieu.setText(entity.getnChitieu() != null ? String.valueOf(entity.getnChitieu()) : "");
        txtDiemSan.setText(entity.getnDiemsan() != null ? entity.getnDiemsan().toPlainString() : "");
        txtDiemTrungTuyen.setText(entity.getnDiemtrungtuyen() != null
                ? entity.getnDiemtrungtuyen().toPlainString() : "");
 
        setCheckboxSl(chkThpt, txtSlThpt, "1".equals(entity.getnThpt()), entity.getSlThpt());
        setCheckboxSl(chkDgnl, txtSlDgnl, "1".equals(entity.getnDgnl()), toStr(entity.getSlDgnl()));
        setCheckboxSl(chkVsat, txtSlVsat, "1".equals(entity.getnVsat()), toStr(entity.getSlVsat()));
        setCheckboxSl(chkTuyenThang, txtSlXtt, "1".equals(entity.getnTuyenthang()), toStr(entity.getSlXtt()));
    }
    
    private void setCheckboxSl(JCheckBox chk, JTextField txtSl, boolean ticked, String sl) {
        chk.setSelected(ticked);
        txtSl.setEnabled(ticked);
        if (ticked && sl != null && !sl.isEmpty()) txtSl.setText(sl);
    }
 
    // Validate va luu entity tu form
    private void doSave() {
        String maNganh  = txtMaNganh.getText().trim().toUpperCase();
        String tenNganh = txtTenNganh.getText().trim();
        String tohopGoc = txtTohopGoc.getText().trim();
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
                if (chiTieu < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                warn("Chỉ tiêu phải là số nguyên không âm!");
                txtChiTieu.requestFocus();
                return;
            }
        } else {
            warn("Chỉ tiêu không được để trống!");
            txtChiTieu.requestFocus();
            return;
        }
        
        // Parse sl tung phuong thuc
        Integer slThpt = parseSl(chkThpt, txtSlThpt, "THPT");
        if (slThpt == null && chkThpt.isSelected()) return;
 
        Integer slDgnl = parseSl(chkDgnl, txtSlDgnl, "ĐGNL");
        if (slDgnl == null && chkDgnl.isSelected()) return;
 
        Integer slVsat = parseSl(chkVsat, txtSlVsat, "V-SAT");
        if (slVsat == null && chkVsat.isSelected()) return;
 
        Integer slXtt  = parseSl(chkTuyenThang, txtSlXtt, "Tuyển thẳng");
        if (slXtt == null && chkTuyenThang.isSelected()) return;
 
        // Validate tong sl = chi tieu (neu co it nhat 1 PT duoc chon)
        boolean anyChecked = chkThpt.isSelected() || chkDgnl.isSelected()
                          || chkVsat.isSelected() || chkTuyenThang.isSelected();
        if (anyChecked) {
            int tongSl = nvl(slThpt) + nvl(slDgnl) + nvl(slVsat) + nvl(slXtt);
            if (tongSl != chiTieu) {
                warn("Tổng chỉ tiêu các phương thức (" + tongSl
                        + ") phải bằng chỉ tiêu tổng (" + chiTieu + ")!");
                return;
            }
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
 
        // Build entity
        Nganh result = (entity != null) ? entity : new Nganh();
        result.setManganh(maNganh);
        result.setTennganh(tenNganh);
        result.setnTohopgoc(txtTohopGoc.getText().trim().isEmpty() ? null : txtTohopGoc.getText().trim());
        result.setnChitieu(chiTieu);
        result.setnDiemsan(diemSan);
        result.setnDiemtrungtuyen(diemTrungTuyen);
 
        result.setnThpt(chkThpt.isSelected() ? "1" : "0");
        result.setSlThpt(slThpt != null ? String.valueOf(slThpt) : null);
 
        result.setnDgnl(chkDgnl.isSelected() ? "1" : "0");
        result.setSlDgnl(slDgnl);
 
        result.setnVsat(chkVsat.isSelected() ? "1" : "0");
        result.setSlVsat(slVsat);
 
        result.setnTuyenthang(chkTuyenThang.isSelected() ? "1" : "0");
        result.setSlXtt(slXtt);
 
        this.entity = result;
        this.saved  = true;
        dispose();
    }
    
    private Integer parseSl(JCheckBox chk, JTextField txtSl, String tenPT) {
        if (!chk.isSelected()) return null;
        try {
            int val = Integer.parseInt(txtSl.getText().trim());
            if (val <= 0) throw new NumberFormatException();
            return val;
        } catch (NumberFormatException e) {
            warn("Chỉ tiêu phương thức " + tenPT + " phải là số nguyên dương!");
            txtSl.requestFocus();
            return null;
        }
    }
 
    private int nvl(Integer v)    { return v != null ? v : 0; }
    private String nvlStr(String s) { return s != null ? s : ""; }
    private String toStr(Integer v) { return v != null ? String.valueOf(v) : ""; }
 
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Loi nhap lieu", JOptionPane.WARNING_MESSAGE);
    }
    
    public boolean isSaved() { return saved; }
    public Nganh getEntity() { return entity; }
}
