package com.xettuyen2026.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.xettuyen2026.dao.BangQuydoiDAO;
import com.xettuyen2026.dao.DiemThiDAO;
import com.xettuyen2026.dao.NganhDAO;
import com.xettuyen2026.dao.NganhTohopDAO;
import com.xettuyen2026.dao.NguyenVongDAO;
import com.xettuyen2026.dao.ThiSinhDAO;
import com.xettuyen2026.dto.TinhDiemRequest;
import com.xettuyen2026.dto.TinhDiemResponse;
import com.xettuyen2026.dto.TraCuuRequest;
import com.xettuyen2026.dto.TraCuuResponse;
import com.xettuyen2026.entity.BangQuydoi;
import com.xettuyen2026.entity.DiemThiXetTuyen;
import com.xettuyen2026.entity.Nganh;
import com.xettuyen2026.entity.NganhTohop;
import com.xettuyen2026.entity.NguyenVongXetTuyen;
import com.xettuyen2026.entity.ThiSinh;
import com.xettuyen2026.service.ThiSinhService;

@Controller
public class WebpageController {
    private final ThiSinhService tsService;
    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private final NguyenVongDAO nguyenVongDAO = new NguyenVongDAO();
    private final NganhDAO nganhDAO = new NganhDAO();
    private final NganhTohopDAO nganhTohopDAO = new NganhTohopDAO();
    private final BangQuydoiDAO bangQuydoiDAO = new BangQuydoiDAO();
    private final DiemThiDAO diemThiDAO = new DiemThiDAO();

    public WebpageController() {
        this.tsService = new ThiSinhService();
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute(
                "nganhList",
                nganhDAO.findAll());

        // return "index";
        return "app";
    }

