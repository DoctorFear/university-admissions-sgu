package com.xettuyen2026.ui;

//ui/thisinh/ThiSinhPanel.java
public class ThiSinhPanel extends JPanel {

 private ThiSinhService thiSinhService = new ThiSinhService();
 private JTextField txtCCCD = new JTextField(20);
 private JLabel lblKetQua = new JLabel();

 public ThiSinhPanel() {
     JButton btnTimKiem = new JButton("Tìm kiếm");

     btnTimKiem.addActionListener(e -> {
         try {
             String cccd = txtCCCD.getText();
             ThiSinh ts = thiSinhService.timKiemTheoCCCD(cccd);

             // Hiển thị kết quả lên form
             lblKetQua.setText(ts.getHo() + " " + ts.getTen()
                 + " | Ngày sinh: " + ts.getNgaySinh());

         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, ex.getMessage(),
                 "Lỗi", JOptionPane.ERROR_MESSAGE);
         }
     });

     add(new JLabel("CCCD:"));
     add(txtCCCD);
     add(btnTimKiem);
     add(lblKetQua);
 }
}
