package com.xettuyen2026.dao.base;

import com.xettuyen2026.config.HibernateConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * BaseDAO - Generic CRUD dùng chung cho tất cả DAO
 *
 * Cách dùng:
 *   public class ThiSinhDAO extends BaseDAO<ThiSinh> {
 *       public ThiSinhDAO() { super(ThiSinh.class); }
 *   }
 *
 * @param <T> Entity class
 */
public class BaseDAO<T> {

    private final Class<T> entityClass;

    public BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ─────────────────────────────────────────────
    // Session helper
    // ─────────────────────────────────────────────

    protected Session openSession() {
        return HibernateConfig.getSessionFactory().openSession();
    }

    // ─────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────

    /**
     * Tìm theo ID (primary key).
     * Trả về null nếu không tìm thấy.
     */
    public T findById(int id) {
        try (Session session = openSession()) {
            return session.get(entityClass, id);
        }
    }

    /**
     * Tìm theo ID, trả về Optional.
     */
    public Optional<T> findByIdOptional(int id) {
        try (Session session = openSession()) {
            return Optional.ofNullable(session.get(entityClass, id));
        }
    }

    /**
     * Lấy toàn bộ danh sách.
     */
    public List<T> findAll() {
        try (Session session = openSession()) {
            return session
                .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                .list();
        }
    }

    /**
     * Lấy danh sách có phân trang.
     *
     * @param page     Trang hiện tại (bắt đầu từ 1)
     * @param pageSize Số dòng mỗi trang
     */
    public List<T> findAll(int page, int pageSize) {
        try (Session session = openSession()) {
            return session
                .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .list();
        }
    }

    /**
     * Đếm tổng số bản ghi.
     */
    public long count() {
        try (Session session = openSession()) {
            return session
                .createQuery("SELECT COUNT(*) FROM " + entityClass.getSimpleName(), Long.class)
                .uniqueResult();
        }
    }

    /**
     * Kiểm tra có tồn tại bản ghi với ID không.
     */
    public boolean existsById(int id) {
        try (Session session = openSession()) {
            Long cnt = session
                .createQuery(
                    "SELECT COUNT(*) FROM " + entityClass.getSimpleName() + " WHERE id = :id",
                    Long.class)
                .setParameter("id", id)
                .uniqueResult();
            return cnt != null && cnt > 0;
        }
    }

    // ─────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────

    /**
     * Thêm mới một bản ghi.
     * Ném exception nếu bị lỗi (VD: trùng unique key).
     */
    public void save(T entity) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Lỗi khi thêm mới: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────

    /**
     * Cập nhật bản ghi đã tồn tại.
     */
    public void update(T entity) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Lỗi khi cập nhật: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // SAVE OR UPDATE
    // ─────────────────────────────────────────────

    /**
     * Thêm mới nếu chưa có, cập nhật nếu đã tồn tại.
     * Dùng cho trường hợp upsert (VD: import file).
     */
    public void saveOrUpdate(T entity) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Lỗi khi lưu: " + e.getMessage(), e);
        }
    }

    /**
     * Thêm mới nhiều bản ghi cùng lúc (batch insert).
     * Hiệu quả hơn khi import file Excel số lượng lớn.
     *
     * @param entities  Danh sách cần lưu
     * @param batchSize Số bản ghi flush mỗi lần (khuyến nghị 50)
     */
    public void saveAll(List<T> entities, int batchSize) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < entities.size(); i++) {
                session.merge(entities.get(i));
                // Flush và clear định kỳ để tránh tràn bộ nhớ
                if ((i + 1) % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Lỗi khi lưu batch: " + e.getMessage(), e);
        }
    }

    /**
     * Gọi saveAll với batchSize mặc định là 50.
     */
    public void saveAll(List<T> entities) {
        saveAll(entities, 50);
    }

    // ─────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────

    /**
     * Xóa một bản ghi.
     */
    public void delete(T entity) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            // Nếu entity từ session khác (detached) thì cần merge trước
            T managed = session.contains(entity) ? entity : session.merge(entity);
            session.remove(managed);
            tx.commit();
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Lỗi khi xóa: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa theo ID.
     * Trả về true nếu xóa thành công, false nếu không tìm thấy.
     */
    public boolean deleteById(int id) {
        Transaction tx = null;
        try (Session session = openSession()) {
            T entity = session.get(entityClass, id);
            if (entity == null) return false;
            tx = session.beginTransaction();
            session.remove(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Lỗi khi xóa theo ID: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa nhiều bản ghi cùng lúc.
     */
    public void deleteAll(List<T> entities) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            for (T entity : entities) {
                T managed = session.contains(entity) ? entity : session.merge(entity);
                session.remove(managed);
            }
            tx.commit();
        } catch (Exception e) {
            rollback(tx);
            throw new RuntimeException("Lỗi khi xóa danh sách: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // QUERY HELPER (dùng trong DAO con)
    // ─────────────────────────────────────────────

    /**
     * Chạy HQL query tùy chỉnh, trả về danh sách.
     *
     * Ví dụ dùng trong DAO con:
     *   return query("FROM ThiSinh WHERE khuVuc = :kv",
     *                q -> q.setParameter("kv", "KV1"));
     */
    protected List<T> query(String hql, QueryCustomizer<T> customizer) {
        try (Session session = openSession()) {
            Query<T> q = session.createQuery(hql, entityClass);
            if (customizer != null) customizer.customize(q);
            return q.list();
        }
    }

    /**
     * Chạy HQL query tùy chỉnh có phân trang.
     */
    protected List<T> query(String hql, QueryCustomizer<T> customizer, int page, int pageSize) {
        try (Session session = openSession()) {
            Query<T> q = session.createQuery(hql, entityClass);
            if (customizer != null) customizer.customize(q);
            q.setFirstResult((page - 1) * pageSize);
            q.setMaxResults(pageSize);
            return q.list();
        }
    }

    /**
     * Chạy HQL query trả về 1 kết quả duy nhất (hoặc null).
     */
    protected T queryOne(String hql, QueryCustomizer<T> customizer) {
        try (Session session = openSession()) {
            Query<T> q = session.createQuery(hql, entityClass);
            if (customizer != null) customizer.customize(q);
            return q.uniqueResult();
        }
    }

    /**
     * Đếm kết quả theo HQL tùy chỉnh.
     */
    protected long countQuery(String hql, QueryCustomizer<Long> customizer) {
        try (Session session = openSession()) {
            Query<Long> q = session.createQuery(hql, Long.class);
            if (customizer != null) customizer.customize(q);
            Long result = q.uniqueResult();
            return result != null ? result : 0L;
        }
    }

    // ─────────────────────────────────────────────
    // INTERNAL
    // ─────────────────────────────────────────────

    private void rollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (Exception ex) {
                // Bỏ qua lỗi rollback
            }
        }
    }

    // ─────────────────────────────────────────────
    // FUNCTIONAL INTERFACE
    // ─────────────────────────────────────────────

    /**
     * Interface để tùy chỉnh query (set parameter, order by...) trước khi execute.
     */
    @FunctionalInterface
    public interface QueryCustomizer<E> {
        void customize(Query<E> query);
    }
}