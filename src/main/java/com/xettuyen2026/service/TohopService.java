package com.xettuyen2026.service;

import com.xettuyen2026.dao.TohopMonthiDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.entity.TohopMonthi;
import com.xettuyen2026.util.TohopValidator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/**
 * Service cho quản lý tổ hợp môn xét tuyển.
 * 
 * Chức năng:
 * - Validate dữ liệu tổ hợp môn
 * - CRUD tổ hợp môn
 * - Import từ file Excel / Text
 * - Kiểm tra ràng buộc dữ liệu
 * 
 * @author Senior Developer
 */
public class TohopService {

    private final TohopMonthiDAO dao = new TohopMonthiDAO();

    public List<TohopMonthi> findAll() {
        return dao.findAll();
    }

    public TohopMonthi findByMaTohop(String maTohop) {
        return dao.findByMaTohop(maTohop);
    }

    /**
     * Thêm mới tổ hợp môn với validation hoàn toàn.
     * - Normalize dữ liệu (trim, uppercase)
     * - Validate mã tổ hợp
     * - Validate 3 môn học
     * - Generate tên tổ hợp tự động
     * - Kiểm tra trùng lặp mã tổ hợp
     * 
     * @param tohop Đối tượng tổ hợp môn
     * @throws IllegalArgumentException Nếu dữ liệu không hợp lệ
     * @throws RuntimeException Nếu xảy ra lỗi database
     */
    public void save(TohopMonthi tohop) {
        if (tohop == null) {
            throw new IllegalArgumentException("Tổ hợp môn không được null");
        }

        // Normalize dữ liệu
        String maTohop = TohopValidator.normalizeMaTohop(tohop.getMatohop());
        String mon1 = TohopValidator.normalizeSubject(tohop.getMon1());
        String mon2 = TohopValidator.normalizeSubject(tohop.getMon2());
        String mon3 = TohopValidator.normalizeSubject(tohop.getMon3());

        // Validate
        String maTohopErr = TohopValidator.getMaTohopError(maTohop);
        if (maTohopErr != null) {
            throw new IllegalArgumentException(maTohopErr);
        }

        String subjectErr = TohopValidator.getSubjectsError(mon1, mon2, mon3);
        if (subjectErr != null) {
            throw new IllegalArgumentException(subjectErr);
        }

        // Check trùng lặp
        if (dao.findByMaTohop(maTohop) != null) {
            throw new IllegalArgumentException("Mã tổ hợp " + maTohop + " đã tồn tại!");
        }

        // Set dữ liệu đã normalize
        tohop.setMatohop(maTohop);
        tohop.setMon1(mon1);
        tohop.setMon2(mon2);
        tohop.setMon3(mon3);

        // Auto-generate tên tổ hợp nếu chưa có hoặc để trống
        if (tohop.getTentohop() == null || tohop.getTentohop().trim().isEmpty()) {
            tohop.setTentohop(TohopValidator.generateTohopName(mon1, mon2, mon3));
        }

        // Lưu vào database
        dao.save(tohop);
    }

    /**
     * Cập nhật tổ hợp môn.
     * - Kiểm tra xem tổ hợp có được sử dụng trong xt_nganh_tohop không
     * - Nếu có, sẽ warning nhưng vẫn cho phép sửa
     * 
     * @param tohop Đối tượng tổ hợp môn cần sửa
     * @throws IllegalArgumentException Nếu dữ liệu không hợp lệ
     */
    public void update(TohopMonthi tohop) {
        if (tohop == null || tohop.getIdtohop() == null) {
            throw new IllegalArgumentException("Dữ liệu tổ hợp không hợp lệ");
        }

        // Normalize dữ liệu
        String mon1 = TohopValidator.normalizeSubject(tohop.getMon1());
        String mon2 = TohopValidator.normalizeSubject(tohop.getMon2());
        String mon3 = TohopValidator.normalizeSubject(tohop.getMon3());

        // Validate 3 môn (không validate mã tổ hợp vì đang sửa)
        String subjectErr = TohopValidator.getSubjectsError(mon1, mon2, mon3);
        if (subjectErr != null) {
            throw new IllegalArgumentException(subjectErr);
        }

        // Set dữ liệu đã normalize
        tohop.setMon1(mon1);
        tohop.setMon2(mon2);
        tohop.setMon3(mon3);

        // Auto-update tên tổ hợp nếu chưa có
        if (tohop.getTentohop() == null || tohop.getTentohop().trim().isEmpty()) {
            tohop.setTentohop(TohopValidator.generateTohopName(mon1, mon2, mon3));
        }

        // Warning: kiểm tra xem tổ hợp có được sử dụng không
        boolean isUsed = dao.isUsedInNganhTohop(tohop.getMatohop());
        if (isUsed) {
            System.err.println("[WARNING] Tổ hợp " + tohop.getMatohop() 
                    + " đang được sử dụng trong dữ liệu ngành-tổ hợp. Hãy chắc chắn bạn biết mình đang làm gì!");
        }

        dao.update(tohop);
    }

