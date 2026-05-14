package com.xettuyen2026.service;

import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.TohopMonthiDAO;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.util.ImportUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

public class NganhService {

    private final NganhDAO nganhDAO;
    private final TohopMonthiDAO tohopDAO;
    private final NguyenVongDAO nguyenVongDAO;

    public NganhService() {
        this.nganhDAO = new NganhDAO();
        this.tohopDAO = new TohopMonthiDAO();
        this.nguyenVongDAO = new NguyenVongDAO();     
    }

    // Lấy toàn bộ danh sách ngành
    public List<Nganh> findAll() {
        return nganhDAO.findAll();
    }
    
    // Lấy danh sách ngành và gắn số lượng nguyện vọng từng phương thức
    // (thống kê động, KHÔNG ghi đè dữ liệu import của ngành)
    public List<Nganh> findAllWithSlNguyenVong() {
        List<Nganh> danhSach = nganhDAO.findAll();
        Map<String, Integer> slMap = nguyenVongDAO.countByNganhAndPhuongThuc();

        for (Nganh n : danhSach) {
            String ma = n.getManganh();
            // ttPhuongthuc: TT=TuyenThang, PT4=DGNL, PT2=THPT, PT5=VSAT
            n.setSlXtt (getCount(slMap, ma, "TT"));
            n.setSlDgnl(getCount(slMap, ma, "PT4", "DGNL"));
            n.setSlThpt(String.valueOf(getCount(slMap, ma, "PT2", "THPT")));
            n.setSlVsat(getCount(slMap, ma, "PT5", "PT3", "VSAT", "V-SAT"));
        }
        return danhSach;
    }

    // Tìm ngành theo mã ngành
    public Nganh findByMaNganh(String maNganh) {
        return nganhDAO.findByMaNganh(maNganh);
    }

