package com.xettuyen2026.service;

//service/ThiSinhService.java
public class ThiSinhService {

 private ThiSinhDAO thiSinhDAO = new ThiSinhDAO();

 public ThiSinh timKiemTheoCCCD(String cccd) {
     // Validate trước khi xuống DAO
     if (cccd == null || cccd.trim().isEmpty()) {
         throw new IllegalArgumentException("CCCD không được để trống");
     }
     if (cccd.length() != 12) {
         throw new IllegalArgumentException("CCCD phải đủ 12 số");
     }

     ThiSinh ts = thiSinhDAO.findByCCCD(cccd.trim());

     if (ts == null) {
         throw new RuntimeException("Không tìm thấy thí sinh với CCCD: " + cccd);
     }
     return ts;
 }
}