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

    public int importFromExcel(File file, String phuongThuc) throws Exception {
        String phuongThucCode = normalizePhuongThucCode(phuongThuc);

        List<DiemThiXetTuyen> list = ImportUtil.readExcel(file, row -> {
            String cccd = isDgnl(phuongThucCode) ? ImportUtil.getString(row, 0) : ImportUtil.getString(row, 1);
            if (cccd.isEmpty()) {
                return null;
            }

            DiemThiXetTuyen d = new DiemThiXetTuyen();
            d.setCccd(cccd);
            d.setdPhuongthuc(phuongThucCode);

            if (isDgnl(phuongThucCode)) {
                d.setSobaodanh(ImportUtil.getString(row, 1));
                d.setNl1(ImportUtil.getDecimal(row, 2));
            } else if (isVsat(phuongThucCode)) {
                populateVsatScores(d, row);
            } else {
                populateThptScores(d, row);
            }

            prepareForSave(d);
            return d;
        });

        for (DiemThiXetTuyen d : list) {
            DiemThiXetTuyen existing = diemThiDAO.findByCccdAndPhuongThuc(d.getCccd(), d.getdPhuongthuc());
            if (existing != null) {
                d.setIddiemthi(existing.getIddiemthi());
                diemThiDAO.update(d);
            } else {
                diemThiDAO.save(d);
            }
        }
        return list.size();
    }

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

    private static void addMonMapping(String alias, String... fieldNames) {
        MON_FIELD_MAP.put(alias, Arrays.asList(fieldNames));
    }

    private static String normalizeKeyword(String keyword) {
        String normalized = Normalizer.normalize(keyword, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .toLowerCase(Locale.ROOT)
            .trim();
        return normalized.replaceAll("\\s+", " ");
    }

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

    private void validate(DiemThiXetTuyen entity) {
        if (entity.getCccd() == null || entity.getCccd().isEmpty()) {
            throw new RuntimeException("CCCD khong duoc de trong!");
        }
        if (entity.getdPhuongthuc() == null || entity.getdPhuongthuc().isEmpty()) {
            throw new RuntimeException("Phuong thuc thi khong duoc de trong!");
        }
    }

    private void populateThptScores(DiemThiXetTuyen d, org.apache.poi.ss.usermodel.Row row) {
        populateCommonAcademicScores(d, row);
        d.setGdcd(ImportUtil.getDecimal(row, 14));
        d.setKtpl(ImportUtil.getDecimal(row, 17));
        d.setTi(ImportUtil.getDecimal(row, 18));
        d.setCncn(ImportUtil.getDecimal(row, 19));
        d.setCnnn(ImportUtil.getDecimal(row, 20));
        d.setNk1(ImportUtil.getDecimal(row, 22));
        d.setNk2(ImportUtil.getDecimal(row, 23));
        d.setNk3(ImportUtil.getDecimal(row, 24));
        d.setNk4(ImportUtil.getDecimal(row, 25));
        d.setNk5(ImportUtil.getDecimal(row, 26));
        d.setNk6(ImportUtil.getDecimal(row, 27));
    }

    private void populateVsatScores(DiemThiXetTuyen d, org.apache.poi.ss.usermodel.Row row) {
        populateCommonAcademicScores(d, row);
        clearVietSatUnsupportedScores(d);
    }

    private void populateCommonAcademicScores(DiemThiXetTuyen d, org.apache.poi.ss.usermodel.Row row) {
        d.setTo(ImportUtil.getDecimal(row, 7));
        d.setVa(ImportUtil.getDecimal(row, 8));
        d.setLi(ImportUtil.getDecimal(row, 9));
        d.setHo(ImportUtil.getDecimal(row, 10));
        d.setSi(ImportUtil.getDecimal(row, 11));
        d.setSu(ImportUtil.getDecimal(row, 12));
        d.setDi(ImportUtil.getDecimal(row, 13));
        d.setN1Thi(ImportUtil.getDecimal(row, 15));
    }

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

    private BigDecimal getDiemDaiDien(DiemThiXetTuyen d, String phuongThucCode) {
        if (isDgnl(phuongThucCode)) {
            return d.getNl1();
        }
        if (isVsat(phuongThucCode)) {
            return calculateRepresentativeAcademicScore(d, true);
        }
        return calculateRepresentativeAcademicScore(d, false);
    }

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

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public static class ThongKe {
        public BigDecimal diemTB;
        public BigDecimal caoNhat;
        public BigDecimal thapNhat;
    }
}
