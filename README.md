# 🎓 Hệ thống Xét Tuyển 2026

Phần mềm quản lý tuyển sinh dành cho trường đại học.  
Công nghệ: **Java Swing + Hibernate + MySQL**

---

## 📋 Mục lục

- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt môi trường](#cài-đặt-môi-trường)
- [Cài đặt project](#cài-đặt-project)
- [Cấu hình database](#cấu-hình-database)
- [Chạy ứng dụng](#chạy-ứng-dụng)
- [Cấu trúc project](#cấu-trúc-project)
- [Phân công](#phân-công)
- [Quy tắc làm việc nhóm](#quy-tắc-làm-việc-nhóm)

---

## ⚙️ Yêu cầu hệ thống

| Công cụ                 | Phiên bản   | Link tải                              |
| ----------------------- | ----------- | ------------------------------------- |
| Java JDK                | 17 trở lên  | https://adoptium.net                  |
| MySQL                   | 8.0 trở lên | https://dev.mysql.com/downloads/mysql |
| Maven                   | 3.8 trở lên | https://maven.apache.org/download.cgi |
| Spring Tool Suite (STS) | 4.x         | https://spring.io/tools               |
| Git                     | Mới nhất    | https://git-scm.com                   |

---

## 🛠️ Cài đặt môi trường

### Bước 1 — Cài Java JDK 17

1. Truy cập https://adoptium.net
2. Tải **Temurin 17 (LTS)**
3. Chạy file cài đặt → Next → Next → Finish
4. Kiểm tra:

```bash
java -version
# Kết quả mong đợi: openjdk version "17.x.x"
```

---

### Bước 2 — Cài MySQL 8

1. Truy cập https://dev.mysql.com/downloads/installer
2. Tải **MySQL Installer for Windows** (hoặc bản tương ứng cho macOS/Linux)
3. Chọn **Developer Default** → Execute → Next
4. Đặt **root password** (nhớ lại password này, sẽ dùng ở bước sau)
5. Kiểm tra:

```bash
mysql -u root -p
# Nhập password → xuất hiện dấu nhắc mysql> là thành công
```

---

### Bước 3 — Cài Git

1. Truy cập https://git-scm.com/downloads
2. Tải và cài đặt (Next liên tục, giữ mặc định)
3. Kiểm tra:

```bash
git --version
# Kết quả: git version 2.x.x
```

---

### Bước 4 — Cài Spring Tool Suite (STS)

1. Truy cập https://spring.io/tools
2. Tải **Spring Tools 4 for Eclipse**
3. Giải nén vào thư mục tùy chọn (VD: `C:\STS`)
4. Chạy file `SpringToolSuite4.exe`

> **Lưu ý:** Lần đầu mở STS sẽ hỏi chọn Workspace — chọn 1 thư mục trống để chứa project, ví dụ `C:\workspace`

---

## 📥 Cài đặt project

### Bước 1 — Clone repository

Mở terminal (hoặc Git Bash) và chạy:

```bash
git clone https://github.com/your-username/xettuyen2026.git
cd xettuyen2026
```

---

### Bước 2 — Import vào STS

1. Mở STS
2. Vào menu **File → Import...**
3. Chọn **Maven → Existing Maven Projects** → Next
4. Nhấn **Browse...** → chọn thư mục `xettuyen2026` vừa clone về
5. Tick vào `/pom.xml` → **Finish**
6. Chờ STS tải dependencies (góc dưới bên phải hiện tiến trình)

---

### Bước 3 — Update Maven

Sau khi import xong:

1. Click chuột phải vào tên project trong **Package Explorer**
2. Chọn **Maven → Update Project...**
3. Tick **Force Update of Snapshots/Releases**
4. Nhấn **OK** và chờ

> ✅ Nếu không còn lỗi đỏ ở project là thành công

---

## 🗄️ Cấu hình database

### Bước 1 — Tạo database

Mở terminal và chạy:

```bash
mysql -u root -p
```

Nhập password, sau đó gõ lệnh sau trong MySQL:

```sql
CREATE DATABASE xettuyen2026
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

EXIT;
```

---

### Bước 2 — Import cấu trúc bảng

Quay lại terminal (không phải MySQL):

```bash
# Windows
mysql -u root -p xettuyen2026 < data\xettuyen2026_empty.sql

# macOS / Linux
mysql -u root -p xettuyen2026 < data/xettuyen2026_empty.sql
```

Nhập password → không báo lỗi là thành công.

Kiểm tra:

```bash
mysql -u root -p -e "USE xettuyen2026; SHOW TABLES;"
# Phải hiện ra 8 bảng: xt_nganh, xt_thisinhxettuyen25, ...
```

---

### Bước 3 — Tạo bảng users (chưa có trong SQL)

Vào MySQL và chạy:

```sql
USE xettuyen2026;

CREATE TABLE users (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(50)  NOT NULL UNIQUE,
  password   VARCHAR(100) NOT NULL,
  ho_ten     VARCHAR(100),
  email      VARCHAR(100),
  role       VARCHAR(10)  NOT NULL DEFAULT 'user',
  enabled    TINYINT(1)   DEFAULT 1,
  created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo tài khoản admin mặc định (password: Admin@123)
INSERT INTO users (username, password, ho_ten, role)
VALUES ('admin', '$2a$10$FH6Atf.0kfssxzYprGebFe5yRA2B4K.a83ZG/yzb1X7Gsxni17LPG', 'Quản trị viên', 'admin');
```

> 💡 Password mặc định của tài khoản `admin` là `Admin@123`  
> Đổi ngay sau khi đăng nhập lần đầu.

---

### Bước 4 — Cấu hình kết nối

```bash
# Windows
copy src\main\resources\hibernate.cfg.xml.example src\main\resources\hibernate.cfg.xml

# macOS / Linux
cp src/main/resources/hibernate.cfg.xml.example src/main/resources/hibernate.cfg.xml
```

Mở file `src/main/resources/hibernate.cfg.xml` và sửa 2 dòng:

```xml
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">NHAP_PASSWORD_CUA_BAN_O_DAY</property>
```

> ⚠️ **Không commit file này lên Git** — file đã được thêm vào `.gitignore`

---

## ▶️ Chạy ứng dụng

### Trong STS:

1. Mở file `src/main/java/com/xettuyen2026/Main.java`
2. Click chuột phải → **Run As → Java Application**
3. Kiểm tra Console:

```
✅ Kết nối DB thành công!
```

4. Cửa sổ đăng nhập hiện ra → dùng tài khoản `admin` / `Admin@123`

### Nếu gặp lỗi kết nối DB:

| Lỗi                               | Nguyên nhân     | Cách sửa                         |
| --------------------------------- | --------------- | -------------------------------- |
| `Access denied for user 'root'`   | Sai password    | Kiểm tra lại `hibernate.cfg.xml` |
| `Unknown database 'xettuyen2026'` | Chưa tạo DB     | Chạy lại Bước 1 phần Cấu hình DB |
| `Communications link failure`     | MySQL chưa chạy | Khởi động MySQL Service          |

**Khởi động MySQL Service:**

```bash
# Windows
net start mysql80

# macOS
brew services start mysql

# Linux
sudo systemctl start mysql
```

---

## 📁 Cấu trúc project

```
xettuyen2026/
├── src/main/java/com/xettuyen2026/
│   ├── Main.java                  ← Entry point
│   ├── config/                    ← Cấu hình Hibernate, hằng số
│   ├── entity/                    ← Mapping 9 bảng DB
│   ├── dao/                       ← Truy vấn database
│   │   └── base/BaseDAO.java      ← CRUD dùng chung
│   ├── service/                   ← Business logic
│   ├── ui/                        ← Giao diện Swing
│   │   ├── common/                ← Component dùng lại (bảng, dialog...)
│   │   ├── nganhPanel.java        ← Màn hình quản lý ngành
│   │   └── thisinhPanel.java			← Màn hình quản lý thi sinh
│   └── util/                      ← Tiện ích (import Excel, mã hóa...)
├── src/main/resources/
│   ├── hibernate.cfg.xml.example  ← Mẫu cấu hình DB (commit)
│   └── hibernate.cfg.xml          ← Cấu hình thật (KHÔNG commit)
├── src/test/                      ← Unit test
├── data/
│   ├── xettuyen2026_empty.sql     ← Script tạo bảng
│   └── mau_*.xlsx                 ← File Excel mẫu để import
├── pom.xml                        ← Dependencies Maven
└── README.md
```

---

## 👥 Phân công

| Thành viên | Phụ trách                                                      |
| ---------- | -------------------------------------------------------------- |
| **Dev A**  | UI/Swing: `MainFrame`, `LoginForm`, tất cả `Panel` và `Dialog` |
| **Dev B**  | Backend: `Entity`, `DAO`, `Service`, logic xét tuyển           |

---

## 📐 Quy tắc làm việc nhóm

### Git workflow

```bash
# Trước khi bắt đầu làm việc — luôn pull code mới nhất
git pull origin main

# Tạo branch riêng cho từng tính năng
git checkout -b feature/ten-tinh-nang

# Sau khi làm xong
git add .
git commit -m "feat: mô tả ngắn những gì đã làm"
git push origin feature/ten-tinh-nang

# Tạo Pull Request trên GitHub để merge vào main
```

### Quy tắc đặt tên commit

```
feat: thêm tính năng mới
fix:  sửa lỗi
wip:  đang làm dở (work in progress)

Ví dụ:
feat: hoàn thành ThiSinhPanel với phân trang
fix:  sửa lỗi import Excel bị lỗi encoding
wip:  đang làm XetTuyenService
```

### Quy tắc code

- Tên biến, method: **tiếng Việt không dấu** (VD: `timKiem`, `luuThiSinh`)
- Comment TODO phải ghi rõ: `// TODO [TênBạn]: mô tả việc cần làm`
- Không commit thẳng vào `main`, phải qua Pull Request
- Không commit file `hibernate.cfg.xml`

---

## ❓ Hỏi đáp thường gặp

**Q: STS báo lỗi đỏ khắp nơi sau khi import?**  
A: Click chuột phải project → Maven → Update Project → OK. Nếu vẫn lỗi, thử Project → Clean.

**Q: Không tìm thấy file `hibernate.cfg.xml`?**  
A: Bạn cần tự copy từ file `.example`. Xem lại Bước 4 phần Cấu hình database.

**Q: Chạy Main.java báo `Table 'xettuyen2026.users' doesn't exist`?**  
A: Chạy lại câu SQL tạo bảng `users` ở Bước 3 phần Cấu hình database.

**Q: Quên password MySQL?**  
A: Xem hướng dẫn reset tại https://dev.mysql.com/doc/refman/8.0/en/resetting-permissions.html

**Q: Cách áp dụng scroll ngang khi có quá nhiều cột dữ liệu?**  
A: Huong dan ap dung scroll ngang cho cac panel khac (khong sua trong dot nay):
   - Bang dung chung da ho tro san trong:
     src/main/java/com/xettuyen2026/ui/common/PaginatedTable.java
   - Chi can sau khi tao bang:
     PaginatedTable styledTable = new PaginatedTable(COLUMNS);
     styledTable.enableHorizontalScroll(COLUMN_WIDTHS);
   - Trong do COLUMN_WIDTHS la mang int[] chua do rong tung cot theo dung thu tu hien thi.
   - Khong can doi mau sac hay sua renderer rieng neu chi muon co scroll ngang.
   - Vi du:
     private static final int[] COLUMN_WIDTHS = {60, 120, 240, 100, 100, 100};
   - Neu bang it cot thi co the khong can goi ham nay.