    /**
     * Xóa tổ hợp môn.
     * - KHÔNG cho phép xóa nếu tổ hợp đang được sử dụng trong xt_nganh_tohop
     * 
     * @param tohop Đối tượng tổ hợp môn
     * @throws IllegalArgumentException Nếu tổ hợp đang được sử dụng
     */
    public void delete(TohopMonthi tohop) {
        if (tohop == null || tohop.getIdtohop() == null) {
            throw new IllegalArgumentException("Dữ liệu tổ hợp không hợp lệ");
        }

        // Check xem có dữ liệu phụ thuộc không
        if (dao.isUsedInNganhTohop(tohop.getMatohop())) {
            throw new IllegalArgumentException(
                    "Không thể xóa vì tổ hợp này đang được sử dụng trong dữ liệu ngành-tổ hợp. " +
                    "Hãy xóa dữ liệu liên quan trước!");
        }

        dao.delete(tohop);
    }

    /**
     * Kiểm tra xem tổ hợp môn có được sử dụng trong xt_nganh_tohop không.
     * @param maTohop Mã tổ hợp
     * @return true nếu đang được sử dụng, false nếu không
     */
    public boolean isUsedInNganhTohop(String maTohop) {
        return dao.isUsedInNganhTohop(maTohop);
    }

    /**
     * Import tổ hợp môn từ file tohopmon.txt.
     * Trích xuất các mã tổ hợp duy nhất từ cột MA_TO_HOP có dạng "A00(TO-3,LI-3,HO-1)".
     * Trả về số bản ghi đã import.
     */
    public int importFromTohopMonFile(File file) throws IOException {
        Map<String, TohopMonthi> tohopMap = new LinkedHashMap<>();
        // Regex matches: A00(TO-3,LI-3,HO-1) — captures maTohop and 3 subjects
        Pattern pattern = Pattern.compile("(\\w+)\\((\\w+)-(\\d+),(\\w+)-(\\d+),(\\w+)-(\\d+)\\)");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---")) continue;
                if (!headerSkipped) {
                    if (line.contains("STT") && line.contains("MANGANH")) {
                        headerSkipped = true;
                    }
                    continue;
                }

                Matcher matcher = pattern.matcher(line);
                if (!matcher.find()) continue;

                String maTohop = matcher.group(1);
                if (tohopMap.containsKey(maTohop)) continue;

                String mon1 = matcher.group(2);
                String mon2 = matcher.group(4);
                String mon3 = matcher.group(6);

                TohopMonthi th = new TohopMonthi();
                th.setMatohop(maTohop);
                th.setMon1(mon1);
                th.setMon2(mon2);
                th.setMon3(mon3);
                th.setTentohop(maTohop);
                tohopMap.put(maTohop, th);
            }
        }

        List<TohopMonthi> toImport = new ArrayList<>();
        for (TohopMonthi th : tohopMap.values()) {
            TohopMonthi existing = dao.findByMaTohop(th.getMatohop());
            if (existing == null) {
                toImport.add(th);
            }
        }

        if (!toImport.isEmpty()) {
            dao.saveAll(toImport);
        }
        return toImport.size();
    }

    /**
     * Import tổ hợp môn từ file Excel (.xlsx/.xls) — file tohopmon.xlsx.
     * Cột trong file: STT(0), MANGANH(1), TEN_NGANHCHUAN(2), MA_TO_HOP(3), tb_keys(4), TEN_TO_HOP(5), Gốc(6), Độ lệch(7)
     * Trích xuất mã tổ hợp duy nhất từ cột MA_TO_HOP có dạng "A00(TO-3,LI-3,HO-1)".
     */
    public int importFromExcel(File file) throws IOException {
        Map<String, TohopMonthi> tohopMap = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("(\\w+)\\((\\w+)-(\\d+),(\\w+)-(\\d+),(\\w+)-(\\d+)\\)");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().toLowerCase().endsWith(".xlsx")
                     ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean headerSkipped = false;

            for (Row row : sheet) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                // Cột 3: MA_TO_HOP dạng "B03(TO-3,VA-3,SI-1)"
                String maToHopRaw = getCellString(row, 3);
                if (maToHopRaw.isEmpty()) continue;

                Matcher matcher = pattern.matcher(maToHopRaw);
                if (!matcher.find()) continue;

                String maTohop = matcher.group(1);
                if (tohopMap.containsKey(maTohop)) continue;

                String mon1 = matcher.group(2);
                String mon2 = matcher.group(4);
                String mon3 = matcher.group(6);

                // Cột 5: TEN_TO_HOP
                String tenTohop = getCellString(row, 5);
                if (tenTohop.isEmpty()) tenTohop = maTohop;

                TohopMonthi th = new TohopMonthi();
                th.setMatohop(maTohop);
                th.setMon1(mon1.toUpperCase());
                th.setMon2(mon2.toUpperCase());
                th.setMon3(mon3.toUpperCase());
                th.setTentohop(tenTohop);
                tohopMap.put(maTohop, th);
            }
        }

        List<TohopMonthi> toImport = new ArrayList<>();
        for (TohopMonthi th : tohopMap.values()) {
            TohopMonthi existing = dao.findByMaTohop(th.getMatohop());
            if (existing == null) {
                toImport.add(th);
            }
        }

        if (!toImport.isEmpty()) {
            dao.saveAll(toImport);
        }
        return toImport.size();
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) yield String.valueOf((long) val);
                else yield String.valueOf(val);
            }
            default -> "";
        };
    }
}