    @PostMapping("/tracuu")
    public String traCuu(TraCuuRequest request, Model model) {
        TraCuuResponse response = new TraCuuResponse();

        try {
            ThiSinh ts = thiSinhDAO.findByCccd(request.getCccd());

            if (ts == null) {
                response.setSuccess(false);
                response.setMessage("Không tìm thấy dữ liệu thí sinh.");
            } else {

                String password = ts.getPassword();
                // Fallback: generate password from DOB if stored password is null
                if (password == null || password.isEmpty()) {
                    String dob = ts.getNgaySinh();
                    password = (dob != null) ? dob.replace("/", "").replace("-", "") : "";
                }

                if (!password.equals(request.getPassword())) {
                    response.setSuccess(false);
                    response.setMessage("Sai mật khẩu.");
                } else {

                    List<NguyenVongXetTuyen> nvs = nguyenVongDAO.findByCccd(ts.getCccd());
                    response.setSuccess(true);

                    // Thông tin thí sinh
                    String hoTen = ((ts.getHo() != null ? ts.getHo() : "") + " " + (ts.getTen() != null ? ts.getTen() : "")).trim();
                    response.setHoTen(hoTen);
                    response.setCccd(ts.getCccd());
                    response.setNgaySinh(ts.getNgaySinh());
                    response.setGioiTinh(ts.getGioiTinh());
                    response.setDanToc(ts.getDanToc());
                    response.setNoiSinh(ts.getNoiSinh());

                    List<TraCuuResponse.NguyenVongDTO> dtoList = new ArrayList<>();
                    boolean isPending = false;
                    boolean isAdmitted = false;
                    TraCuuResponse.NguyenVongDTO admittedNV = null;

                    for (NguyenVongXetTuyen nv : nvs) {
                        TraCuuResponse.NguyenVongDTO dto = new TraCuuResponse.NguyenVongDTO();
                        dto.setMaNganh(nv.getNvManganh());

                        Nganh nganh = nganhDAO.findByMaNganh(nv.getNvManganh());
                        dto.setTenNganh(nganh != null ? nganh.getTennganh() : "Chưa xác định");
                        dto.setDiemSan(nganh != null && nganh.getnDiemsan() != null ? nganh.getnDiemsan().doubleValue()
                                : null);
                        dto.setDiemChuan(nganh != null && nganh.getnDiemtrungtuyen() != null
                                ? nganh.getnDiemtrungtuyen().doubleValue()
                                : null);

                        dto.setThuTu(nv.getNvTt());
                        // Ưu tiên tổ hợp thực tế thí sinh đã đăng ký (tt_thm), fallback sang tổ hợp gốc ngành
                        String toHopDisplay = nv.getTtThm();
                        if (toHopDisplay == null || toHopDisplay.trim().isEmpty()) {
                            toHopDisplay = (nganh != null ? nganh.getnTohopgoc() : "Chưa xác định");
                        }
                        dto.setToHop(toHopDisplay);
                        dto.setPhuongThuc(nv.getTtPhuongthuc());
                        if (nv.getDiemXettuyen() != null) {
                            dto.setDiemXetTuyen(nv.getDiemXettuyen().doubleValue());
                        }
                        dto.setKetQua(nv.getNvKetqua());

                        // Chi tiết điểm từ entity
                        if (nv.getDiemThxt() != null) dto.setDiemThxt(nv.getDiemThxt().doubleValue());
                        if (nv.getDiemUtqd() != null) dto.setDiemUtqd(nv.getDiemUtqd().doubleValue());
                        if (nv.getDiemCong() != null) dto.setDiemCongDetail(nv.getDiemCong().doubleValue());
                        dto.setToHopXetTuyen(nv.getTtThm());

                        // Tên phương thức hiển thị
                        String pt = nv.getTtPhuongthuc();
                        if (pt != null) {
                            if ("4".equals(pt) || pt.toUpperCase().contains("DGNL")) {
                                dto.setPhuongThucDisplay("ĐGNL (Thang 1200)");
                            } else if ("5".equals(pt) || pt.toUpperCase().contains("VSAT")) {
                                dto.setPhuongThucDisplay("V-SAT / THPT");
                            } else if ("1".equals(pt) || "2".equals(pt) || pt.toUpperCase().contains("PT") || pt.toUpperCase().contains("THPT")) {
                                dto.setPhuongThucDisplay("Xét điểm THPT");
                            } else {
                                dto.setPhuongThucDisplay(pt);
                            }
                        }

                        // Lấy điểm thi chi tiết theo CCCD và phương thức
                        String ptCode = nv.getTtPhuongthuc();
                        if (ptCode != null) {
                            DiemThiXetTuyen diemThi = diemThiDAO.findByCccdAndPhuongThuc(ts.getCccd(), ptCode);
                            // Fallback: nếu không tìm thấy theo phương thức, thử tìm theo CCCD
                            if (diemThi == null) {
                                diemThi = diemThiDAO.findByCccd(ts.getCccd());
                            }
                            if (diemThi != null) {
                                Map<String, Double> chiTiet = new LinkedHashMap<>();
                                addIfNotNull(chiTiet, "Toán", diemThi.getTo());
                                addIfNotNull(chiTiet, "Ngữ văn", diemThi.getVa());
                                addIfNotNull(chiTiet, "Vật lý", diemThi.getLi());
                                addIfNotNull(chiTiet, "Hóa học", diemThi.getHo());
                                addIfNotNull(chiTiet, "Sinh học", diemThi.getSi());
                                addIfNotNull(chiTiet, "Lịch sử", diemThi.getSu());
                                addIfNotNull(chiTiet, "Địa lí", diemThi.getDi());
                                addIfNotNull(chiTiet, "GDCD", diemThi.getGdcd());
                                addIfNotNull(chiTiet, "Tiếng Anh (thi)", diemThi.getN1Thi());
                                addIfNotNull(chiTiet, "Tiếng Anh (CC)", diemThi.getN1Cc());
                                addIfNotNull(chiTiet, "ĐGNL", diemThi.getNl1());
                                addIfNotNull(chiTiet, "Tin học", diemThi.getTi());
                                addIfNotNull(chiTiet, "KTPL", diemThi.getKtpl());
                                addIfNotNull(chiTiet, "CN Chăn nuôi", diemThi.getCncn());
                                addIfNotNull(chiTiet, "CN Nông nghiệp", diemThi.getCnnn());
                                addIfNotNull(chiTiet, "Năng khiếu 1", diemThi.getNk1());
                                addIfNotNull(chiTiet, "Năng khiếu 2", diemThi.getNk2());
                                addIfNotNull(chiTiet, "Năng khiếu 3", diemThi.getNk3());
                                addIfNotNull(chiTiet, "Năng khiếu 4", diemThi.getNk4());
                                addIfNotNull(chiTiet, "Năng khiếu 5", diemThi.getNk5());
                                addIfNotNull(chiTiet, "Năng khiếu 6", diemThi.getNk6());
                                if (!chiTiet.isEmpty()) {
                                    dto.setDiemThiChiTiet(chiTiet);
                                }
                            }
                        }

                        dtoList.add(dto);

                        String kq = nv.getNvKetqua();
                        if (kq == null || kq.trim().isEmpty()) {
                            isPending = true;
                        } else if ("yes".equalsIgnoreCase(kq) || "Đậu".equalsIgnoreCase(kq)
                                || "Trúng tuyển".equalsIgnoreCase(kq)) {
                            isAdmitted = true;
                            if (admittedNV == null || (dto.getThuTu() != null && admittedNV.getThuTu() != null
                                    && dto.getThuTu() < admittedNV.getThuTu())) {
                                admittedNV = dto;
                            }
                        }
                    }

                    response.setNguyenVongs(dtoList);
                    response.setPending(isPending);
                    response.setAdmitted(isAdmitted);
                    response.setAdmittedNV(admittedNV);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }

        model.addAttribute("result", response);

        model.addAttribute(
                "nganhList",
                nganhDAO.findAll());
        model.addAttribute(
                "activeTab",
                "tracuu");
        model.addAttribute(
                "inputCccd",
                request.getCccd());
        model.addAttribute(
                "inputPassword",
                request.getPassword());

        // return "index";
        return "app";
    }

    @PostMapping("/vsat")
    public String tinhDiemVSAT(TinhDiemRequest request, Model model) {
        TinhDiemResponse response = new TinhDiemResponse();
        try {
            double diemCong = request.getDiemCong() != null ? request.getDiemCong() : 0;
            double khuVuc = request.getKhuVuc() != null ? request.getKhuVuc() : 0;
            int doiTuongCode = request.getDoiTuong() != null ? request.getDoiTuong().intValue() : 0;
            double doiTuongValue = mapDoiTuongToValue(doiTuongCode);
            double mdut = khuVuc + doiTuongValue;

            // Map subject code -> input value
            Map<String, Double> subjectInputs = new java.util.HashMap<>();
            subjectInputs.put("TO", request.getToan() != null ? request.getToan() : 0.0);
            subjectInputs.put("VA", request.getVan() != null ? request.getVan() : 0.0);
            subjectInputs.put("SI", request.getSinh() != null ? request.getSinh() : 0.0);
            subjectInputs.put("LI", request.getLy() != null ? request.getLy() : 0.0);
            subjectInputs.put("HO", request.getHoa() != null ? request.getHoa() : 0.0);
            subjectInputs.put("SU", request.getSu() != null ? request.getSu() : 0.0);
            subjectInputs.put("DI", request.getDia() != null ? request.getDia() : 0.0);
            subjectInputs.put("AN", request.getAnh() != null ? request.getAnh() : 0.0);

            Nganh nganh = nganhDAO.findByMaNganh(request.getMaNganh());
            if (nganh == null) {
                throw new Exception("Ngành không tồn tại");
            }
            double diemSan = nganh.getnDiemsan() != null ? nganh.getnDiemsan().doubleValue() : 20.0;

            List<NganhTohop> tohopList = nganhTohopDAO.findByMaNganh(request.getMaNganh());

            List<Map<String, Object>> combinations = new java.util.ArrayList<>();
            String bestTohop = "";
            double bestScore = -Double.MAX_VALUE;
            double bestDut = mdut;

            // Map monCode to Vietnamese name for V-SAT
            Map<String, String> monMap = new java.util.HashMap<>();
            monMap.put("TO", "Toán");
            monMap.put("VA", "Ngữ văn");
            monMap.put("SI", "Sinh học");
            monMap.put("LI", "Vật lý");
            monMap.put("HO", "Hóa học");
            monMap.put("SU", "Lịch sử");
            monMap.put("DI", "Địa lí");
            monMap.put("N1", "Tiếng Anh");

            List<BangQuydoi> allQuydoi = bangQuydoiDAO.findAll();
            List<BangQuydoi> vsatRules = new java.util.ArrayList<>();
            for (BangQuydoi b : allQuydoi) {
                if ("V-SAT".equalsIgnoreCase(b.getdPhuongthuc())) {
                    vsatRules.add(b);
                }
            }

            // Helper: look up BangQuydoi and compute converted score for one subject
            // Returns a map with: conv, diema, diemb, diemc, diemd, error
            java.util.function.Function<String[], Map<String, Object>> convertSubject = (args) -> {
                String monCode = args[0];
                String tohopCode = args[1];
                double inputVal = subjectInputs.getOrDefault(monCode, 0.0);
                Map<String, Object> res = new java.util.LinkedHashMap<>();
                res.put("inputVal", inputVal);
                if (inputVal <= 0) {
                    res.put("conv", 0.0);
                    res.put("error", false);
                    return res;
                }
                String monName = monMap.get(monCode);
                List<BangQuydoi> rows = new java.util.ArrayList<>();
                for (BangQuydoi b : vsatRules) {
                    if (monName != null && monName.equalsIgnoreCase(b.getdMon())) {
                        rows.add(b);
                    }
                }

                BangQuydoi matched = null;
                for (BangQuydoi bq : rows) {
                    double a = bq.getdDiema() != null ? bq.getdDiema().doubleValue() : 0;
                    double b = bq.getdDiemb() != null ? bq.getdDiemb().doubleValue() : 0;
                    if (inputVal >= a && inputVal <= b) {
                        matched = bq;
                        break;
                    }
                }
                if (matched == null) {
                    res.put("conv", 0.0);
                    res.put("error", true);
                    return res;
                }
                double a = matched.getdDiema().doubleValue();
                double b = matched.getdDiemb().doubleValue();
                double c = matched.getdDiemc().doubleValue();
                double d = matched.getdDiemd().doubleValue();
                double conv = (b == a) ? c : c + (inputVal - a) / (b - a) * (d - c);
                res.put("conv", conv);
                res.put("diema", a);
                res.put("diemb", b);
                res.put("diemc", c);
                res.put("diemd", d);
                res.put("error", false);
                return res;
            };

            if (tohopList != null && !tohopList.isEmpty()) {
                for (NganhTohop th : tohopList) {
                    String mon1 = th.getThMon1();
                    String mon2 = th.getThMon2();
                    String mon3 = th.getThMon3();
                    int hs1 = th.getHsmon1() != null ? th.getHsmon1().intValue() : 1;
                    int hs2 = th.getHsmon2() != null ? th.getHsmon2().intValue() : 1;
                    int hs3 = th.getHsmon3() != null ? th.getHsmon3().intValue() : 1;
                    int hsSum = hs1 + hs2 + hs3;
                    double dolech = th.getDolech() != null ? th.getDolech().doubleValue() : 0.0;

                    Map<String, Object> r1 = convertSubject.apply(new String[] { mon1, th.getMatohop() });
                    Map<String, Object> r2 = convertSubject.apply(new String[] { mon2, th.getMatohop() });
                    Map<String, Object> r3 = convertSubject.apply(new String[] { mon3, th.getMatohop() });

                    double m1Conv = (Double) r1.get("conv");
                    double m2Conv = (Double) r2.get("conv");
                    double m3Conv = (Double) r3.get("conv");
                    double m1Val = (Double) r1.get("inputVal");
                    double m2Val = (Double) r2.get("inputVal");
                    double m3Val = (Double) r3.get("inputVal");

                    // DTHXT (before dolech subtraction, this is the raw weighted average)
                    double dthxtRaw = (m1Conv * hs1 + m2Conv * hs2 + m3Conv * hs3) / (double) hsSum * 3.0;
                    // Net DTHXT sent to formula display
                    double dthxt = dthxtRaw;
                    // Xet nguong = sum of converted + uu tien
                    double xetNguong = m1Conv + m2Conv + m3Conv + mdut;
                    
                    double dthgxt = dthxt - dolech;
                    double actualDut = mdut;
                    if (dthgxt + diemCong >= 22.5) {
                        actualDut = ((30.0 - dthxt - diemCong) / 7.5) * mdut;
                        if (actualDut < 0) actualDut = 0.0;
                    }

                    // DXT = DTHGXT + DC + DUT (capped <=30)
                    double dxtCombo = Math.min(dthgxt + diemCong + actualDut, 30.0);

                    Map<String, Object> thDetail = new java.util.LinkedHashMap<>();
                    thDetail.put("maTohop", th.getMatohop());
                    thDetail.put("mon1", mon1);
                    thDetail.put("mon2", mon2);
                    thDetail.put("mon3", mon3);
                    thDetail.put("mon1Name", monMap.getOrDefault(mon1, mon1));
                    thDetail.put("mon2Name", monMap.getOrDefault(mon2, mon2));
                    thDetail.put("mon3Name", monMap.getOrDefault(mon3, mon3));
                    thDetail.put("hs1", hs1);
                    thDetail.put("hs2", hs2);
                    thDetail.put("hs3", hs3);
                    thDetail.put("dolech", dolech);
                    thDetail.put("m1Val", m1Val);
                    thDetail.put("m2Val", m2Val);
                    thDetail.put("m3Val", m3Val);
                    thDetail.put("m1Conv", m1Conv);
                    thDetail.put("m2Conv", m2Conv);
                    thDetail.put("m3Conv", m3Conv);
                    // Pass phân vị values for formula display
                    thDetail.put("m1Error", r1.get("error"));
                    thDetail.put("m2Error", r2.get("error"));
                    thDetail.put("m3Error", r3.get("error"));
                    thDetail.put("m1Diema", r1.getOrDefault("diema", null));
                    thDetail.put("m1Diemb", r1.getOrDefault("diemb", null));
                    thDetail.put("m1Diemc", r1.getOrDefault("diemc", null));
                    thDetail.put("m1Diemd", r1.getOrDefault("diemd", null));
                    thDetail.put("m2Diema", r2.getOrDefault("diema", null));
                    thDetail.put("m2Diemb", r2.getOrDefault("diemb", null));
                    thDetail.put("m2Diemc", r2.getOrDefault("diemc", null));
                    thDetail.put("m2Diemd", r2.getOrDefault("diemd", null));
                    thDetail.put("m3Diema", r3.getOrDefault("diema", null));
                    thDetail.put("m3Diemb", r3.getOrDefault("diemb", null));
                    thDetail.put("m3Diemc", r3.getOrDefault("diemc", null));
                    thDetail.put("m3Diemd", r3.getOrDefault("diemd", null));
                    thDetail.put("dthxt", dthxt);
                    thDetail.put("xetNguong", xetNguong);
                    thDetail.put("dut", actualDut);
                    thDetail.put("dc", diemCong);
                    thDetail.put("diemXetTuyen", dxtCombo);

                    if (dxtCombo > bestScore) {
                        bestScore = dxtCombo;
                        bestTohop = th.getMatohop();
                        bestDut = actualDut;
                    }
                    combinations.add(thDetail);
                }
            }

            if (bestScore == -Double.MAX_VALUE)
                bestScore = 0;
            if (bestTohop.isEmpty() && !combinations.isEmpty()) {
                bestTohop = (String) combinations.get(0).get("maTohop");
            }

            response.setSuccess(true);
            response.setMaNganh(nganh.getManganh());
            response.setTenNganh(nganh.getTennganh());
            response.setToHop(bestTohop);
            response.setDiemCong(diemCong);
            response.setDiemUuTien(bestDut);
            response.setDiemXetTuyen(bestScore);
            response.setDiemNguong(diemSan);
            response.setIsDat(bestScore >= diemSan);
            response.setToHopDetails(combinations);

            // Set tổ hợp gốc from ngành entity
            response.setTohopGoc(nganh.getnTohopgoc() != null ? nganh.getnTohopgoc() : bestTohop);

            // Set khu vực priority name & value
            response.setKhuVucValue(khuVuc);
            if (khuVuc == 0.75) response.setKhuVucName("KV1");
            else if (khuVuc == 0.50) response.setKhuVucName("2NT");
            else if (khuVuc == 0.25) response.setKhuVucName("KV2");
            else response.setKhuVucName("Không có");

            // Set đối tượng priority name & value
            response.setDoiTuongValue(doiTuongValue);
            response.setDoiTuongName(mapDoiTuongToName(doiTuongCode));

        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }

        model.addAttribute("vsatResult", response);
        model.addAttribute("vsatInput", request);

        model.addAttribute(
                "nganhList",
                nganhDAO.findAll());
        model.addAttribute(
                "activeCalcMethod",
                "vsat");
        model.addAttribute(
                "activeTab",
                "tinhdiem");

        // return "index";
        return "app";
    }

    @PostMapping("/dgnl")
    public String tinhDiemDGNL(TinhDiemRequest request, Model model) {
        TinhDiemResponse response = new TinhDiemResponse();
        try {
            double diemThi = request.getDiemThi() != null ? request.getDiemThi() : 0;
            double diemCong = request.getDiemCong() != null ? request.getDiemCong() : 0;
            double khuVuc = request.getKhuVuc() != null ? request.getKhuVuc() : 0;
            int doiTuongCode = request.getDoiTuong() != null ? request.getDoiTuong().intValue() : 0;
            double doiTuongValue = mapDoiTuongToValue(doiTuongCode);

            // Priority score
            double mdut = khuVuc + doiTuongValue;

            Nganh nganh = nganhDAO.findByMaNganh(request.getMaNganh());
            if (nganh == null) {
                throw new Exception("Ngành không tồn tại");
            }
            double diemSan = nganh.getnDiemsan() != null ? nganh.getnDiemsan().doubleValue() : 24.0;

            // Đối với ĐGNL, theo quy tắc chuẩn: Chỉ dùng Tổ hợp gốc của ngành để quy đổi
            // Không lặp qua tất cả các tổ hợp để lấy max như THPT/VSAT
            String tohopGoc = nganh.getnTohopgoc();
            if (tohopGoc == null || tohopGoc.trim().isEmpty()) {
                tohopGoc = "A01"; // Fallback mặc định
            }

            double bestDiemQuyDoi = 0;
            BangQuydoi bestMatched = null;
            String bestTohopGoc = tohopGoc;

            List<BangQuydoi> rows = bangQuydoiDAO.findByPhuongthucAndTohop("DGNL", tohopGoc);
            if (rows == null || rows.isEmpty()) {
                // Fallback to A01 if the combination has no DGNL mapping in the database
                rows = bangQuydoiDAO.findByPhuongthucAndTohop("DGNL", "A01");
                bestTohopGoc = "A01 (Quy đổi mặc định)";
            }

            double diemQuyDoi = 0;
            if (rows != null && !rows.isEmpty()) {
                rows.sort(java.util.Comparator.comparing(BangQuydoi::getdDiema));
                if (diemThi <= rows.get(0).getdDiema().doubleValue()) {
                    bestMatched = rows.get(0);
                    diemQuyDoi = bestMatched.getdDiemc() != null ? bestMatched.getdDiemc().doubleValue() : 0;
                } else if (diemThi >= rows.get(rows.size() - 1).getdDiemb().doubleValue()) {
                    bestMatched = rows.get(rows.size() - 1);
                    diemQuyDoi = bestMatched.getdDiemd() != null ? bestMatched.getdDiemd().doubleValue()
                            : (bestMatched.getdDiemc() != null ? bestMatched.getdDiemc().doubleValue() : 0);
                } else {
                    for (BangQuydoi qd : rows) {
                        if (qd.getdDiema() == null || qd.getdDiemb() == null)
                            continue;
                        double a = qd.getdDiema().doubleValue();
                        double b = qd.getdDiemb().doubleValue();
                        if (diemThi >= a && diemThi < b) {
                            bestMatched = qd;
                            double c = qd.getdDiemc() != null ? qd.getdDiemc().doubleValue() : 0;
                            double d = qd.getdDiemd() != null ? qd.getdDiemd().doubleValue() : c;
                            if (b == a) {
                                diemQuyDoi = c;
                            } else {
                                diemQuyDoi = c + (diemThi - a) / (b - a) * (d - c);
                            }
                            break;
                        }
                    }
                }
            }
            bestDiemQuyDoi = diemQuyDoi;

            double finalDiemQuyDoi = bestDiemQuyDoi > 0 ? bestDiemQuyDoi : 0;
            
            double actualDut = mdut;
            if (finalDiemQuyDoi + diemCong >= 22.5) {
                actualDut = ((30.0 - finalDiemQuyDoi - diemCong) / 7.5) * mdut;
                if (actualDut < 0) actualDut = 0.0;
            }
            
            double diemXetTuyen = Math.min(finalDiemQuyDoi + diemCong + actualDut, 30.0);

            if (bestMatched != null) {
                response.setDiema(bestMatched.getdDiema() != null ? bestMatched.getdDiema().doubleValue() : null);
                response.setDiemb(bestMatched.getdDiemb() != null ? bestMatched.getdDiemb().doubleValue() : null);
                response.setDiemc(bestMatched.getdDiemc() != null ? bestMatched.getdDiemc().doubleValue() : null);
                response.setDiemd(bestMatched.getdDiemd() != null ? bestMatched.getdDiemd().doubleValue() : null);
            }

            response.setSuccess(true);
            response.setMaNganh(nganh.getManganh());
            response.setTenNganh(nganh.getTennganh());
            response.setToHop(bestTohopGoc);
            response.setDiemThi(diemThi);
            response.setDiemQuyDoi(finalDiemQuyDoi);
            response.setDiemCong(diemCong);
            response.setDiemUuTien(actualDut);
            response.setDiemXetTuyen(diemXetTuyen);
            response.setDiemNguong(diemSan);
            response.setIsDat(diemXetTuyen >= diemSan);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        model.addAttribute("dgnlResult", response);
        model.addAttribute("dgnlInput", request);

        model.addAttribute(
                "nganhList",
                nganhDAO.findAll());
        model.addAttribute(
                "activeCalcMethod",
                "dgnl");
        model.addAttribute(
                "activeTab",
                "tinhdiem");

        // return "index";
        return "app";
    }
    
    @PostMapping("/thpt")
    public String tinhDiemTHPT(TinhDiemRequest request, Model model) {

        TinhDiemResponse response = new TinhDiemResponse();

        try {

            double diemCong = request.getDiemCong() != null
                    ? request.getDiemCong()
                    : 0;

            double khuVuc = request.getKhuVuc() != null
                    ? request.getKhuVuc()
                    : 0;

            int doiTuongCode = request.getDoiTuong() != null
                    ? request.getDoiTuong().intValue()
                    : 0;

            double doiTuongValue = mapDoiTuongToValue(doiTuongCode);

            double mdut = khuVuc + doiTuongValue;

            // SUBJECT INPUTS
            Map<String, Double> scores = new java.util.HashMap<>();

            if (request.getToan() != null) {
                scores.put("TO", request.getToan());
            }

            if (request.getVan() != null) {
                scores.put("VA", request.getVan());
            }

            if (request.getLy() != null) {
                scores.put("LI", request.getLy());
            }

            if (request.getHoa() != null) {
                scores.put("HO", request.getHoa());
            }

            if (request.getSinh() != null) {
                scores.put("SI", request.getSinh());
            }

            if (request.getSu() != null) {
                scores.put("SU", request.getSu());
            }

            if (request.getDia() != null) {
                scores.put("DI", request.getDia());
            }

            if (request.getAnh() != null) {
                scores.put("N1", request.getAnh());
            }

            if (request.getCncn() != null) {
                scores.put("CNCN", request.getCncn());
            }

            if (request.getCnnn() != null) {
                scores.put("CNNN", request.getCnnn());
            }

            if (request.getTin() != null) {
                scores.put("TI", request.getTin());
            }

            if (request.getKtpl() != null) {
                scores.put("KTPL", request.getKtpl());
            }

            if (request.getNk1() != null) {
                scores.put("NK1", request.getNk1());
            }

            if (request.getNk2() != null) {
                scores.put("NK2", request.getNk2());
            }

            if (request.getNk3() != null) {
                scores.put("NK3", request.getNk3());
            }

            if (request.getNk4() != null) {
                scores.put("NK4", request.getNk4());
            }

            if (request.getNk5() != null) {
                scores.put("NK5", request.getNk5());
            }

            if (request.getNk6() != null) {
                scores.put("NK6", request.getNk6());
            }

            Nganh nganh = nganhDAO.findByMaNganh(request.getMaNganh());

            if (nganh == null) {
                throw new Exception("Ngành không tồn tại");
            }

            String toHopGoc = nganh.getnTohopgoc();

            double diemSan = nganh.getnDiemsan() != null
                    ? nganh.getnDiemsan().doubleValue()
                    : 20.0;

            List<NganhTohop> tohopList =
                    nganhTohopDAO.findByMaNganh(request.getMaNganh());

            List<Map<String, Object>> combinations =
                    new ArrayList<>();

            double bestScore = -999;
            String bestToHop = "";

            // ĐỘ LỆCH THPT
            Map<String, Map<String, Double>> diffTable =
                    new java.util.HashMap<>();

            // A00
            Map<String, Double> A00 = new java.util.HashMap<>();
            A00.put("A01", -0.69);
            A00.put("B00", -1.21);
            A00.put("C00", 2.32);
            A00.put("C01", 0.94);
            A00.put("D01", -0.68);
            A00.put("D07", -1.62);
            diffTable.put("A00", A00);

            // A01
            Map<String, Double> A01 = new java.util.HashMap<>();
            A01.put("A00", 0.69);
            A01.put("B00", -0.52);
            A01.put("C00", 3.01);
            A01.put("C01", 1.63);
            A01.put("D01", 0.01);
            A01.put("D07", -0.93);
            diffTable.put("A01", A01);

            // B00
            Map<String, Double> B00 = new java.util.HashMap<>();
            B00.put("A00", 1.21);
            B00.put("A01", 0.52);
            B00.put("C00", 3.53);
            B00.put("C01", 2.15);
            B00.put("D01", 0.53);
            B00.put("D07", -0.41);
            diffTable.put("B00", B00);

            // C00
            Map<String, Double> C00 = new java.util.HashMap<>();
            C00.put("A00", -2.32);
            C00.put("A01", -3.01);
            C00.put("B00", -3.53);
            C00.put("C01", -1.38);
            C00.put("D01", -3.00);
            C00.put("D07", -3.94);
            diffTable.put("C00", C00);

            // C01
            Map<String, Double> C01 = new java.util.HashMap<>();
            C01.put("A00", -0.94);
            C01.put("A01", -1.63);
            C01.put("B00", -2.15);
            C01.put("C00", 1.38);
            C01.put("D01", -1.62);
            C01.put("D07", -2.56);
            diffTable.put("C01", C01);

            // D01
            Map<String, Double> D01 = new java.util.HashMap<>();
            D01.put("A00", 0.68);
            D01.put("A01", -0.01);
            D01.put("B00", -0.53);
            D01.put("C00", 3.00);
            D01.put("C01", 1.62);
            D01.put("D07", -0.94);
            diffTable.put("D01", D01);

            for (NganhTohop th : tohopList) {

                String mon1 = th.getThMon1();
                String mon2 = th.getThMon2();
                String mon3 = th.getThMon3();

                if (!scores.containsKey(mon1) || !scores.containsKey(mon2) || !scores.containsKey(mon3)) {
                    continue;
                }

                double m1 = scores.getOrDefault(mon1, 0.0);
                double m2 = scores.getOrDefault(mon2, 0.0);
                double m3 = scores.getOrDefault(mon3, 0.0);

                int hs1 = th.getHsmon1() != null
                        ? th.getHsmon1().intValue()
                        : 1;

                int hs2 = th.getHsmon2() != null
                        ? th.getHsmon2().intValue()
                        : 1;

                int hs3 = th.getHsmon3() != null
                        ? th.getHsmon3().intValue()
                        : 1;

                int hsSum = hs1 + hs2 + hs3;

                double dthxt =
                        ((m1 * hs1)
                        + (m2 * hs2)
                        + (m3 * hs3))
                        / hsSum * 3.0;

                // ĐỘ LỆCH
                double dolech = 0.0;

                if (diffTable.containsKey(toHopGoc)) {

                    Map<String, Double> row =
                            diffTable.get(toHopGoc);

                    if (row.containsKey(th.getMatohop())) {
                        dolech = row.get(th.getMatohop());
                    }
                }

                // QUY ĐỔI
                double diemSauQuyDoi = dthxt - dolech;

                double actualDut = mdut;

                if (diemSauQuyDoi + diemCong >= 22.5) {

                    actualDut =
                            ((30.0 - diemSauQuyDoi - diemCong) / 7.5)
                            * mdut;

                    if (actualDut < 0) {
                        actualDut = 0;
                    }
                }

                double dxt =
                        Math.min(
                                diemSauQuyDoi
                                + diemCong
                                + actualDut,
                                30.0);

                Map<String, Object> detail = new java.util.LinkedHashMap<>();

                detail.put("maTohop", th.getMatohop());

                detail.put("mon1", mon1);
                detail.put("mon2", mon2);
                detail.put("mon3", mon3);

                detail.put("m1Val", m1);
                detail.put("m2Val", m2);
                detail.put("m3Val", m3);

                detail.put("hs1", hs1);
                detail.put("hs2", hs2);
                detail.put("hs3", hs3);

                detail.put("dthxt", dthxt);

                detail.put("dolech", dolech);

                detail.put("dthgxt", diemSauQuyDoi);

                detail.put("dc", diemCong);

                detail.put("dut", actualDut);

                detail.put("diemXetTuyen", dxt);

                combinations.add(detail);

                if (dxt > bestScore) {
                    bestScore = dxt;
                    bestToHop = th.getMatohop();
                }
            }

            response.setSuccess(true);

            response.setMaNganh(nganh.getManganh());

            response.setTenNganh(nganh.getTennganh());

            response.setToHop(bestToHop);

            response.setDiemCong(diemCong);

            response.setDiemUuTien(mdut);

            response.setDiemXetTuyen(bestScore);

            response.setDiemNguong(diemSan);

            response.setIsDat(bestScore >= diemSan);

            response.setToHopDetails(combinations);

        } catch (Exception e) {

            e.printStackTrace();

            response.setSuccess(false);

            response.setMessage(e.getMessage());
        }

        model.addAttribute("thptResult", response);

        model.addAttribute("thptInput", request);

        model.addAttribute(
                "nganhList",
                nganhDAO.findAll());

        model.addAttribute(
                "activeCalcMethod",
                "thpt");

        model.addAttribute(
                "activeTab",
                "tinhdiem");

        return "app";
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER: MAPPING ĐỐI TƯỢNG ƯU TIÊN
    // ════════════════════════════════════════════════════════════

    /**
     * Chuyển mã đối tượng (1-7) sang điểm ưu tiên tương ứng.
     * Khớp với AdmissionService.calculateMdut().
     */
    private double mapDoiTuongToValue(int code) {
        switch (code) {
            case 1:  return 2.0;
            case 2:  return 1.5;
            case 3:  return 1.0;
            case 4:  return 0.0;
            case 5:
            case 6:
            case 7:  return 1.0;
            default: return 0.0;
        }
    }

    /**
     * Chuyển mã đối tượng (1-7) sang tên hiển thị.
     */
    private String mapDoiTuongToName(int code) {
        if (code >= 1 && code <= 7) {
            return "ĐT 0" + code;
        }
        return "Không đối tượng";
    }

    private void addIfNotNull(Map<String, Double> map, String key, BigDecimal value) {
        if (value != null && value.doubleValue() > 0) {
            map.put(key, value.doubleValue());
        }
    }
}