    // Tìm kiếm ngành theo mã hoặc tên
    public List<Nganh> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllWithSlNguyenVong();
        }

        String normalizedKeyword = normalize(keyword);
        return findAllWithSlNguyenVong().stream()
                .filter(n -> matches(n, normalizedKeyword))
                .sorted(Comparator
                        .comparingInt((Nganh n) -> getBestRank(n, normalizedKeyword))
                        .thenComparing(n -> normalize(n.getTennganh()))
                        .thenComparing(n -> normalize(n.getManganh())))
                .collect(Collectors.toList());
    }

    // Thêm mới ngành từ form quản lý ngành
    public void save(Nganh nganh) {
        validate(nganh);
        if (nganhDAO.existsByMaNganh(nganh.getManganh())) {
            throw new RuntimeException("Mã ngành '" + nganh.getManganh() + "' đã tồn tại!");
        }
        nganhDAO.save(nganh);
    }

    // Cập nhật ngành từ form quản lý ngành
    public void update(Nganh nganh) {
        validate(nganh);
        validateCanDisableMethods(nganh);
        nganhDAO.update(nganh);
    }

    // Xóa ngành khỏi hệ thống
    // Chỉ cho xóa nếu ngành chưa có nguyện vọng nào đăng ký
    public void delete(Nganh nganh) {
        Map<String, Integer> slMap = nguyenVongDAO.countByNganhAndPhuongThuc();
        String ma = nganh.getManganh();

        boolean coNguyenVong =
                getCount(slMap, ma, "TT") > 0 ||
                getCount(slMap, ma, "PT4", "DGNL") > 0 ||
                getCount(slMap, ma, "PT2", "THPT") > 0 ||
                getCount(slMap, ma, "PT5", "PT3", "VSAT", "V-SAT") > 0;

        if (coNguyenVong) {
            throw new RuntimeException(
                "Không thể xóa ngành '" + nganh.getManganh() +
                "' vì đã có thí sinh đăng ký nguyện vọng!");
        }
        nganhDAO.delete(nganh);
    }

    // Import danh sách ngành từ file Nganh.xlsx gồm 3 sheet ChiTieu, DiemSan, ToHop
    public int importFromExcel(File file) throws Exception {
        // Code cũ:
        // List<Nganh> list = ImportUtil.readExcel(file, row -> {
        //     String ma = ImportUtil.getString(row, 0);
        //     if (ma.isEmpty()) return null;
        //
        //     Nganh n = new Nganh();
        //     n.setManganh(ma);
        //     n.setTennganh(ImportUtil.getString(row, 1));
        //     n.setnTohopgoc(ImportUtil.getString(row, 2));
        //     n.setnChitieu(ImportUtil.getInt(row, 3));
        //     n.setnDiemsan(ImportUtil.getDecimal(row, 4));
        //     n.setnDiemtrungtuyen(ImportUtil.getDecimal(row, 5));
        //     n.setnTuyenthang(ImportUtil.getString(row, 6));
        //     n.setnDgnl(ImportUtil.getString(row, 7));
        //     n.setnThpt(ImportUtil.getString(row, 8));
        //     n.setnVsat(ImportUtil.getString(row, 9));
        //     n.setSlXtt(ImportUtil.getInt(row, 10));
        //     n.setSlDgnl(ImportUtil.getInt(row, 11));
        //     n.setSlVsat(ImportUtil.getInt(row, 12));
        //     n.setSlThpt(ImportUtil.getString(row, 13));
        //     return n;
        // });
        // nganhDAO.saveAll(list);
        // return list.size();

        ImportNganhResult result = importWorkbookNganh(file);
        return result.updateCount;
    }

    // Import một workbook ngành và xử lý lần lượt 3 sheet theo đúng thứ tự
    public ImportNganhResult importWorkbookNganh(File file) throws Exception {
        ImportNganhResult tongKet = new ImportNganhResult();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheetChiTieu = timSheet(workbook, "ChiTieu", 0);
            Sheet sheetDiemSan = timSheet(workbook, "DiemSan", 1);
            Sheet sheetToHop = timSheet(workbook, "ToHop", 2);

            if (sheetChiTieu != null) {
                congKetQua(tongKet, xuLyChiTieuTuSheet(sheetChiTieu));
            }
            if (sheetDiemSan != null) {
                congKetQua(tongKet, xuLyDiemSanTuSheet(sheetDiemSan));
            }
            if (sheetToHop != null) {
                congKetQua(tongKet, xuLyToHopGocTuSheet(sheetToHop));
            }
        }

        return tongKet;
    }

    // Import file chỉ tiêu 
    public ImportNganhResult importChiTieu(File file) throws Exception {
        return xuLyChiTieu(file);
    }

    // Import file điểm sàn 
    public ImportNganhResult importDiemSan(File file) throws Exception {
        return xuLyDiemSan(file);
    }

    // Import file tổ hợp gốc của ngành 
    public ImportNganhResult importTohopGoc(File file) throws Exception {
        return xuLyToHopGoc(file);
    }

    // Đọc riêng file chỉ tiêu và chuyển sang luồng xử lý chung
    private ImportNganhResult xuLyChiTieu(File file) throws Exception {
        List<String[]> rawList = ImportUtil.readExcel(file, row -> {
            String ma = ImportUtil.getString(row, 1);
            if (ma.isEmpty()) return null;

            try {
                Integer.parseInt(ImportUtil.getString(row, 0));
            } catch (NumberFormatException e) {
                return null;
            }

            String tenNganh = ImportUtil.getString(row, 2);
            String chiTieu = ImportUtil.getString(row, 3);
            return new String[]{ma.trim(), tenNganh, chiTieu};
        });

        return xuLyDanhSachChiTieu(rawList);
    }

    // Đọc sheet chỉ tiêu từ workbook ngành và chuyển sang luồng xử lý chung
    private ImportNganhResult xuLyChiTieuTuSheet(Sheet sheet) {
        List<String[]> rawList = new ArrayList<>();
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String ma = ImportUtil.getString(row, 1);
            if (ma.isEmpty()) continue;

            try {
                Integer.parseInt(ImportUtil.getString(row, 0));
            } catch (NumberFormatException e) {
                continue;
            }

            String tenNganh = ImportUtil.getString(row, 2);
            String chiTieu = ImportUtil.getString(row, 3);
            rawList.add(new String[]{ma.trim(), tenNganh, chiTieu});
        }
        return xuLyDanhSachChiTieu(rawList);
    }

    // Xử lý danh sách chỉ tiêu sau khi đã đọc từ file hoặc sheet
    private ImportNganhResult xuLyDanhSachChiTieu(List<String[]> rawList) {
        ImportNganhResult result = new ImportNganhResult();

        for (String[] row : rawList) {
            String ma = row[0];
            String tenNganh = row[1];
            String chiTieuStr = row[2];

            try {
                Nganh nganh = layHoacTaoNganhImport(ma, tenNganh);
                int chiTieu = (int) Double.parseDouble(chiTieuStr);
                nganh.setnChitieu(chiTieu);
                luuNganhImport(nganh);
                result.updateCount++;
            } catch (Exception e) {
                result.errorCount++;
                result.errors.add("Mã " + ma + ": " + e.getMessage());
            }
        }

        return result;
    }

    // Đọc riêng file điểm sàn và chuyển sang luồng xử lý chung
    private ImportNganhResult xuLyDiemSan(File file) throws Exception {
        List<String[]> rawList = ImportUtil.readExcel(file, row -> {
            String ma = ImportUtil.getString(row, 1);
            if (ma.isEmpty()) return null;

            try {
                Integer.parseInt(ImportUtil.getString(row, 0));
            } catch (NumberFormatException e) {
                return null;
            }

            String tenNganh = ImportUtil.getString(row, 2);
            String diemSan = ImportUtil.getString(row, 3);
            return new String[]{ma.trim(), tenNganh, diemSan};
        });

        return xuLyDanhSachDiemSan(rawList);
    }

    // Đọc sheet điểm sàn từ workbook ngành và chuyển sang luồng xử lý chung
    private ImportNganhResult xuLyDiemSanTuSheet(Sheet sheet) {
        List<String[]> rawList = new ArrayList<>();
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String ma = ImportUtil.getString(row, 1);
            if (ma.isEmpty()) continue;

            try {
                Integer.parseInt(ImportUtil.getString(row, 0));
            } catch (NumberFormatException e) {
                continue;
            }

            String tenNganh = ImportUtil.getString(row, 2);
            String diemSan = ImportUtil.getString(row, 3);
            rawList.add(new String[]{ma.trim(), tenNganh, diemSan});
        }
        return xuLyDanhSachDiemSan(rawList);
    }

    // Xử lý danh sách điểm sàn sau khi đã đọc từ file hoặc sheet
    private ImportNganhResult xuLyDanhSachDiemSan(List<String[]> rawList) {
        ImportNganhResult result = new ImportNganhResult();

        for (String[] row : rawList) {
            String ma = row[0];
            String tenNganh = row[1];
            String diemSanStr = row[2];

            try {
                Nganh nganh = layHoacTaoNganhImport(ma, tenNganh);
                nganh.setnDiemsan(parseDecimal(diemSanStr));
                luuNganhImport(nganh);
                result.updateCount++;
            } catch (Exception e) {
                result.errorCount++;
                result.errors.add("Mã " + ma + ": " + e.getMessage());
            }
        }

        return result;
    }

    // Đọc riêng file tổ hợp gốc và chuyển sang luồng xử lý chung
    private ImportNganhResult xuLyToHopGoc(File file) throws Exception {
        List<String[]> rawList = ImportUtil.readExcel(file, row -> {
            // Bỏ qua dòng tiêu đề (thường là row 0)
            if (row.getRowNum() == 0) return null;

            String goc = ImportUtil.getString(row, 6).trim();
            if (!"Gốc".equalsIgnoreCase(goc) && !"Goc".equalsIgnoreCase(goc)) return null;

            String maNganh = ImportUtil.getString(row, 1).trim();
            if (maNganh.isEmpty()) return null;

            String tenNganh = ImportUtil.getString(row, 2).trim();
            // Lấy trực tiếp cột TEN_TO_HOP (index 5)
            String maTohop = ImportUtil.getString(row, 5).trim();
            
            if (maTohop.isEmpty()) return null;

            return new String[]{maNganh, tenNganh, maTohop};
        });

        return xuLyDanhSachToHopGoc(rawList);
    }

    // Đọc sheet tổ hợp gốc từ workbook ngành và chuyển sang luồng xử lý chung
    private ImportNganhResult xuLyToHopGocTuSheet(Sheet sheet) {
        List<String[]> rawList = new ArrayList<>();
        // Bắt đầu từ i = 1 để bỏ qua dòng tiêu đề
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String goc = ImportUtil.getString(row, 6).trim();
            if (!"Gốc".equalsIgnoreCase(goc) && !"Goc".equalsIgnoreCase(goc)) continue;

            String maNganh = ImportUtil.getString(row, 1).trim();
            if (maNganh.isEmpty()) continue;

            String tenNganh = ImportUtil.getString(row, 2).trim();
            // Lấy trực tiếp cột TEN_TO_HOP (index 5)
            String maTohop = ImportUtil.getString(row, 5).trim();
            
            if (maTohop.isEmpty()) continue;

            rawList.add(new String[]{maNganh, tenNganh, maTohop});
        }
        return xuLyDanhSachToHopGoc(rawList);
    }

    // Xử lý danh sách tổ hợp gốc sau khi đã đọc từ file hoặc sheet
    private ImportNganhResult xuLyDanhSachToHopGoc(List<String[]> rawList) {
        ImportNganhResult result = new ImportNganhResult();
        Set<String> daXuLy = new LinkedHashSet<>();

        for (String[] row : rawList) {
            String maNganh = row[0];
            String tenNganh = row[1];
            String maTohop = row[2];

            if (daXuLy.contains(maNganh)) {
                result.skipCount++;
                continue;
            }
            daXuLy.add(maNganh);

            try {
                Nganh nganh = layHoacTaoNganhImport(maNganh, tenNganh);
                nganh.setnTohopgoc(maTohop);
                luuNganhImport(nganh);
                result.updateCount++;
            } catch (Exception e) {
                result.errorCount++;
                result.errors.add("Mã " + maNganh + ": " + e.getMessage());
            }
        }

        return result;
    }

    // Tìm sheet đúng tên kỳ vọng hoặc fallback theo vị trí sheet trong workbook
    private Sheet timSheet(Workbook workbook, String tenSheet, int fallbackIndex) {
        Sheet sheetTheoTen = workbook.getSheet(tenSheet);
        if (sheetTheoTen != null) {
            return sheetTheoTen;
        }

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String tenDangCo = workbook.getSheetName(i);
            if (normalize(tenDangCo).equalsIgnoreCase(normalize(tenSheet))) {
                return workbook.getSheetAt(i);
            }
        }

        if (fallbackIndex >= 0 && fallbackIndex < workbook.getNumberOfSheets()) {
            return workbook.getSheetAt(fallbackIndex);
        }
        return null;
    }

    // Gộp kết quả của từng sheet vào kết quả import tổng
    private void congKetQua(ImportNganhResult tongKet, ImportNganhResult ketQuaSheet) {
        tongKet.updateCount += ketQuaSheet.updateCount;
        tongKet.skipCount += ketQuaSheet.skipCount;
        tongKet.errorCount += ketQuaSheet.errorCount;
        tongKet.errors.addAll(ketQuaSheet.errors);
        tongKet.skippedMa.addAll(ketQuaSheet.skippedMa);
    }

    // Chuẩn hóa mã tổ hợp gốc để chỉ còn phần mã ngắn phù hợp với cột n_tohopgoc
    private String chuanHoaMaToHopGoc(String tenToHop, String maToHopDayDu) {
        String maTohop = tenToHop != null ? tenToHop.trim().toUpperCase() : "";
        if (!maTohop.isEmpty()) {
            return maTohop;
        }

        // Fallback: tách phần mã đứng trước dấu "(" từ cột MA_TO_HOP
        String maToHopGoc = maToHopDayDu != null ? maToHopDayDu.trim().toUpperCase() : "";
        int indexNgoac = maToHopGoc.indexOf('(');
        if (indexNgoac > 0) {
            maToHopGoc = maToHopGoc.substring(0, indexNgoac).trim();
        }
        return maToHopGoc;
    }

    // Lấy ngành hiện có hoặc tạo ngành mới với dữ liệu tối thiểu để import tiếp
    private Nganh layHoacTaoNganhImport(String maNganh, String tenNganh) {
        Nganh nganh = nganhDAO.findByMaNganh(maNganh);
        if (nganh != null) {
            capNhatTenNganhNeuCan(nganh, tenNganh);
            batTatCaPhuongThuc(nganh);
            return nganh;
        }

        Nganh nganhMoi = new Nganh();
        nganhMoi.setManganh(maNganh);
        nganhMoi.setTennganh(chuanHoaTenNganh(tenNganh, maNganh));
        nganhMoi.setnChitieu(0);
        batTatCaPhuongThuc(nganhMoi);
        return nganhMoi;
    }

    // Lưu ngành theo đúng trạng thái thêm mới hoặc cập nhật
    private void luuNganhImport(Nganh nganh) {
        if (nganh.getIdnganh() == null) {
            nganhDAO.save(nganh);
            return;
        }
        nganhDAO.update(nganh);
    }

    // Bổ sung tên ngành nếu bản ghi cũ đang chưa có tên
    private void capNhatTenNganhNeuCan(Nganh nganh, String tenNganhMoi) {
        if (nganh.getTennganh() == null || nganh.getTennganh().trim().isEmpty()) {
            nganh.setTennganh(chuanHoaTenNganh(tenNganhMoi, nganh.getManganh()));
        }
    }

    // Chuẩn hóa tên ngành khi phải tạo mới ngành từ file import
    private String chuanHoaTenNganh(String tenNganh, String maNganh) {
        if (tenNganh != null && !tenNganh.trim().isEmpty()) {
            return tenNganh.trim();
        }
        return "Chưa có tên - " + maNganh;
    }

    // Parse số thập phân từ chuỗi Excel về BigDecimal
    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Giá trị số không được để trống.");
        }
        return new BigDecimal(value.trim().replace(",", "."));
    }

    // Kiểm tra hợp lệ dữ liệu nhập từ form ngành
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
        if (!isMethodEnabled(nganh.getnTuyenthang()) && !isMethodEnabled(nganh.getnDgnl())
                && !isMethodEnabled(nganh.getnThpt()) && !isMethodEnabled(nganh.getnVsat())) {
            throw new RuntimeException("Ngành phải có ít nhất một phương thức xét tuyển!");
        }
    }

    // Kiểm tra không bỏ phương thức đã có nguyện vọng
    private void validateCanDisableMethods(Nganh nganh) {
        Map<String, Integer> slMap = nguyenVongDAO.countByNganhAndPhuongThuc();
        String ma = nganh.getManganh();

        if (!isMethodEnabled(nganh.getnTuyenthang()) && getCount(slMap, ma, "TT") > 0) {
            throw new RuntimeException("Không thể bỏ phương thức Tuyển thẳng vì đã có nguyện vọng đăng ký!");
        }
        if (!isMethodEnabled(nganh.getnDgnl()) && getCount(slMap, ma, "PT4", "DGNL") > 0) {
            throw new RuntimeException("Không thể bỏ phương thức ĐGNL vì đã có nguyện vọng đăng ký!");
        }
        if (!isMethodEnabled(nganh.getnThpt()) && getCount(slMap, ma, "PT2", "THPT") > 0) {
            throw new RuntimeException("Không thể bỏ phương thức THPT vì đã có nguyện vọng đăng ký!");
        }
        if (!isMethodEnabled(nganh.getnVsat()) && getCount(slMap, ma, "PT5", "PT3", "VSAT", "V-SAT") > 0) {
            throw new RuntimeException("Không thể bỏ phương thức V-SAT vì đã có nguyện vọng đăng ký!");
        }
    }

    // Bật mặc định tất cả phương thức khi import ngành
    private void batTatCaPhuongThuc(Nganh nganh) {
        nganh.setnTuyenthang("1");
        nganh.setnDgnl("1");
        nganh.setnThpt("1");
        nganh.setnVsat("1");
    }

    // Kiểm tra phương thức còn được xét tuyển hay không
    private boolean isMethodEnabled(String flag) {
        return flag == null || !"-".equals(flag.trim());
    }

    // Lấy số lượng nguyện vọng theo ngành và các mã phương thức tương ứng
    private int getCount(Map<String, Integer> slMap, String maNganh, String... phuongThucList) {
        String ma = maNganh != null ? maNganh.trim().toUpperCase(Locale.ROOT) : "";
        int total = 0;
        for (String phuongThuc : phuongThucList) {
            String pt = phuongThuc != null ? phuongThuc.trim().toUpperCase(Locale.ROOT) : "";
            total += slMap.getOrDefault(ma + "|" + pt, 0);
        }
        return total;
    }

    // Lấy danh sách tổ hợp hợp lệ để kiểm tra dữ liệu ngành
    public List<String> getValidTohopCheck() {
        return tohopDAO.findAll()
                .stream()
                .map(t -> t.getMatohop().trim().toUpperCase())
                .collect(Collectors.toList());
    }

    // Kiểm tra ngành có khớp với từ khóa tìm kiếm hay không
    private boolean matches(Nganh nganh, String keyword) {
        return containsNormalized(nganh.getManganh(), keyword)
                || containsNormalized(nganh.getTennganh(), keyword);
    }

    // Tính độ ưu tiên khớp tốt nhất giữa mã ngành và tên ngành
    private int getBestRank(Nganh nganh, String keyword) {
        int maRank = getMatchRank(nganh.getManganh(), keyword);
        int tenRank = getMatchRank(nganh.getTennganh(), keyword);
        return Math.min(maRank, tenRank);
    }

    // Tính mức độ khớp của một chuỗi nguồn với từ khóa tìm kiếm
    private int getMatchRank(String source, String keyword) {
        String normalizedSource = normalize(source);
        if (normalizedSource.isEmpty() || keyword.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int index = normalizedSource.indexOf(keyword);
        if (index < 0) {
            return Integer.MAX_VALUE;
        }
        if (index == 0) {
            return 0;
        }
        if (normalizedSource.charAt(index - 1) == ' ') {
            return 1;
        }
        return 2;
    }

    // Kiểm tra chuỗi nguồn có chứa từ khóa sau khi chuẩn hóa hay không
    private boolean containsNormalized(String source, String keyword) {
        return !normalize(source).isEmpty() && normalize(source).contains(keyword);
    }

    // Chuẩn hóa chuỗi tiếng Việt để tìm kiếm không dấu
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    // Chứa kết quả tổng hợp sau mỗi lần import dữ liệu ngành
    public static class ImportNganhResult {
        public int updateCount = 0;
        public int skipCount = 0;
        public int errorCount = 0;
        public List<String> errors = new ArrayList<>();
        public List<String> skippedMa = new ArrayList<>();
    }
}
