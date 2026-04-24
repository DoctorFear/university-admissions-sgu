package com.xettuyen2026.service;

import com.xettuyen2026.dao.DiemThiDAO;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.util.ImportUtil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DiemThiService {

    public static final String PHUONG_THUC_THPT = "THPT";
    public static final String PHUONG_THUC_DGNL = "DGNL";
    public static final String PHUONG_THUC_VSAT = "VSAT";

    public static final String PHUONG_THUC_CODE_THPT = "1";
    public static final String PHUONG_THUC_CODE_DGNL = "4";
    public static final String PHUONG_THUC_CODE_VSAT = "5";

    private static final Map<String, List<String>> MON_FIELD_MAP = new LinkedHashMap<>();

    static {
        addMonMapping("toan", "to");
        addMonMapping("van", "va");
        addMonMapping("ly", "li");
        addMonMapping("hoa", "ho");
        addMonMapping("sinh", "si");
        addMonMapping("su", "su");
        addMonMapping("dia", "di");
        addMonMapping("gdcd", "gdcd");
        addMonMapping("anh", "n1Thi", "n1Cc");
        addMonMapping("tieng anh", "n1Thi", "n1Cc");
        addMonMapping("anh thi", "n1Thi");
        addMonMapping("anh cc", "n1Cc");
        addMonMapping("n1", "n1Thi", "n1Cc");
        addMonMapping("n1 thi", "n1Thi");
        addMonMapping("n1 cc", "n1Cc");
        addMonMapping("tin", "ti");
        addMonMapping("ktpl", "ktpl");
        addMonMapping("cncn", "cncn");
        addMonMapping("cnnn", "cnnn");
        addMonMapping("nk1", "nk1");
        addMonMapping("nk2", "nk2");
        addMonMapping("nk3", "nk3");
        addMonMapping("nk4", "nk4");
        addMonMapping("nk5", "nk5");
        addMonMapping("nk6", "nk6");
    }

    private final DiemThiDAO diemThiDAO;

    public DiemThiService() {
        this.diemThiDAO = new DiemThiDAO();
    }

    // ════════════════════════════════════════════════════════════
    //  KẾT QUẢ IMPORT — trả về để UI hiển thị thông báo chi tiết
    // ════════════════════════════════════════════════════════════

    public static class ImportResult {
        public int insertCount = 0;
        public int updateCount = 0;
        public int skipCount   = 0;
        public int errorCount  = 0;
        public List<String> errors = new ArrayList<>();

        // Tổng số bản ghi xử lý thành công
        public int total() {
            return insertCount + updateCount;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  CRUD CƠ BẢN
    // ════════════════════════════════════════════════════════════

    public List<DiemThiXetTuyen> findAll() {
        return diemThiDAO.findAll();
    }

    public List<DiemThiXetTuyen> findByPhuongThuc(String phuongThuc) {
        return diemThiDAO.findByPhuongThuc(normalizePhuongThucCode(phuongThuc));
    }

    public DiemThiXetTuyen findByCccd(String cccd) {
        return diemThiDAO.findByCccd(cccd);
    }

    public DiemThiXetTuyen findByCccdAndPhuongThuc(String cccd, String phuongThuc) {
        return diemThiDAO.findByCccdAndPhuongThuc(cccd, normalizePhuongThucCode(phuongThuc));
    }

    public List<DiemThiXetTuyen> search(String keyword, String phuongThuc) {
        String phuongThucCode = normalizePhuongThucCode(phuongThuc);
        if (keyword == null || keyword.trim().isEmpty()) {
            return diemThiDAO.findByPhuongThuc(phuongThucCode);
        }

        if (isDgnl(phuongThucCode)) {
            return diemThiDAO.searchByCccd(keyword.trim().toLowerCase(Locale.ROOT), phuongThucCode);
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        List<String> matchedFields = findMatchingSubjectFields(normalizedKeyword, phuongThucCode);
        if (!matchedFields.isEmpty()) {
            return diemThiDAO.searchByAnyMon(matchedFields, phuongThucCode);
        }

        return diemThiDAO.searchByCccd(keyword.trim().toLowerCase(Locale.ROOT), phuongThucCode);
    }

    public void save(DiemThiXetTuyen entity) {
        prepareForSave(entity);
        validate(entity);
        if (diemThiDAO.existsByCccdAndPhuongThuc(entity.getCccd(), entity.getdPhuongthuc())) {
            throw new RuntimeException(
                "CCCD '" + entity.getCccd() + "' da ton tai o phuong thuc " + getPhuongThucLabel(entity.getdPhuongthuc()) + "!"
            );
        }
        diemThiDAO.save(entity);
    }

    public void update(DiemThiXetTuyen entity) {
        prepareForSave(entity);
        validate(entity);

        if (entity.getIddiemthi() == null) {
            DiemThiXetTuyen existing = diemThiDAO.findByCccdAndPhuongThuc(entity.getCccd(), entity.getdPhuongthuc());
            if (existing == null) {
                throw new RuntimeException("Khong tim thay ban ghi diem thi can cap nhat.");
            }
            entity.setIddiemthi(existing.getIddiemthi());
        }

        diemThiDAO.update(entity);
    }

    public void delete(DiemThiXetTuyen entity) {
        diemThiDAO.delete(entity);
    }

    // ════════════════════════════════════════════════════════════
    //  IMPORT EXCEL
    // ════════════════════════════════════════════════════════════

    /**
     * Import điểm thi từ file Excel theo phương thức thi.
     * Trả về ImportResult chứa số liệu thêm mới / cập nhật / lỗi để UI hiển thị.
     *
     * Cấu trúc file Excel:
     *   - THPT : "Ds thi sinh.xlsx" — dòng header ở row 0, dữ liệu từ row 1
     *   - ĐGNL : sheet "DGNL" — mỗi thí sinh có thể có nhiều dòng (nhiều đợt),
     *            chỉ lưu điểm cao nhất (Hướng A)
     *   - VSAT : sheet "VSAT" — mỗi dòng là 1 bài thi riêng (nhiều môn, nhiều đợt),
     *            gộp các môn của cùng CCCD vào 1 entity, lấy điểm cao nhất mỗi môn
     */
    public ImportResult importFromExcel(File file, String phuongThuc) throws Exception {
        String phuongThucCode = normalizePhuongThucCode(phuongThuc);

        if (isDgnl(phuongThucCode)) {
            return importDgnl(file, phuongThucCode);
        } else if (isVsat(phuongThucCode)) {
            return importVsat(file, phuongThucCode);
        } else {
            return importThpt(file, phuongThucCode);
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Import THPT
    //
    //  Cấu trúc cột "Ds thi sinh.xlsx" (sheet đầu tiên):
    //  A=STT | B=CCCD | C=Họ Tên |H=Toán(7) | I=Văn(8) | J=Lý(9) | K=Hóa(10) | L=Sinh(11) | M=Sử(12) | N=Địa(13)
    //  O=GDCD(14) | P=Điểm NN(15) | Q=Mã môn NN(16) | R=KTPL(17) | S=Tin(18) | T=CNCN(19) | U=CNNN(20)
    //  W=NK1(22) | X=NK2(23) | Y=NK3(24) | Z=NK4(25) | AA=NK5(26) | AB=NK6(27) 
    // ────────────────────────────────────────────────────────────
    private ImportResult importThpt(File file, String phuongThucCode) throws Exception {
        ImportResult result = new ImportResult();

        List<DiemThiXetTuyen> list = ImportUtil.readExcel(file, row -> {
            // Cột B (index 1) = CCCD
            String cccd = ImportUtil.getString(row, 1);
            if (cccd.isEmpty()) return null;

            DiemThiXetTuyen d = new DiemThiXetTuyen();
            d.setCccd(cccd);
            d.setdPhuongthuc(phuongThucCode);

            // Cột C (index 2) = SBD (số báo danh, trong file THPT là Họ Tên — bỏ qua)
            // Không có SBD riêng trong file THPT, để null

            // Điểm các môn cơ bản (cột H–N, index 7–13)
            d.setTo(ImportUtil.getDecimal(row, 7));   // Toán
            d.setVa(ImportUtil.getDecimal(row, 8));   // Văn
            d.setLi(ImportUtil.getDecimal(row, 9));   // Lý
            d.setHo(ImportUtil.getDecimal(row, 10));  // Hóa
            d.setSi(ImportUtil.getDecimal(row, 11));  // Sinh
            d.setSu(ImportUtil.getDecimal(row, 12));  // Sử
            d.setDi(ImportUtil.getDecimal(row, 13));  // Địa

            // Cột O (index 14) = GDCD
            d.setGdcd(ImportUtil.getDecimal(row, 14));

            // Cột P (index 15) = Điểm Ngoại ngữ (thi)
            // Cột Q (index 16) = Mã môn NN — dùng để xác định môn nhưng đều lưu vào n1Thi
            d.setN1Thi(ImportUtil.getDecimal(row, 15));

            // Cột R (index 17) = KTPL
            d.setKtpl(ImportUtil.getDecimal(row, 17));

            // Cột S (index 18) = Tin học
            d.setTi(ImportUtil.getDecimal(row, 18));

            // Cột T (index 19) = CNCN
            d.setCncn(ImportUtil.getDecimal(row, 19));

            // Cột U (index 20) = CNNN
            d.setCnnn(ImportUtil.getDecimal(row, 20));

            // Cột V (index 21) = Chương trình học — bỏ qua (không có field)

            // Cột W–AB (index 22–27) = NK1–NK6
            d.setNk1(ImportUtil.getDecimal(row, 22));
            d.setNk2(ImportUtil.getDecimal(row, 23));
            d.setNk3(ImportUtil.getDecimal(row, 24));
            d.setNk4(ImportUtil.getDecimal(row, 25));
            d.setNk5(ImportUtil.getDecimal(row, 26));
            d.setNk6(ImportUtil.getDecimal(row, 27));

            prepareForSave(d);
            return d;
        });

        // Upsert từng bản ghi: cập nhật nếu đã tồn tại, thêm mới nếu chưa có
        for (DiemThiXetTuyen d : list) {
            try {
                DiemThiXetTuyen existing = diemThiDAO.findByCccdAndPhuongThuc(d.getCccd(), d.getdPhuongthuc());
                if (existing != null) {
                    d.setIddiemthi(existing.getIddiemthi());
                    diemThiDAO.update(d);
                    result.updateCount++;
                } else {
                    diemThiDAO.save(d);
                    result.insertCount++;
                }
            } catch (Exception e) {
                result.errorCount++;
                result.errors.add("CCCD " + d.getCccd() + ": " + e.getMessage());
            }
        }

        return result;
    }

    // ────────────────────────────────────────────────────────────
    //  Import ĐGNL — Hướng A: chỉ lưu điểm cao nhất
    //
    //  Cấu trúc file "Diem DGNL VSAT - 0908.xls", sheet "DGNL":
    //  A=STT | B=CMND | C=DOTHI | D=MADOTHI | E=NGAYTHI | F=NAMTHI
    //  G=MAMONTHI | H=TENMONTH | I=DIEM | J=THANGDIEM | K=MADVTCTDL | L=TENDVTCTDL
    //
    //  Một thí sinh có thể có 2 dòng (2 đợt thi). Chỉ lưu điểm cao nhất.
    //  Xử lý: đọc hết file → group theo CCCD → lấy max(DIEM) → upsert vào DB.
    // ────────────────────────────────────────────────────────────
    private ImportResult importDgnl(File file, String phuongThucCode) throws Exception {
        ImportResult result = new ImportResult();

        // Đọc tất cả dòng từ sheet "DGNL"
        List<DiemThiXetTuyen> rawList = ImportUtil.readExcel(file, "DGNL", row -> {
            // Cột B (index 1) = CMND/CCCD
            String cccd = ImportUtil.getString(row, 1);
            if (cccd.isEmpty()) return null;

            DiemThiXetTuyen d = new DiemThiXetTuyen();
            d.setCccd(cccd);
            d.setdPhuongthuc(phuongThucCode);

            // Cột C (index 2) = Đợt thi (số thứ tự đợt) — dùng làm SBD tạm
            d.setSobaodanh(ImportUtil.getString(row, 2));

            // Cột I (index 8) = Điểm ĐGNL (thang điểm 1200)
            d.setNl1(ImportUtil.getDecimal(row, 8));

            return d;
        });

        // Group theo CCCD, lấy điểm cao nhất trong các đợt thi
        Map<String, DiemThiXetTuyen> bestMap = new LinkedHashMap<>();
        for (DiemThiXetTuyen d : rawList) {
            String key = d.getCccd();
            if (!bestMap.containsKey(key)) {
                bestMap.put(key, d);
            } else {
                // So sánh điểm, giữ lại bản ghi có điểm cao hơn
                DiemThiXetTuyen existing = bestMap.get(key);
                BigDecimal currentBest = existing.getNl1() != null ? existing.getNl1() : BigDecimal.ZERO;
                BigDecimal challenger  = d.getNl1()        != null ? d.getNl1()        : BigDecimal.ZERO;
                if (challenger.compareTo(currentBest) > 0) {
                    bestMap.put(key, d);
                }
                result.skipCount++; // Đợt thi thấp hơn bị bỏ qua
            }
        }

        // Upsert danh sách điểm cao nhất vào DB
        for (DiemThiXetTuyen d : bestMap.values()) {
            try {
                prepareForSave(d);
                DiemThiXetTuyen dbRecord = diemThiDAO.findByCccdAndPhuongThuc(d.getCccd(), d.getdPhuongthuc());
                if (dbRecord != null) {
                    d.setIddiemthi(dbRecord.getIddiemthi());
                    diemThiDAO.update(d);
                    result.updateCount++;
                } else {
                    diemThiDAO.save(d);
                    result.insertCount++;
                }
            } catch (Exception e) {
                result.errorCount++;
                result.errors.add("CCCD " + d.getCccd() + ": " + e.getMessage());
            }
        }

        return result;
    }

    // ────────────────────────────────────────────────────────────
    //  Import VSAT — Nhiều dòng/thí sinh, nhiều môn, nhiều đợt
    //
    //  Cấu trúc file "Diem DGNL VSAT - 0908.xls", sheet "VSAT":
    //  A=STT | B=CMND | C=DOTHI | D=MADOTHI | E=NGAYTHI | F=NAMTHI
    //  G=MAMONTHI | H=TENMONHI | I=DIEM | J=THANGDIEM | K=MADVTCTDL | L=TENDVTCTDL
    //
    //  Mã môn thi (cột G): TO_VS=Toán, VA_VS=Văn, LI_VS=Lý, HO_VS=Hóa,
    //                       SI_VS=Sinh, SU_VS=Sử, DI_VS=Địa, N1_VS=Anh
    //                       M1=Toán, M2=Vật lý, M3=Hóa, ... (HUBSA format)
    //
    //  Xử lý: đọc hết file → group theo CCCD → map từng môn → lấy điểm cao nhất
    //         mỗi môn nếu thi ở nhiều đợt/trường → upsert 1 entity/thí sinh.
    // ────────────────────────────────────────────────────────────
    private ImportResult importVsat(File file, String phuongThucCode) throws Exception {
        ImportResult result = new ImportResult();

        // Đọc toàn bộ dòng thô từ sheet "VSAT"
        List<String[]> rawRows = ImportUtil.readExcel(file, "VSAT", row -> {
            String cccd    = ImportUtil.getString(row, 1); // Cột B = CMND
            String maMon   = ImportUtil.getString(row, 6); // Cột G = Mã môn thi
            String diemStr = ImportUtil.getString(row, 8); // Cột I = Điểm

            if (cccd.isEmpty() || maMon.isEmpty()) return null;
            return new String[]{cccd, maMon, diemStr};
        });

        // Group theo CCCD, tích lũy điểm cao nhất mỗi môn
        // Key = CCCD, Value = entity đang được xây dựng
        Map<String, DiemThiXetTuyen> entityMap = new LinkedHashMap<>();

        for (String[] row : rawRows) {
            String cccd    = row[0];
            String maMon   = row[1].toUpperCase(Locale.ROOT).trim();
            BigDecimal diem = parseBigDecimalSafe(row[2]);

            // Lấy hoặc tạo entity cho CCCD này
            DiemThiXetTuyen d = entityMap.computeIfAbsent(cccd, k -> {
                DiemThiXetTuyen entity = new DiemThiXetTuyen();
                entity.setCccd(k);
                entity.setdPhuongthuc(phuongThucCode);
                return entity;
            });

            // Map mã môn → field tương ứng, lấy điểm cao nhất nếu thi nhiều lần
            setVsatMonDiem(d, maMon, diem);
        }

        // Upsert từng entity vào DB
        for (DiemThiXetTuyen d : entityMap.values()) {
            try {
                clearVietSatUnsupportedScores(d); // Xóa các field không dùng cho VSAT
                d.setNl1(null);

                DiemThiXetTuyen dbRecord = diemThiDAO.findByCccdAndPhuongThuc(d.getCccd(), d.getdPhuongthuc());
                if (dbRecord != null) {
                    d.setIddiemthi(dbRecord.getIddiemthi());
                    diemThiDAO.update(d);
                    result.updateCount++;
                } else {
                    diemThiDAO.save(d);
                    result.insertCount++;
                }
            } catch (Exception e) {
                result.errorCount++;
                result.errors.add("CCCD " + d.getCccd() + ": " + e.getMessage());
            }
        }

        return result;
    }

    // ════════════════════════════════════════════════════════════
    //  THỐNG KÊ
    // ════════════════════════════════════════════════════════════

    public ThongKe thongKe(List<DiemThiXetTuyen> list, String phuongThuc) {
        String phuongThucCode = normalizePhuongThucCode(phuongThuc);
        ThongKe tk = new ThongKe();
        if (list == null || list.isEmpty()) {
            return tk;
        }

        BigDecimal tong = BigDecimal.ZERO;
        BigDecimal cao = null;
        BigDecimal thap = null;
        int count = 0;

        for (DiemThiXetTuyen d : list) {
            BigDecimal diem = getDiemDaiDien(d, phuongThucCode);
            if (diem == null) {
                continue;
            }
            count++;
            tong = tong.add(diem);
            if (cao == null || diem.compareTo(cao) > 0) {
                cao = diem;
            }
            if (thap == null || diem.compareTo(thap) < 0) {
                thap = diem;
            }
        }

        if (count > 0) {
            tk.diemTB = tong.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            tk.caoNhat = cao;
            tk.thapNhat = thap;
        }
        return tk;
    }

    // ════════════════════════════════════════════════════════════
    //  TIỆN ÍCH ĐIỂM MÔN
    // ════════════════════════════════════════════════════════════

    public BigDecimal getDiemMon(DiemThiXetTuyen d, String tenMon) {
        if (d == null || tenMon == null) {
            return BigDecimal.ZERO;
        }

        switch (tenMon.toUpperCase(Locale.ROOT)) {
            case "TO":
                return nvl(d.getTo());
            case "VA":
                return nvl(d.getVa());
            case "LI":
                return nvl(d.getLi());
            case "HO":
                return nvl(d.getHo());
            case "SI":
                return nvl(d.getSi());
            case "SU":
                return nvl(d.getSu());
            case "DI":
                return nvl(d.getDi());
            case "GDCD":
                return nvl(d.getGdcd());
            case "N1":
                return nvl(preferNgoaiNguScore(d));
            case "KTPL":
                return nvl(d.getKtpl());
            case "TI":
                return nvl(d.getTi());
            case "CNCN":
                return nvl(d.getCncn());
            case "CNNN":
                return nvl(d.getCnnn());
            case "NK1":
                return nvl(d.getNk1());
            case "NK2":
                return nvl(d.getNk2());
            case "NK3":
                return nvl(d.getNk3());
            case "NK4":
                return nvl(d.getNk4());
            case "NK5":
                return nvl(d.getNk5());
            case "NK6":
                return nvl(d.getNk6());
            default:
                return BigDecimal.ZERO;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  TIỆN ÍCH PHƯƠNG THỨC THI
    // ════════════════════════════════════════════════════════════

    public static String normalizePhuongThucCode(String phuongThuc) {
        if (phuongThuc == null) {
            throw new IllegalArgumentException("Phuong thuc khong duoc de trong.");
        }

        String value = phuongThuc.trim().toUpperCase(Locale.ROOT);
        switch (value) {
            case "1":
            case "THPT":
            case "PT2":
                return PHUONG_THUC_CODE_THPT;
            case "4":
            case "DGNL":
                return PHUONG_THUC_CODE_DGNL;
            case "5":
            case "VSAT":
            case "V-SAT":
                return PHUONG_THUC_CODE_VSAT;
            default:
                throw new IllegalArgumentException("Phuong thuc diem thi khong hop le: " + phuongThuc);
        }
    }

    public static String getPhuongThucLabel(String phuongThuc) {
        String code = normalizePhuongThucCode(phuongThuc);
        switch (code) {
            case PHUONG_THUC_CODE_DGNL:
                return PHUONG_THUC_DGNL;
            case PHUONG_THUC_CODE_VSAT:
                return PHUONG_THUC_VSAT;
            default:
                return PHUONG_THUC_THPT;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  PRIVATE — HỖ TRỢ NỘI BỘ
    // ════════════════════════════════════════════════════════════

    // Thêm mapping alias môn học → tên field entity
    private static void addMonMapping(String alias, String... fieldNames) {
        MON_FIELD_MAP.put(alias, Arrays.asList(fieldNames));
    }

    // Chuẩn hóa từ khóa tìm kiếm: bỏ dấu, lowercase, trim
    private static String normalizeKeyword(String keyword) {
        String normalized = Normalizer.normalize(keyword, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .toLowerCase(Locale.ROOT)
            .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    // Tìm các field entity khớp với từ khóa tìm kiếm môn học
    private List<String> findMatchingSubjectFields(String keyword, String phuongThucCode) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> matchedFields = new LinkedHashSet<>();
        Set<String> allowedFields = getAllowedSubjectFields(phuongThucCode);

        for (Map.Entry<String, List<String>> entry : MON_FIELD_MAP.entrySet()) {
            if (entry.getKey().contains(keyword) || keyword.contains(entry.getKey())) {
                for (String field : entry.getValue()) {
                    if (allowedFields.contains(field)) {
                        matchedFields.add(field);
                    }
                }
            }
        }

        return new ArrayList<>(matchedFields);
    }

    // Trả về tập hợp các field điểm được phép tìm kiếm theo phương thức
    private Set<String> getAllowedSubjectFields(String phuongThucCode) {
        Set<String> fields = new LinkedHashSet<>(Arrays.asList(
            "to", "va", "li", "ho", "si", "su", "di", "n1Thi"
        ));

        if (!isVsat(phuongThucCode)) {
            fields.addAll(Arrays.asList(
                "gdcd", "n1Cc", "cncn", "cnnn", "ti", "ktpl",
                "nk1", "nk2", "nk3", "nk4", "nk5", "nk6"
            ));
        }
        return fields;
    }

    // Chuẩn bị entity trước khi lưu: trim, chuẩn hóa phương thức, xóa field không dùng
    private void prepareForSave(DiemThiXetTuyen entity) {
        entity.setCccd(entity.getCccd() != null ? entity.getCccd().trim() : null);
        entity.setSobaodanh(trimToNull(entity.getSobaodanh()));
        entity.setdPhuongthuc(normalizePhuongThucCode(entity.getdPhuongthuc()));

        if (isDgnl(entity.getdPhuongthuc())) {
            clearAcademicScores(entity);
        } else if (isVsat(entity.getdPhuongthuc())) {
            clearVietSatUnsupportedScores(entity);
            entity.setNl1(null);
        } else {
            entity.setNl1(null);
        }
    }

    // Kiểm tra dữ liệu bắt buộc trước khi lưu
    private void validate(DiemThiXetTuyen entity) {
        if (entity.getCccd() == null || entity.getCccd().isEmpty()) {
            throw new RuntimeException("CCCD khong duoc de trong!");
        }
        if (entity.getdPhuongthuc() == null || entity.getdPhuongthuc().isEmpty()) {
            throw new RuntimeException("Phuong thuc thi khong duoc de trong!");
        }
    }

    // Điền điểm VSAT vào đúng field, giữ lại điểm cao hơn nếu thi nhiều lần
    private void setVsatMonDiem(DiemThiXetTuyen d, String maMon, BigDecimal diem) {
        if (diem == null) return;

        switch (maMon) {
            // Format chuẩn VSAT (hậu tố _VS)
            case "TO_VS": case "M1":  // Toán
                d.setTo(maxBigDecimal(d.getTo(), diem));
                break;
            case "VA_VS":             // Văn
                d.setVa(maxBigDecimal(d.getVa(), diem));
                break;
            case "LI_VS": case "M2":  // Vật lý
                d.setLi(maxBigDecimal(d.getLi(), diem));
                break;
            case "HO_VS": case "M3":  // Hóa học
                d.setHo(maxBigDecimal(d.getHo(), diem));
                break;
            case "SI_VS": case "M4":  // Sinh học
                d.setSi(maxBigDecimal(d.getSi(), diem));
                break;
            case "SU_VS": case "M5":  // Lịch sử
                d.setSu(maxBigDecimal(d.getSu(), diem));
                break;
            case "DI_VS": case "M6":  // Địa lý
                d.setDi(maxBigDecimal(d.getDi(), diem));
                break;
            case "N1_VS": case "M7":  // Tiếng Anh VSAT
                d.setN1Thi(maxBigDecimal(d.getN1Thi(), diem));
                break;
            // Bỏ qua mã môn không nhận ra
            default:
                break;
        }
    }

    // Trả về giá trị lớn hơn trong 2 BigDecimal (null được coi là 0)
    private BigDecimal maxBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) >= 0 ? a : b;
    }

    // Parse chuỗi thành BigDecimal, trả về null nếu không hợp lệ
    private BigDecimal parseBigDecimalSafe(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            return new BigDecimal(raw.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Xóa tất cả điểm học tập (dùng cho ĐGNL, chỉ lưu nl1)
    private void clearAcademicScores(DiemThiXetTuyen entity) {
        entity.setTo(null);
        entity.setVa(null);
        entity.setLi(null);
        entity.setHo(null);
        entity.setSi(null);
        entity.setSu(null);
        entity.setDi(null);
        entity.setGdcd(null);
        entity.setN1Thi(null);
        entity.setN1Cc(null);
        entity.setCncn(null);
        entity.setCnnn(null);
        entity.setTi(null);
        entity.setKtpl(null);
        entity.setNk1(null);
        entity.setNk2(null);
        entity.setNk3(null);
        entity.setNk4(null);
        entity.setNk5(null);
        entity.setNk6(null);
    }

    // Xóa các field điểm không áp dụng cho VSAT
    private void clearVietSatUnsupportedScores(DiemThiXetTuyen entity) {
        entity.setGdcd(null);
        entity.setN1Cc(null);
        entity.setCncn(null);
        entity.setCnnn(null);
        entity.setTi(null);
        entity.setKtpl(null);
        entity.setNk1(null);
        entity.setNk2(null);
        entity.setNk3(null);
        entity.setNk4(null);
        entity.setNk5(null);
        entity.setNk6(null);
    }

    // Tính điểm đại diện để thống kê (cao nhất, thấp nhất, trung bình)
    private BigDecimal getDiemDaiDien(DiemThiXetTuyen d, String phuongThucCode) {
        if (isDgnl(phuongThucCode)) {
            return d.getNl1();
        }
        if (isVsat(phuongThucCode)) {
            return calculateRepresentativeAcademicScore(d, true);
        }
        return calculateRepresentativeAcademicScore(d, false);
    }

    // Tính điểm tổng đại diện: Toán + Văn + 2 môn cao nhất còn lại
    private BigDecimal calculateRepresentativeAcademicScore(DiemThiXetTuyen d, boolean vsatOnly) {
        List<BigDecimal> optionalScores = new ArrayList<>();
        addIfPositive(optionalScores, d.getLi());
        addIfPositive(optionalScores, d.getHo());
        addIfPositive(optionalScores, d.getSi());
        addIfPositive(optionalScores, d.getSu());
        addIfPositive(optionalScores, d.getDi());
        addIfPositive(optionalScores, vsatOnly ? d.getN1Thi() : preferNgoaiNguScore(d));

        if (!vsatOnly) {
            addIfPositive(optionalScores, d.getGdcd());
            addIfPositive(optionalScores, d.getKtpl());
            addIfPositive(optionalScores, d.getTi());
            addIfPositive(optionalScores, d.getCncn());
            addIfPositive(optionalScores, d.getCnnn());
            addIfPositive(optionalScores, d.getNk1());
            addIfPositive(optionalScores, d.getNk2());
            addIfPositive(optionalScores, d.getNk3());
            addIfPositive(optionalScores, d.getNk4());
            addIfPositive(optionalScores, d.getNk5());
            addIfPositive(optionalScores, d.getNk6());
        }

        optionalScores.sort(Comparator.reverseOrder());

        BigDecimal tong = BigDecimal.ZERO;
        int soMonCanBoSung = 4;

        if (isPositive(d.getTo())) {
            tong = tong.add(d.getTo());
            soMonCanBoSung--;
        }
        if (isPositive(d.getVa())) {
            tong = tong.add(d.getVa());
            soMonCanBoSung--;
        }

        for (int i = 0; i < Math.min(soMonCanBoSung, optionalScores.size()); i++) {
            tong = tong.add(optionalScores.get(i));
        }

        return tong.compareTo(BigDecimal.ZERO) > 0 ? tong : null;
    }

    private void addIfPositive(List<BigDecimal> list, BigDecimal value) {
        if (isPositive(value)) {
            list.add(value);
        }
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    // Ưu tiên dùng điểm chứng chỉ ngoại ngữ nếu có, ngược lại dùng điểm thi
    private BigDecimal preferNgoaiNguScore(DiemThiXetTuyen d) {
        if (isPositive(d.getN1Cc())) {
            return d.getN1Cc();
        }
        return d.getN1Thi();
    }

    private boolean isDgnl(String phuongThucCode) {
        return PHUONG_THUC_CODE_DGNL.equals(phuongThucCode);
    }

    private boolean isVsat(String phuongThucCode) {
        return PHUONG_THUC_CODE_VSAT.equals(phuongThucCode);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Trả về BigDecimal.ZERO nếu value là null (tránh NullPointerException khi tính toán)
    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // ════════════════════════════════════════════════════════════
    //  INNER CLASS — KẾT QUẢ THỐNG KÊ
    // ════════════════════════════════════════════════════════════

    public static class ThongKe {
        public BigDecimal diemTB;
        public BigDecimal caoNhat;
        public BigDecimal thapNhat;
    }
}