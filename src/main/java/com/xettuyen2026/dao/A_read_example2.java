package com.xettuyen2026.dao;

import com.xettuyen2026.dao.base.BaseDAO;
import com.xettuyen2026.entity.ThiSinh;

import java.util.List;
import java.util.Optional;

/**
 * ThiSinhDAO - Ví dụ minh họa cách dùng BaseDAO
 *
 * Nguyên tắc:
 *   - Các method CRUD cơ bản (save, update, delete, findAll...) → KHÔNG cần viết lại, dùng từ BaseDAO
 *   - Chỉ viết thêm method đặc thù của ThiSinh (tìm theo CCCD, tìm theo họ tên...)
 */
public class ThiSinhDAO extends BaseDAO<ThiSinh> {

    // ✅ Bắt buộc: truyền class entity vào constructor của BaseDAO
    public ThiSinhDAO() {
        super(ThiSinh.class);
    }

    // ─────────────────────────────────────────────
    // Các method đặc thù — KHÔNG có trong BaseDAO
    // ─────────────────────────────────────────────

    /**
     * Tìm thí sinh theo CCCD (trả về null nếu không có).
     */
    public ThiSinh findByCCCD(String cccd) {
        return queryOne(
            "FROM ThiSinh WHERE cccd = :cccd",
            q -> q.setParameter("cccd", cccd)
        );
    }

    /**
     * Tìm thí sinh theo CCCD, trả về Optional.
     */
    public Optional<ThiSinh> findByCCCDOptional(String cccd) {
        return Optional.ofNullable(findByCCCD(cccd));
    }

    /**
     * Tìm kiếm theo CCCD hoặc họ tên (có phân trang).
     */
    public List<ThiSinh> search(String keyword, int page, int pageSize) {
        String kw = "%" + keyword.trim() + "%";
        return query(
            "FROM ThiSinh WHERE cccd LIKE :kw OR CONCAT(ho, ' ', ten) LIKE :kw",
            q -> q.setParameter("kw", kw),
            page, pageSize
        );
    }

    /**
     * Đếm kết quả tìm kiếm (dùng để tính tổng trang).
     */
    public long countSearch(String keyword) {
        String kw = "%" + keyword.trim() + "%";
        return countQuery(
            "SELECT COUNT(*) FROM ThiSinh WHERE cccd LIKE :kw OR CONCAT(ho, ' ', ten) LIKE :kw",
            q -> q.setParameter("kw", kw)
        );
    }

    /**
     * Lấy danh sách thí sinh theo khu vực.
     */
    public List<ThiSinh> findByKhuVuc(String khuVuc) {
        return query(
            "FROM ThiSinh WHERE khuVuc = :kv ORDER BY ho, ten",
            q -> q.setParameter("kv", khuVuc)
        );
    }

    /**
     * Lấy danh sách thí sinh theo đối tượng ưu tiên.
     */
    public List<ThiSinh> findByDoiTuong(String doiTuong) {
        return query(
            "FROM ThiSinh WHERE doiTuong = :dt ORDER BY ho, ten",
            q -> q.setParameter("dt", doiTuong)
        );
    }
}