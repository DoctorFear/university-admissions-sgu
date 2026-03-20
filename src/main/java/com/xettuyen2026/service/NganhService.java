package com.xettuyen2026.service;

import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.util.ImportUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NganhService {
	
	private final NganhDAO nganhDAO;
	
	public NganhService() {
		this.nganhDAO = new NganhDAO();
	}
	
	// Xem va tim kiem nganh
	public List<Nganh> findAll() {
		return nganhDAO.findAll();
	}
	public Nganh findByMaNganh(String maNganh) {
		return nganhDAO.findByMaNganh(maNganh);
	}
	public List<Nganh> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return nganhDAO.search(keyword.trim());
    }
	
	// Them moi nganh
	public void save(Nganh nganh) {
		validate(nganh);
		if (nganhDAO.existsByMaNganh(nganh.getManganh())) {
            throw new RuntimeException("Mã ngành '" + nganh.getManganh() + "' đã tồn tại!");
        }
        nganhDAO.save(nganh);
    }
	
	// Sua chua thong tin nganh
	public void update(Nganh nganh) {
		validate(nganh);
        nganhDAO.update(nganh);
    }
	
	// Xoa nganh 
	public void delete(Nganh nganh) {
        nganhDAO.delete(nganh);
    }
	
	// Import DS Nganh tu File Excel co row dua tren Entity
	public int importFromExcel(File file) throws Exception {
	    List<Nganh> list = ImportUtil.readExcel(file, row -> {
	        String ma = ImportUtil.getString(row, 0);
	        if (ma.isEmpty()) return null; // bo qua

	        Nganh n = new Nganh();
	        n.setManganh(ma); // Cot A
	        n.setTennganh(ImportUtil.getString(row, 1));  // Cot B
	        n.setnTohopgoc(ImportUtil.getString(row, 2)); // Cot C
	        n.setnChitieu(ImportUtil.getInt(row, 3));     // Cot D
	        n.setnDiemsan(ImportUtil.getDecimal(row, 4)); // Cot E
	        n.setnDiemtrungtuyen(ImportUtil.getDecimal(row, 5)); // Cot F
	        n.setnTuyenthang(ImportUtil.getString(row, 6)); // Cot G
	        n.setnDgnl(ImportUtil.getString(row, 7));     // Cot H
	        n.setnThpt(ImportUtil.getString(row, 8));     // Cot I
	        n.setnVsat(ImportUtil.getString(row, 9));     // Cot J
	        n.setSlXtt(ImportUtil.getInt(row, 10));       // Cot K
	        n.setSlDgnl(ImportUtil.getInt(row, 11));      // Cot L
	        n.setSlVsat(ImportUtil.getInt(row, 12));      // Cot M
	        n.setSlThpt(ImportUtil.getString(row, 13));   // Cot N
	        return n;
	    });

	    nganhDAO.saveAll(list);
	    return list.size();
	}
	
	// Kiem tra hop le thong tin
	private void validate(Nganh nganh) {
	    if (nganh.getManganh() == null || nganh.getManganh().trim().isEmpty()) {
	        throw new RuntimeException("Mã ngành không được để trống!");
	    }
	    if (nganh.getTennganh() == null || nganh.getTennganh().trim().isEmpty()) {
	        throw new RuntimeException("Tên ngành không được để trống!");
	    }
	    if (nganh.getnChitieu() == null || nganh.getnChitieu() < 0) {
	        throw new RuntimeException("Chỉ tiêu không hợp lệ!");
	    }
	}
}
