-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: localhost    Database: xettuyen2026
-- ------------------------------------------------------
-- Server version	8.0.42

CREATE DATABASE IF NOT EXISTS `xettuyen2026` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `xettuyen2026`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `xt_bangquydoi`
--

DROP TABLE IF EXISTS `xt_bangquydoi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_bangquydoi` (
  `idqd` int NOT NULL AUTO_INCREMENT,
  `d_phuongthuc` varchar(45) DEFAULT NULL,
  `d_tohop` varchar(45) DEFAULT NULL,
  `d_mon` varchar(45) DEFAULT NULL,
  `d_diema` decimal(6,2) DEFAULT NULL,
  `d_diemb` decimal(6,2) DEFAULT NULL,
  `d_diemc` decimal(6,2) DEFAULT NULL,
  `d_diemd` decimal(6,2) DEFAULT NULL,
  `d_maquydoi` varchar(45) DEFAULT NULL,
  `d_phanvi` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idqd`),
  UNIQUE KEY `d_maquydoi_UNIQUE` (`d_maquydoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_bangquydoi`
--

LOCK TABLES `xt_bangquydoi` WRITE;
/*!40000 ALTER TABLE `xt_bangquydoi` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_bangquydoi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_diemcongxetuyen`
--

DROP TABLE IF EXISTS `xt_diemcongxetuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_diemcongxetuyen` (
  `iddiemcong` int unsigned NOT NULL AUTO_INCREMENT,
  `ts_cccd` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `manganh` varchar(20) DEFAULT '0.00',
  `matohop` varchar(10) DEFAULT '0.00',
  `phuongthuc` varchar(45) DEFAULT NULL,
  `diemCC` decimal(6,2) DEFAULT NULL,
  `diemUtxt` decimal(6,2) DEFAULT NULL,
  `diemTong` decimal(6,2) DEFAULT '0.00',
  `ghichu` text,
  `dc_keys` varchar(45) NOT NULL,
  PRIMARY KEY (`iddiemcong`),
  UNIQUE KEY `dc_keys_UNIQUE` (`dc_keys`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_diemcongxetuyen`
--

LOCK TABLES `xt_diemcongxetuyen` WRITE;
/*!40000 ALTER TABLE `xt_diemcongxetuyen` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_diemcongxetuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_diemthixettuyen`
--

DROP TABLE IF EXISTS `xt_diemthixettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_diemthixettuyen` (
  `iddiemthi` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sobaodanh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `d_phuongthuc` varchar(10) DEFAULT NULL,
  `TO` decimal(8,2) DEFAULT '0.00',
  `LI` decimal(8,2) DEFAULT '0.00',
  `HO` decimal(8,2) DEFAULT '0.00',
  `SI` decimal(8,2) DEFAULT '0.00',
  `SU` decimal(8,2) DEFAULT '0.00',
  `DI` decimal(8,2) DEFAULT '0.00',
  `VA` decimal(8,2) DEFAULT '0.00',
  `GDCD` decimal(8,2) DEFAULT '0.00',
  `N1_THI` decimal(8,2) DEFAULT NULL COMMENT 'Diem thi goc',
  `N1_CC` decimal(8,2) DEFAULT '0.00' COMMENT 'max(N1_Thi, N1_QD)',
  `CNCN` decimal(8,2) DEFAULT '0.00',
  `CNNN` decimal(8,2) DEFAULT '0.00',
  `TI` decimal(8,2) DEFAULT '0.00',
  `KTPL` decimal(8,2) DEFAULT '0.00',
  `NK_MON1` varchar(20) DEFAULT NULL,
  `NK_DIEM1` decimal(8,2) DEFAULT NULL,
  `NK_MON2` varchar(20) DEFAULT NULL,
  `NK_DIEM2` decimal(8,2) DEFAULT NULL,
  `NL1` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`iddiemthi`),
  UNIQUE KEY `cccd_UNIQUE` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_diemthixettuyen`
--

LOCK TABLES `xt_diemthixettuyen` WRITE;
/*!40000 ALTER TABLE `xt_diemthixettuyen` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_diemthixettuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nganh`
--

DROP TABLE IF EXISTS `xt_nganh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nganh` (
  `idnganh` int NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `tennganh` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `n_tohopgoc` varchar(3) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_chitieu` int NOT NULL DEFAULT '0',
  `n_diemsan` decimal(10,2) DEFAULT NULL,
  `n_diemtrungtuyen` decimal(10,2) DEFAULT NULL,
  `n_tuyenthang` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_dgnl` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_thpt` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `n_vsat` varchar(1) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `sl_xtt` int DEFAULT NULL,
  `sl_dgnl` int DEFAULT NULL,
  `sl_vsat` int DEFAULT NULL,
  `sl_thpt` varchar(45) COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idnganh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nganh`
--

LOCK TABLES `xt_nganh` WRITE;
/*!40000 ALTER TABLE `xt_nganh` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_nganh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nganh_tohop`
--

DROP TABLE IF EXISTS `xt_nganh_tohop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nganh_tohop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `matohop` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `th_mon1` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `hsmon1` tinyint DEFAULT NULL,
  `th_mon2` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `hsmon2` tinyint DEFAULT NULL,
  `th_mon3` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `hsmon3` tinyint DEFAULT NULL,
  `tb_keys` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'manganh_matohop',
  `N1` tinyint(1) DEFAULT NULL,
  `TO` tinyint(1) DEFAULT NULL,
  `LI` tinyint(1) DEFAULT NULL,
  `HO` tinyint(1) DEFAULT NULL,
  `SI` tinyint(1) DEFAULT NULL,
  `VA` tinyint(1) DEFAULT NULL,
  `SU` tinyint(1) DEFAULT NULL,
  `DI` tinyint(1) DEFAULT NULL,
  `TI` tinyint(1) DEFAULT NULL,
  `KHAC` tinyint(1) DEFAULT NULL,
  `KTPL` tinyint(1) DEFAULT NULL,
  `dolech` decimal(6,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_UNIQUE` (`tb_keys`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nganh_tohop`
--

LOCK TABLES `xt_nganh_tohop` WRITE;
/*!40000 ALTER TABLE `xt_nganh_tohop` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_nganh_tohop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nguyenvongxettuyen`
--

DROP TABLE IF EXISTS `xt_nguyenvongxettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_nguyenvongxettuyen` (
  `idnv` int NOT NULL AUTO_INCREMENT,
  `nn_cccd` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `nv_manganh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `nv_tt` int NOT NULL,
  `diem_thxt` decimal(10,5) DEFAULT NULL COMMENT 'đã cộng điểm môn chính',
  `diem_utqd` decimal(10,5) DEFAULT NULL COMMENT 'Điểm UTQD theo tổ họp sẽ khác nhau.',
  `diem_cong` decimal(6,2) DEFAULT NULL COMMENT 'Tong 3 mon chua tinh mon chinh + diem uu tien\\\\\\\\n',
  `diem_xettuyen` decimal(10,5) DEFAULT NULL COMMENT 'đã cộng điểm ưu tiên',
  `nv_ketqua` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `nv_keys` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `tt_phuongthuc` varchar(45) DEFAULT NULL,
  `tt_thm` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idnv`),
  UNIQUE KEY `nv_keys_UNIQUE` (`nv_keys`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nguyenvongxettuyen`
--

LOCK TABLES `xt_nguyenvongxettuyen` WRITE;
/*!40000 ALTER TABLE `xt_nguyenvongxettuyen` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_nguyenvongxettuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_thisinhxettuyen25`
--

DROP TABLE IF EXISTS `xt_thisinhxettuyen25`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_thisinhxettuyen25` (
  `idthisinh` int NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sobaodanh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `ho` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `ten` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `ngay_sinh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `dien_thoai` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `password` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `gioi_tinh` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT NULL,
  `noi_sinh` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `updated_at` date DEFAULT NULL,
  `doi_tuong` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `khu_vuc` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`idthisinh`),
  UNIQUE KEY `cccd_UNIQUE` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_thisinhxettuyen25`
--

LOCK TABLES `xt_thisinhxettuyen25` WRITE;
/*!40000 ALTER TABLE `xt_thisinhxettuyen25` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_thisinhxettuyen25` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_tohop_monthi`
--

DROP TABLE IF EXISTS `xt_tohop_monthi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `xt_tohop_monthi` (
  `idtohop` int NOT NULL AUTO_INCREMENT,
  `matohop` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci NOT NULL,
  `mon1` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `mon2` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `mon3` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `tentohop` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  PRIMARY KEY (`idtohop`),
  UNIQUE KEY `matohop_UNIQUE` (`matohop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_tohop_monthi`
--

LOCK TABLES `xt_tohop_monthi` WRITE;
/*!40000 ALTER TABLE `xt_tohop_monthi` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_tohop_monthi` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-11 16:08:42

-- ============================================================
-- DỮ LIỆU MẪU
-- ============================================================

-- xt_tohop_monthi
INSERT INTO `xt_tohop_monthi` (`matohop`, `mon1`, `mon2`, `mon3`, `tentohop`) VALUES
('A00', 'TO', 'LI', 'HO', 'Toán, Vật lý, Hóa học'),
('A01', 'TO', 'LI', 'N1', 'Toán, Vật lý, Tiếng Anh'),
('B00', 'TO', 'HO', 'SI', 'Toán, Hóa học, Sinh học'),
('B03', 'TO', 'VA', 'SI', 'Toán, Ngữ văn, Sinh học'),
('C00', 'VA', 'DI', 'SU', 'Ngữ văn, Lịch sử, Địa lí'),
('C01', 'TO', 'VA', 'HO', 'Toán, Ngữ văn, Hóa học'),
('C02', 'TO', 'VA', 'N1', 'Toán, Ngữ văn, Tiếng Anh'),
('C03', 'TO', 'VA', 'SU', 'Toán, Lịch sử, Ngữ văn'),
('C04', 'TO', 'VA', 'DI', 'Toán, Địa lí, Ngữ văn'),
('C19', 'VA', 'SU', 'GD', 'Văn - Sử - GDCD'),
('D01', 'TO', 'VA', 'N1', 'Toán, Tiếng Anh, Ngữ văn'),
('H00', 'VA', 'NK3', 'NK4', 'Ngữ văn, Hình hoa, Trang trí'),
('M01', 'VA', 'NK1', 'NK2', 'Ngữ văn, Kể chuyện - Đọc diễn cảm, Hát - Nhạc'),
('M02', 'VA', 'NK1', 'NK2', 'Ngữ văn, Kể chuyện - Đọc diễn cảm, Hát - Nhạc'),
('N01', 'VA', 'NK5', 'NK6', 'Ngữ văn, Hát - Nhạc cụ, Xướng âm - Thẩm âm');

-- xt_nganh
INSERT INTO `xt_nganh` (`manganh`, `tennganh`, `n_tohopgoc`, `n_chitieu`, `n_diemsan`) VALUES
('7140114', 'Quản lý giáo dục',       'D01', 40,  17.00),
('7140201', 'Giáo dục Mầm non',        'M01', 200, 20.00),
('7140202', 'Giáo dục Tiểu học',       'C01', 200, 21.00),
('7140205', 'Giáo dục chính trị',      'C01', 10,  23.00),
('7140209', 'Sư phạm Toán học',        'A00', 40,  24.50),
('7140211', 'Sư phạm Vật lý',          'A00', 10,  24.00),
('7140212', 'Sư phạm Hóa học',         'A00', 10,  24.00),
('7140213', 'Sư phạm Sinh học',        'B00', 10,  23.00),
('7140217', 'Sư phạm Ngữ văn',         'C01', 50,  24.00),
('7140215', 'Sư phạm Lịch sử',         'C00', 10,  25.00),
('7310401', 'Luật',                    'C01', 80,  22.00),
('7340101', 'Quản trị kinh doanh',     'A00', 120, 20.00),
('7380101', 'Luật Kinh tế',            'C01', 60,  21.50),
('7480201', 'Công nghệ thông tin',     'A00', 100, 21.00),
('7510301', 'Công nghệ kỹ thuật điện', 'A00', 60,  18.00),
('7510302', 'Kỹ thuật điện tử',        'A00', 60,  18.00),
('7610302', 'Thú y',                   'B00', 50,  18.00);

-- xt_nganh_tohop
INSERT INTO `xt_nganh_tohop` (`manganh`, `matohop`, `th_mon1`, `hsmon1`, `th_mon2`, `hsmon2`, `th_mon3`, `hsmon3`, `tb_keys`, `N1`, `TO`, `LI`, `HO`, `SI`, `VA`, `SU`, `DI`, `TI`, `dolech`) VALUES
('7140114', 'B03', 'TO', 3, 'VA', 3, 'SI', 1, '7140114_B03', 0, 1, 0, 0, 1, 1, 0, 0, 0, 0.00),
('7140114', 'C01', 'TO', 3, 'VA', 3, 'HO', 1, '7140114_C01', 0, 1, 0, 1, 0, 1, 0, 0, 0, 0.00),
('7140114', 'C02', 'TO', 3, 'VA', 3, 'N1', 1, '7140114_C02', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0.00),
('7140114', 'C03', 'TO', 3, 'VA', 3, 'SU', 1, '7140114_C03', 0, 1, 0, 0, 0, 1, 1, 0, 0, 0.00),
('7140114', 'C04', 'TO', 3, 'VA', 3, 'DI', 1, '7140114_C04', 0, 1, 0, 0, 0, 1, 0, 1, 0, 0.00),
('7140114', 'D01', 'TO', 3, 'VA', 3, 'N1', 1, '7140114_D01', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0.00),
('7140209', 'A00', 'TO', 3, 'LI', 3, 'HO', 1, '7140209_A00', 0, 1, 1, 1, 0, 0, 0, 0, 0, 0.00),
('7140209', 'A01', 'TO', 3, 'LI', 3, 'N1', 1, '7140209_A01', 1, 1, 1, 0, 0, 0, 0, 0, 0, 0.00),
('7140209', 'D01', 'TO', 3, 'VA', 3, 'N1', 1, '7140209_D01', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0.00),
('7480201', 'A00', 'TO', 3, 'LI', 3, 'HO', 1, '7480201_A00', 0, 1, 1, 1, 0, 0, 0, 0, 0, 0.00),
('7480201', 'A01', 'TO', 3, 'LI', 3, 'N1', 1, '7480201_A01', 1, 1, 1, 0, 0, 0, 0, 0, 0, 0.00),
('7480201', 'D01', 'TO', 3, 'VA', 3, 'N1', 1, '7480201_D01', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0.00),
('7340101', 'A00', 'TO', 3, 'LI', 3, 'HO', 1, '7340101_A00', 0, 1, 1, 1, 0, 0, 0, 0, 0, 0.00),
('7340101', 'B00', 'TO', 3, 'HO', 3, 'SI', 1, '7340101_B00', 0, 1, 0, 1, 1, 0, 0, 0, 0, 0.00),
('7340101', 'D01', 'TO', 3, 'VA', 3, 'N1', 1, '7340101_D01', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0.00),
('7310401', 'C01', 'TO', 3, 'VA', 3, 'HO', 1, '7310401_C01', 0, 1, 0, 1, 0, 1, 0, 0, 0, 0.00),
('7310401', 'D01', 'TO', 3, 'VA', 3, 'N1', 1, '7310401_D01', 1, 1, 0, 0, 0, 1, 0, 0, 0, 0.00),
('7610302', 'B00', 'TO', 3, 'HO', 3, 'SI', 1, '7610302_B00', 0, 1, 0, 1, 1, 0, 0, 0, 0, 0.00);

-- xt_thisinhxettuyen25
INSERT INTO `xt_thisinhxettuyen25` (`cccd`, `sobaodanh`, `ho`, `ten`, `ngay_sinh`, `dien_thoai`, `password`, `gioi_tinh`, `email`, `noi_sinh`, `updated_at`, `doi_tuong`, `khu_vuc`) VALUES
('001207004846', '24001001', 'Nguyễn Văn', 'An',    '01/01/2007', '0901234561', '123456', 'Nam',  'an.nguyen@email.com',   'Hà Nội',     '2026-03-01', '01', 'KV1'),
('001207004848', '24001002', 'Trần Thị',   'Bình',  '02/02/2007', '0901234562', '123456', 'Nữ',   'binh.tran@email.com',   'Hà Nội',     '2026-03-01', '01', 'KV2'),
('001207005157', '24001003', 'Lê Thị',     'Chi',   '03/03/2007', '0901234563', '123456', 'Nữ',   'chi.le@email.com',      'Hải Phòng',  '2026-03-01', '00', 'KV2NT'),
('001207008593', '24001004', 'Phạm Văn',   'Dũng',  '04/04/2007', '0901234564', '123456', 'Nam',  'dung.pham@email.com',   'Nam Định',   '2026-03-01', '02', 'KV3'),
('001207008630', '24001005', 'Hoàng Thị',  'Em',    '05/05/2007', '0901234565', '123456', 'Nữ',   'em.hoang@email.com',    'Nghệ An',    '2026-03-01', '00', 'KV1'),
('001207009704', '24001006', 'Vũ Văn',     'Phúc',  '06/06/2007', '0901234566', '123456', 'Nam',  'phuc.vu@email.com',     'Hà Nội',     '2026-03-01', '01', 'KV2NT'),
('001207011458', '24001007', 'Đặng Thị',   'Giang', '07/07/2007', '0901234567', '123456', 'Nữ',   'giang.dang@email.com',  'Thái Bình',  '2026-03-01', '00', 'KV2'),
('001207012341', '24001008', 'Bùi Văn',    'Hải',   '08/08/2007', '0901234568', '123456', 'Nam',  'hai.bui@email.com',     'Hà Nam',     '2026-03-01', '00', 'KV1'),
('001207012439', '24001009', 'Ngô Thị',    'Lan',   '09/09/2007', '0901234569', '123456', 'Nữ',   'lan.ngo@email.com',     'TP.HCM',     '2026-03-01', '01', 'KV3'),
('001207012684', '24001010', 'Đinh Văn',   'Minh',  '10/10/2007', '0901234570', '123456', 'Nam',  'minh.dinh@email.com',   'Đà Nẵng',    '2026-03-01', '00', 'KV2NT');

-- xt_diemthixettuyen
INSERT INTO `xt_diemthixettuyen` (`cccd`, `sobaodanh`, `d_phuongthuc`, `TO`, `LI`, `HO`, `SI`, `SU`, `DI`, `VA`, `GDCD`, `N1_THI`, `N1_CC`, `CNCN`, `CNNN`, `TI`, `KTPL`, `NK_MON1`, `NK_DIEM1`, `NK_MON2`, `NK_DIEM2`, `NL1`) VALUES
('001207004846', '24001001', 'THPT', 8.40, 7.80, 8.00, NULL, NULL, NULL, 7.20, NULL, 7.50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 820.00),
('001207004848', '24001002', 'THPT', 9.20, 8.40, NULL, NULL, NULL, NULL, 7.60, NULL, 7.00, 8.50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 910.00),
('001207005157', '24001003', 'THPT', 7.80, NULL, NULL, NULL, NULL, NULL, 8.20, NULL, 8.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 750.00),
('001207008593', '24001004', 'THPT', 8.00, NULL, 7.25, NULL, NULL, NULL, 7.50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 780.00),
('001207008630', '24001005', 'THPT', 6.50, NULL, 6.75, 7.25, NULL, NULL, 7.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 690.00),
('001207009704', '24001006', 'THPT', 7.00, NULL, NULL, NULL, 7.80, 8.00, 8.50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 760.00),
('001207011458', '24001007', 'THPT', 6.75, NULL, NULL, NULL, 8.25, NULL, 7.75, 7.50, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 710.00),
('001207012341', '24001008', 'THPT', NULL, NULL, NULL, NULL, NULL, NULL, 8.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'NK1', 7.50, 'NK2', 8.00, NULL),
('001207012439', '24001009', 'DGNL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 950.00),
('001207012684', '24001010', 'THPT', 7.25, NULL, NULL, NULL, NULL, NULL, 7.75, NULL, 7.50, NULL, NULL, NULL, 8.00, 6.75, NULL, NULL, NULL, NULL, 720.00),
('001207015001', '24001011', 'THPT', 8.75, 9.00, NULL, NULL, NULL, NULL, 7.50, NULL, NULL, 8.75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 870.00),
('001207015002', '24001012', 'THPT', 7.50, NULL, 7.00, NULL, NULL, NULL, 8.00, NULL, 7.25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 730.00),
('001207015003', '24001013', 'THPT', 8.25, NULL, NULL, 8.50, NULL, NULL, 7.75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 810.00),
('001207015004', '24001014', 'THPT', 6.00, NULL, NULL, NULL, 6.50, 7.00, 7.25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 650.00),
('001207015005', '24001015', 'DGNL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 980.00),
('001207015006', '24001016', 'THPT', 7.00, NULL, NULL, NULL, 7.50, NULL, 8.25, 7.75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 700.00),
('001207015007', '24001017', 'THPT', 8.00, NULL, 8.25, NULL, NULL, NULL, 7.50, NULL, 8.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 800.00),
('001207015008', '24001018', 'THPT', 7.75, 7.50, NULL, NULL, NULL, NULL, 8.50, NULL, 7.25, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 770.00),
('001207015009', '24001019', 'THPT', NULL, NULL, NULL, NULL, NULL, NULL, 8.75, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'NK3', 8.25, 'NK4', 7.75, NULL),
('001207015010', '24001020', 'THPT', 6.25, NULL, NULL, 6.75, NULL, NULL, 7.00, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 640.00);

-- xt_diemcongxetuyen
INSERT INTO `xt_diemcongxetuyen` (`ts_cccd`, `manganh`, `matohop`, `phuongthuc`, `diemCC`, `diemUtxt`, `diemTong`, `ghichu`, `dc_keys`) VALUES
('056307010216', '7340101', 'B00', 'PT4', NULL, NULL, 1.00, NULL, '056307010216_7340101_B00'),
('056307010216', '7340101', 'X22', 'PT4', NULL, NULL, 1.00, NULL, '056307010216_7340101_X22'),
('056307010216', '7340101', 'B02', 'PT4', NULL, NULL, 1.00, NULL, '056307010216_7340101_B02'),
('056307010216', '7340101', 'B01', 'PT4', NULL, NULL, 1.00, NULL, '056307010216_7340101_B01'),
('056307010216', '7340101', 'A07', 'PT4', NULL, NULL, 1.00, NULL, '056307010216_7340101_A07'),
('001207008593',  '7310401', 'C01', 'PT2', NULL, NULL, 0.50, NULL, '001207008593_7310401_C01'),
('001207008630',  '7480201', 'D01', 'PT2', NULL, NULL, 0.75, NULL, '001207008630_7480201_D01'),
('001207009704',  '7310401', 'D01', 'PT2', NULL, NULL, 0.50, NULL, '001207009704_7310401_D01'),
('001207012439',  '7480201', 'A01', 'PT2', NULL, NULL, 0.61, NULL, '001207012439_7480201_A01'),
('001207012341',  '7380101', 'D01', 'PT2', NULL, NULL, 0.00, NULL, '001207012341_7380101_D01');

-- xt_nguyenvongxettuyen
INSERT INTO `xt_nguyenvongxettuyen` (`nn_cccd`, `nv_manganh`, `nv_tt`, `diem_thxt`, `diem_utqd`, `diem_cong`, `diem_xettuyen`, `nv_ketqua`, `nv_keys`, `tt_phuongthuc`, `tt_thm`) VALUES
('001207004846', '7610302', 1,  19.50000, 0.25000, NULL, 19.75000, 'duolaar', '001207004846_7610302_PT2', 'PT2', NULL),
('001207005157', '7510301', 1,  16.70000, 0.25000, NULL, 16.95000, 'duolaar', '001207005157_7510301_PT2', 'PT2', NULL),
('001207005157', '7510302', 2,  15.70000, 0.00000, NULL, 15.70000, 'duolaar', '001207005157_7510302_PT2', 'PT2', NULL),
('001207005157', '7220201', 3,  15.70000, 0.00000, NULL, 15.70000, 'duolaar', '001207005157_7220201_PT2', 'PT2', NULL),
('001207008593', '7310401', 1,  20.25000, 0.00000, NULL, 20.25000, NULL,      '001207008593_7310401_PT2', 'PT2', NULL),
('001207008630', '7310401', 1,  21.18000, 0.00000, NULL, 21.18000, NULL,      '001207008630_7310401_PT2', 'PT2', NULL),
('001207009704', '7310401', 1,  21.85000, 0.00000, NULL, 21.85000, 'yes',     '001207009704_7310401_PT2', 'PT2', NULL),
('001207012341', '7380101', 1,  19.93000, 0.00000, NULL, 19.93000, 'yes',     '001207012341_7380101_PT2', 'PT2', NULL),
('001207012439', '7480201', 1,  23.85000, 0.61000, NULL, 24.46000, 'yes',     '001207012439_7480201_PT2', 'PT2', NULL),
('001207012684', '7480201', 2,  20.10000, 0.00000, NULL, 20.10000, NULL,      '001207012684_7480201_PT2', 'PT2', NULL);

-- xt_bangquydoi
INSERT INTO `xt_bangquydoi` (`d_phuongthuc`, `d_tohop`, `d_mon`, `d_diema`, `d_diemb`, `d_diemc`, `d_diemd`, `d_maquydoi`, `d_phanvi`) VALUES
('DGNL', 'A01', NULL, 980.00, 997.00, 20.25, 20.10, 'DGNL_A01_2',  '2'),
('DGNL', 'A01', NULL, 984.00, 997.00, 25.75, 26.10, 'DGNL_A01_3',  '3'),
('DGNL', 'A01', NULL, 979.00, 983.00, 25.00, 25.65, 'DGNL_A01_4',  '4'),
('DGNL', 'A01', NULL, 962.00, 972.00, 25.05, 25.25, 'DGNL_A01_5',  '5'),
('DGNL', 'A01', NULL, 954.00, 961.00, 24.85, 25.05, 'DGNL_A01_6',  '6'),
('DGNL', 'A01', NULL, 946.00, 953.00, 24.00, 24.75, 'DGNL_A01_7',  '7'),
('DGNL', 'A01', NULL, 939.00, 945.00, 24.30, 24.50, 'DGNL_A01_8',  '8'),
('DGNL', 'A01', NULL, 932.00, 938.00, 24.00, 24.25, 'DGNL_A01_9',  '9'),
('DGNL', 'A01', NULL, 926.00, 931.00, 23.60, 24.20, 'DGNL_A01_10', '10'),
('DGNL', 'A01', NULL, 919.00, 925.00, 23.30, 23.95, 'DGNL_A01_11', '11'),
('DGNL', 'A01', NULL, 913.00, 918.00, 23.55, 23.75, 'DGNL_A01_12', '12'),
('THPT', 'A00', NULL, 27.00,  30.00,  27.00, 27.00, 'THPT_A00_1',  '1'),
('THPT', 'A00', NULL, 25.00,  26.99,  25.00, 25.00, 'THPT_A00_2',  '2'),
('THPT', 'D01', NULL, 27.00,  30.00,  27.00, 27.00, 'THPT_D01_1',  '1'),
('THPT', 'D01', NULL, 25.00,  26.99,  25.00, 25.00, 'THPT_D01_2',  '2');


DROP TABLE IF EXISTS `user_groups`;

CREATE TABLE user_groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_nhom VARCHAR(100) NOT NULL,         
    ma_nhom VARCHAR(50) UNIQUE,              
    loai_nhom VARCHAR(50),                
    mo_ta TEXT,
    parent_id INT,                        
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES user_groups(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Trường
INSERT INTO user_groups (ten_nhom, ma_nhom, loai_nhom)
VALUES ('Đại học Sài Gòn', 'SGU', 'truong');

-- Khoa
INSERT INTO user_groups (ten_nhom, ma_nhom, loai_nhom, parent_id)
VALUES ('Khoa Công nghệ thông tin', 'CNTT', 'khoa', 1);

-- Ngành
INSERT INTO user_groups (ten_nhom, ma_nhom, loai_nhom, parent_id)
VALUES ('Công nghệ thông tin', '7480201', 'nganh', 2);

-- Phòng ban
INSERT INTO user_groups (ten_nhom, ma_nhom, loai_nhom)
VALUES ('Phòng tuyển sinh', 'PTS', 'phongban');

DROP TABLE IF EXISTS `users`;

CREATE TABLE users (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(50)  NOT NULL UNIQUE,
  password   VARCHAR(100) NOT NULL,
  ho_ten     VARCHAR(100),
  email      VARCHAR(100),
  role       VARCHAR(10)  NOT NULL DEFAULT 'user',
  enabled    TINYINT(1)   DEFAULT 1,
  created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
  group_id INT,

  CONSTRAINT fk_user_group
    FOREIGN KEY (group_id)
    REFERENCES user_groups(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo tài khoản admin mặc định (password: Admin@123)
INSERT INTO users (username, password, ho_ten, role)
VALUES ('admin', '$2a$10$FH6Atf.0kfssxzYprGebFe5yRA2B4K.a83ZG/yzb1X7Gsxni17LPG', 'Quản trị viên', 'admin');