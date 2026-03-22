package com.xettuyen2026.service;

import com.xettuyen2026.dao.DiemThiDAO;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.util.ImportUtil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiemThiService {

    private final DiemThiDAO diemThiDAO;

    // Map tu khoa nguoi dung nhap -> ten field trong entity
    private static final Map<String, String> MON_FIELD_MAP = new HashMap<>();
    static {
        MON_FIELD_MAP.put("toán",       "to");
        MON_FIELD_MAP.put("toan",       "to");
        MON_FIELD_MAP.put("văn",        "va");
        MON_FIELD_MAP.put("van",        "va");
        MON_FIELD_MAP.put("lý",         "li");
        MON_FIELD_MAP.put("ly",         "li");
        MON_FIELD_MAP.put("hóa",        "ho");
        MON_FIELD_MAP.put("hoa",        "ho");
        MON_FIELD_MAP.put("sinh",       "si");
        MON_FIELD_MAP.put("sử",         "su");
        MON_FIELD_MAP.put("su",         "su");
        MON_FIELD_MAP.put("địa",        "di");
        MON_FIELD_MAP.put("dia",        "di");
        MON_FIELD_MAP.put("gdcd",       "gdcd");
        MON_FIELD_MAP.put("ngoại ngữ",  "n1Thi");
        MON_FIELD_MAP.put("ngoai ngu",  "n1Thi");
        MON_FIELD_MAP.put("anh",        "n1Thi");
        MON_FIELD_MAP.put("tin",        "ti");
        MON_FIELD_MAP.put("ktpl",       "ktpl");
        MON_FIELD_MAP.put("cncn",       "cncn");
        MON_FIELD_MAP.put("cnnn",       "cnnn");
        MON_FIELD_MAP.put("nk1",        "nkDiem1");
        MON_FIELD_MAP.put("nk2",        "nkDiem2");
        MON_FIELD_MAP.put("năng khiếu", "nkDiem1");
        MON_FIELD_MAP.put("nang khieu", "nkDiem1");
    }

    public DiemThiService() {
        this.diemThiDAO = new DiemThiDAO();
    }

    // Xem va tim kiem
    public List<DiemThiXetTuyen> findAll() {
        return diemThiDAO.findAll();
    }

    public List<DiemThiXetTuyen> findByPhuongThuc(String phuongThuc) {
        return diemThiDAO.findByPhuongThuc(phuongThuc);
    }

    public DiemThiXetTuyen findByCccd(String cccd) {
        return diemThiDAO.findByCccd(cccd);
    }

    public List<DiemThiXetTuyen> search(String keyword, String phuongThuc) {
        if (keyword == null || keyword.trim().isEmpty())
            return findByPhuongThuc(phuongThuc);

        String kw = keyword.trim().toLowerCase();

        // Uu tien tim theo ten mon truoc
        for (Map.Entry<String, String> entry : MON_FIELD_MAP.entrySet()) {
            if (entry.getKey().contains(kw) || kw.contains(entry.getKey())) {
                return diemThiDAO.searchByMon(entry.getValue(), phuongThuc);
            }
        }

        // Neu khong khop mon nao thi tim theo CCCD bang LIKE %kw%
        return diemThiDAO.searchByCccd(kw, phuongThuc);
    }

    // Them moi diem thi
    public void save(DiemThiXetTuyen entity) {
        validate(entity);
        if (diemThiDAO.existsByCccd(entity.getCccd()))
            throw new RuntimeException("CCCD '" + entity.getCccd() + "' da ton tai!");
        diemThiDAO.save(entity);
    }

    // Sua diem thi
    public void update(DiemThiXetTuyen entity) {
        validate(entity);
        diemThiDAO.update(entity);
    }

    // Xoa diem thi
    public void delete(DiemThiXetTuyen entity) {
        diemThiDAO.delete(entity);
    }

    // Import tu Excel
    // THPT: STT(0) CCCD(1) HoTen(2) NgaySinh(3) GioiTinh(4) DTUT(5) KVUT(6)
    //       TO(7) VA(8) LI(9) HO(10) SI(11) SU(12) DI(13) GDCD(14)
    //       NN_Diem(15) NN_Ten(16) KTPL(17) TI(18) CNCN(19) CNNN(20)
    //       ChuongTrinhHoc(21) NK_Mon1(22) NK_Diem1(23) NK_Mon2(24) NK_Diem2(25)
    // DGNL: CCCD(0) SBD(1) NL1(2)
    // VSAT: TODO - chua co cau truc
    public int importFromExcel(File file, String phuongThuc) throws Exception {
        List<DiemThiXetTuyen> list = ImportUtil.readExcel(file, row -> {
            String cccd = ImportUtil.getString(row, 1);
            if (cccd.isEmpty()) return null;

            DiemThiXetTuyen d = new DiemThiXetTuyen();
            d.setCccd(cccd);
            d.setdPhuongthuc(phuongThuc);

            if ("DGNL".equals(phuongThuc)) {
                d.setSobaodanh(ImportUtil.getString(row, 1));
                d.setNl1(ImportUtil.getDecimal(row, 2));

            } else if ("VSAT".equals(phuongThuc)) {
                // TODO: chua co cau truc VSAT

            } else { // THPT
                d.setTo(ImportUtil.getDecimal(row, 7));
                d.setVa(ImportUtil.getDecimal(row, 8));
                d.setLi(ImportUtil.getDecimal(row, 9));
                d.setHo(ImportUtil.getDecimal(row, 10));
                d.setSi(ImportUtil.getDecimal(row, 11));
                d.setSu(ImportUtil.getDecimal(row, 12));
                d.setDi(ImportUtil.getDecimal(row, 13));
                d.setGdcd(ImportUtil.getDecimal(row, 14));
                d.setN1Thi(ImportUtil.getDecimal(row, 15));
                // Cot 16: Ten mon NN - bo qua
                d.setKtpl(ImportUtil.getDecimal(row, 17));
                d.setTi(ImportUtil.getDecimal(row, 18));
                d.setCncn(ImportUtil.getDecimal(row, 19));
                d.setCnnn(ImportUtil.getDecimal(row, 20));
                // Cot 21: Chuong trinh hoc - bo qua
                String nkMon1  = ImportUtil.getString(row, 22);
                BigDecimal nkDiem1 = ImportUtil.getDecimal(row, 23);
                String nkMon2  = ImportUtil.getString(row, 24);
                BigDecimal nkDiem2 = ImportUtil.getDecimal(row, 25);
                if (!nkMon1.isEmpty()) { d.setNkMon1(nkMon1); d.setNkDiem1(nkDiem1); }
                if (!nkMon2.isEmpty()) { d.setNkMon2(nkMon2); d.setNkDiem2(nkDiem2); }
            }
            return d;
        });

        // Upsert: cap nhat neu da co, them moi neu chua co
        for (DiemThiXetTuyen d : list) {
            if (diemThiDAO.existsByCccd(d.getCccd())) diemThiDAO.update(d);
            else diemThiDAO.save(d);
        }
        return list.size();
    }

    // Thong ke toan bo theo phuong thuc - dung cho stats co dinh
    // THPT: Toan + Van + 2 mon cao nhat con lai
    // DGNL: NL1
    // VSAT: TODO
    public ThongKe thongKe(List<DiemThiXetTuyen> list, String phuongThuc) {
        ThongKe tk = new ThongKe();
        if (list == null || list.isEmpty()) return tk;

        BigDecimal tong = BigDecimal.ZERO;
        BigDecimal cao  = null, thap = null;
        int count = 0;

        for (DiemThiXetTuyen d : list) {
            BigDecimal diem = getDiemDaiDien(d, phuongThuc);
            if (diem == null) continue;
            count++;
            tong = tong.add(diem);
            if (cao  == null || diem.compareTo(cao)  > 0) cao  = diem;
            if (thap == null || diem.compareTo(thap) < 0) thap = diem;
        }

        if (count > 0) {
            tk.diemTB   = tong.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            tk.caoNhat  = cao;
            tk.thapNhat = thap;
        }
        return tk;
    }

    // Lay diem 1 mon theo ma mon - dung cho tinh diem to hop o nguyen vong
    public BigDecimal getDiemMon(DiemThiXetTuyen d, String tenMon) {
        if (d == null || tenMon == null) return BigDecimal.ZERO;
        switch (tenMon.toUpperCase()) {
            case "TO":   return nvl(d.getTo());
            case "VA":   return nvl(d.getVa());
            case "LI":   return nvl(d.getLi());
            case "HO":   return nvl(d.getHo());
            case "SI":   return nvl(d.getSi());
            case "SU":   return nvl(d.getSu());
            case "DI":   return nvl(d.getDi());
            case "GDCD": return nvl(d.getGdcd());
            case "N1":
                BigDecimal cc = nvl(d.getN1Cc());
                return cc.compareTo(BigDecimal.ZERO) > 0 ? cc : nvl(d.getN1Thi());
            case "KTPL": return nvl(d.getKtpl());
            case "TI":   return nvl(d.getTi());
            case "CNCN": return nvl(d.getCncn());
            case "CNNN": return nvl(d.getCnnn());
            case "NK1":  return nvl(d.getNkDiem1());
            case "NK2":  return nvl(d.getNkDiem2());
            default:     return BigDecimal.ZERO;
        }
    }

    // Validate
    private void validate(DiemThiXetTuyen entity) {
        if (entity.getCccd() == null || entity.getCccd().trim().isEmpty())
            throw new RuntimeException("CCCD khong duoc de trong!");
        if (entity.getdPhuongthuc() == null || entity.getdPhuongthuc().trim().isEmpty())
            throw new RuntimeException("Phuong thuc thi khong duoc de trong!");
    }

    // Diem dai dien de tinh thong ke
    private BigDecimal getDiemDaiDien(DiemThiXetTuyen d, String phuongThuc) {
        switch (phuongThuc) {
            case "DGNL": return d.getNl1();
            case "VSAT": return null; // TODO
            default: { // THPT
                BigDecimal toan = nvl(d.getTo());
                BigDecimal van  = nvl(d.getVa());
                if (toan.compareTo(BigDecimal.ZERO) == 0
                        && van.compareTo(BigDecimal.ZERO) == 0) return null;

                List<BigDecimal> tuChon = new ArrayList<>();
                addIfPositive(tuChon, d.getLi());
                addIfPositive(tuChon, d.getHo());
                addIfPositive(tuChon, d.getSi());
                addIfPositive(tuChon, d.getSu());
                addIfPositive(tuChon, d.getDi());
                addIfPositive(tuChon, d.getGdcd());
                addIfPositive(tuChon, d.getN1Cc() != null
                        && d.getN1Cc().compareTo(BigDecimal.ZERO) > 0
                        ? d.getN1Cc() : d.getN1Thi());
                addIfPositive(tuChon, d.getKtpl());
                addIfPositive(tuChon, d.getTi());
                addIfPositive(tuChon, d.getCncn());
                addIfPositive(tuChon, d.getCnnn());
                addIfPositive(tuChon, d.getNkDiem1());
                addIfPositive(tuChon, d.getNkDiem2());

                tuChon.sort(Comparator.reverseOrder());
                BigDecimal tong = toan.add(van);
                for (int i = 0; i < Math.min(2, tuChon.size()); i++) {
                    tong = tong.add(tuChon.get(i));
                }
                return tong;
            }
        }
    }

    private void addIfPositive(List<BigDecimal> list, BigDecimal val) {
        if (val != null && val.compareTo(BigDecimal.ZERO) > 0) list.add(val);
    }

    private BigDecimal nvl(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    // Ket qua thong ke
    public static class ThongKe {
        public BigDecimal diemTB, caoNhat, thapNhat;
    }
}