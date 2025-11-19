-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 29, 2025 at 06:18 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `safepassage`
--

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admins`
--

INSERT INTO `admins` (`id`, `username`, `password_hash`, `created_at`) VALUES
(1, 'admin', '$2y$10$7BvwTwOV.jWmI6phV8D5Se9xP4jflo.gmrusGm2IzntlxBhEL1IX2', '2025-08-23 05:48:31');

-- --------------------------------------------------------

--
-- Table structure for table `cards`
--

CREATE TABLE `cards` (
  `id` int(11) NOT NULL,
  `car_id` int(11) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `title` varchar(200) NOT NULL,
  `card_holder_name` varchar(150) DEFAULT NULL,
  `card_number` varchar(255) DEFAULT NULL,
  `expiration_date` varchar(10) DEFAULT NULL,
  `cvv` varchar(255) DEFAULT NULL,
  `pin` varchar(255) DEFAULT NULL,
  `note` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `cat_id` int(11) NOT NULL,
  `cat_name` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id`, `cat_id`, `cat_name`, `created_at`) VALUES
(1, 1, 'Social Media', '2025-08-23 05:48:31'),
(2, 2, 'Banking', '2025-08-23 05:48:31'),
(3, 3, 'Shopping', '2025-08-23 05:48:31'),
(4, 4, 'Work', '2025-08-23 05:48:31'),
(5, 5, 'Personal', '2025-08-23 05:48:31'),
(6, 6, 'Gaming', '2025-08-23 05:48:31'),
(7, 7, 'Email', '2025-08-23 05:48:31'),
(8, 8, 'Other', '2025-08-23 05:48:31');

-- --------------------------------------------------------

--
-- Table structure for table `documents`
--

CREATE TABLE `documents` (
  `id` int(11) NOT NULL,
  `document_id` int(11) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `title` varchar(200) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint(20) NOT NULL,
  `mime_type` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `passwords`
--

CREATE TABLE `passwords` (
  `id` int(11) NOT NULL,
  `pass_id` int(11) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `title` varchar(200) NOT NULL,
  `user_name` varchar(150) DEFAULT NULL,
  `email` varchar(190) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `password` text DEFAULT NULL,
  `url` varchar(500) DEFAULT NULL,
  `package_name` varchar(200) DEFAULT NULL,
  `note` text DEFAULT NULL,
  `cat_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `last_used` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `pins`
--

CREATE TABLE `pins` (
  `id` int(11) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `pin_hash` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pins`
--

INSERT INTO `pins` (`id`, `user_id`, `pin_hash`, `created_at`) VALUES
(1, 'user_68a9654d7750f8.70312284', '', '2025-08-24 10:24:00'),
(2, 'user_68a9654ee1c0e4.84966849', '', '2025-08-24 10:24:00'),
(3, 'user_68a96550490588.30703287', '', '2025-08-24 10:24:00'),
(4, 'LmJXBP4ibxOIgbkTQz5p1XWZqQk2', '', '2025-08-24 10:24:00'),
(5, 'test_68a9669e2172a', '', '2025-08-24 10:24:00'),
(6, 'complete_test_68a969dcbf341', '', '2025-08-24 10:24:00'),
(7, 'complete_test_68a989e4c45a0', '', '2025-08-24 10:24:00'),
(8, 'log_test_68a98a7385aa8', '', '2025-08-24 10:24:00'),
(17, '77eIAgMvcXNc8t72eePIVqGulZA3', '$2y$10$UlvvV5/3zzv5ugZ9XGixLOlCGOcW16LDt.eLcSnBiYv/.vWKyfgZO', '2025-08-23 10:09:41'),
(18, '6tjtbZfclzVwajgs6Ik7lfGXEgi2', '$2y$10$/08klGBFYAVll0HSRGgCE.3uYF3ZyYaLKrWvvCRniucubBmZV621m', '2025-08-23 16:25:40'),
(19, 'user_1755978526684_9307', '$2y$10$cFbNNt21IkTZ5uRoCHPRy.XfZzidysErCSWfQeNTZkXWZQwwqmbmi', '2025-08-23 19:48:54'),
(20, 'user_1755979447934_1856', '$2y$10$nMt0VY6VPuUwvxHiQm2k4OG45hQx2C8SbMNoEDoPezhPfJhoq0ZSy', '2025-08-23 20:04:14'),
(21, 'user_1755979797690_9481', '$2y$10$PYAtqpbRm9mRVtz0YragtOEFX5DWy9vM.SGAprO45f0Bgq2uehX.G', '2025-08-23 20:10:05'),
(22, 'user_1755979986200_7828', '$2y$10$Ll56ksHJlRCdQVPuV.EoDOiSUvwxdeaY4JKAKM1IhaEto18JIh6Uq', '2025-08-23 20:13:11'),
(23, 'user_1755980055816_4570', '$2y$10$OLC2L1xKKC3/OnhkXhF4Ue4nVaGcXtC03Z83pZIxJwjIVgaFgT8ri', '2025-08-23 20:14:20'),
(24, 'user_1755980095805_5188', '$2y$10$LpSXqiq53NxM0wSo0AD2pu7o7cidNETMnbLZglb.4eZJBFRTIn8Ii', '2025-08-23 20:15:00'),
(25, 'user_1755980563822_8960', '$2y$10$l1n3v4iwzkHibFbQYgaZfuF/P04c4JZSH5YQMw3U8mHpKSWCLYW7S', '2025-08-23 20:23:07'),
(26, 'user_1755981572237_8989', '$2y$10$CuxcQHPZ7bPqe.oPmo.GZOeyg.HcGJM9hk9cow8OlqanK9RIVg4zy', '2025-08-23 20:39:52'),
(27, 'user_1755981894845_9996', '$2y$10$QaN.42rPGFXKOtjPoWBLPeWhHzl42mBR2hMfoSSmCLeupFJ.QUBhK', '2025-08-23 20:45:24'),
(28, 'user_1755982024459_4231', '$2y$10$dskHbllBtalLmjnjjU0UQ.nimiT3Xc6OHrCSJVbWUTIoDqSTDySAy', '2025-08-23 20:47:21'),
(29, 'user_1755982271787_907', '$2y$10$Ds6nMigANvxu9o610OkyU.bpIIEwBJP7iEBo01IUJNwQ9BIsNKKVu', '2025-08-23 20:51:31'),
(30, 'user_1755982386210_1834', '$2y$10$fkTbhI3IybtuouQ1nJQYgej7X/DWKUo7nX7.CrpmfHTOWBZpX4yLa', '2025-08-23 20:53:30'),
(31, 'user_1755982658835_2427', '$2y$10$/l0kxdzhaVCiIimLkn0fFO3lWCR1QUSY0ZbTbrWBJwu2t6eFiTegi', '2025-08-23 20:58:13'),
(32, 'user_1755982956140_7944', '$2y$10$ycJ/KDg8hI8H9swZFNSIUOOshtnluv8l2/Z3MtJD7qPnDWXbthY4.', '2025-08-23 21:04:04'),
(33, 'user_1755986019032_1727', '$2y$10$7cZ7w3r.zlZPwDukXXeEUew4Qfv7FXw.0ckI0DE6ksXilsu.AkRpu', '2025-08-23 21:54:08'),
(34, 'user_1755986637066_4374', '$2y$10$K6Mni5lNpRBZxaVpNoOlsud3w3R4xBuhNT6nyyYRg0R.gCUQ3SgQK', '2025-08-23 22:04:32'),
(35, 'user_1755987015857_3233', '$2y$10$HKwNstu/owlTa6cFxbdbtu5f0qAzmjjXGvNm/npGmaRHLZNyJ7xmm', '2025-08-23 22:10:43'),
(36, 'user_1755987137259_5768', '$2y$10$1JToa3o02OoJ5L6INshtS.eA2Y8cwcWLncUITmKKdYQtP8eDqFJ5m', '2025-08-23 22:12:44'),
(37, 'user_1755987196787_8300', '$2y$10$SlKxjCz7uv0qdAyLsvOXd.gwZwms.Gt28Mx5GTxjkIcX1ORI/GvHS', '2025-08-23 22:13:39'),
(38, 'user_1755987253417_5425', '$2y$10$0MjgA0Rmels5rRvcm7ZWbu2ebqa3eoCezDyUNgE8iYu2nvPwB8gSu', '2025-08-23 22:14:33'),
(39, 'user_1756024916114_6999', '$2y$10$fBGs02gn.ynrplPK5FPGBuetWpGSth4ZO7jgsit3QnNw8jXgp1ttC', '2025-08-24 08:42:20'),
(40, 'user_1756024978597_1451', '$2y$10$YhXqfkKM17mnsOKaxPsILu9Di3dhWKgFQc7XbT4SJ7JxtYON5eUeG', '2025-08-24 08:43:23'),
(41, 'user_1756028922650_1801', '$2y$10$9cpbUQKdsnNSgX7FeA.lCuJNpgBNxqzWZ319K83NQq84Jd1S5/Zou', '2025-08-24 09:48:59'),
(42, 'user_1756029093669_3249', '$2y$10$2L2wZwvM5jyKKpCqi7TUC.3w4xYIY1VigiDh.1Uk4qZ2RJmNufvs6', '2025-08-24 09:51:50'),
(53, 'user_1756031966707_6900', '$2y$10$ngpS14XQ19Op2hCpT4px8.m6EZxmbp65m/n30Y1BXOS1uvODxaie2', '2025-08-24 12:49:03'),
(57, 'user_1756040752582_4883', '$2y$10$b/Adr5Pcylt0w1LNoKRtzeBc.gbTr9d8URaOCx6KSqLG0Kr..qN4G', '2025-08-24 13:06:04'),
(58, 'user_1756040950476_3860', '$2y$10$DGad3JwfOrF98xwWSFWTr.qBJe8qMEWor4/.CQiJHBf3ZRysbYsly', '2025-08-24 13:09:26'),
(59, 'user_1756041954475_3477', '$2y$10$dWUJCkwPi/j0sW6VRF9d7.fVwjVwryT4VstbZpZ2km4ieit5kCnEC', '2025-08-24 13:26:10'),
(60, 'user_1756042790814_8368', '$2y$10$Oj3Je559LbB4Qw0HgZp4Ieoz1.PK.ZjJbZrNfav4jvasYLrXTt8ES', '2025-08-24 13:41:50'),
(61, 'user_1756045218018_5790', '$2y$10$LZDPFuOtleNRNf7D5AGZsODDBtwlhpIG1OYhCs5T7xdgck.R66ZJO', '2025-08-24 14:20:33'),
(62, 'user_1756045448877_8399', '$2y$10$RgqgelbE8afTe8Z/IBBdx.DByy5UpD268KSr4dKdsCfF.ydy./Y7G', '2025-08-24 14:24:26'),
(63, 'user_1756482488536_2871', '$2y$10$8v./wrYUdvUVT0EApsiIlOyhzPv.h5cE2hUkEVxbdnwFOwYD2eYz.', '2025-08-29 15:48:25');

-- --------------------------------------------------------

--
-- Table structure for table `pins_backup`
--

CREATE TABLE `pins_backup` (
  `id` int(11) NOT NULL DEFAULT 0,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `pin_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `encrypted_pin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `pins_backup`
--

INSERT INTO `pins_backup` (`id`, `user_id`, `pin_hash`, `encrypted_pin`, `created_at`) VALUES
(1, 'user_68a9654d7750f8.70312284', '$2y$10$uNVjGZu0WuDKJXrDXlC8o.CuRF80FmNAjUjDGMtbY9aT1a3xSjuyu', '', 0),
(2, 'user_68a9654ee1c0e4.84966849', '$2y$10$o5iGGHLSj.m40xIk4DdRJOAJncNjX6IaqYqa9fwu.mx3j0GyKvv4a', '', 0),
(3, 'user_68a96550490588.30703287', '$2y$10$njKW.DSsQWVfnxHX5QLexevhW7.qtQZoZxgAq4QGcECaMoHdzC6tq', '', 0),
(4, 'LmJXBP4ibxOIgbkTQz5p1XWZqQk2', '$2y$10$Xt.HkkEbm4pdwnxmSZoDnu.b/YK.ojCRYbFoU5p4HYweRVvP/SttS', '', 0),
(5, 'test_68a9669e2172a', '$2y$10$XL36ATQn9WMPdZ8ABZUd4uiTx3CnEqCOLsZhswaeYXYf3s0b9v99C', '', 0),
(6, 'complete_test_68a969dcbf341', '$2y$10$OXErCt03E9MMf2YsBQx6deQJZvqFp0UsLlVcu8YnZ4wRkPGjVKjpy', '', 0),
(7, 'complete_test_68a989e4c45a0', '$2y$10$E15hvRJu9csX9fYLYQGgPOnBblr5qwkRS81IDi13JaU6S7z/gq1ZS', '', 0),
(8, 'log_test_68a98a7385aa8', '$2y$10$rqtcmoNklErpXUr7ZQQaeOdC30Ct3hE7t/o7lIbcEWE4SX3nuALlO', '', 0);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `email` varchar(190) NOT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `user_name` varchar(150) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `is_active` tinyint(1) DEFAULT 1,
  `last_login` timestamp NULL DEFAULT NULL,
  `login_count` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `user_id`, `email`, `password_hash`, `user_name`, `created_at`, `updated_at`, `is_active`, `last_login`, `login_count`) VALUES
(1, 'user_68a9654d7750f8.70312284', 'test32@gmail.com', '$2y$10$KunpFp.QYOVu6U8HFGfMhePKUj8xkcUO98fq6nEtmZY8KL8XkCWmC', 'test32', '2025-08-23 06:53:01', '2025-08-23 15:42:59', 0, NULL, 0),
(2, 'user_68a9654ee1c0e4.84966849', 'test33@gmail.com', '$2y$10$o5qzCDY7LUgZqq8axn4tHe8kJ5zLPd1cqhOr0Vpmovz.wL6Nycf9C', 'test33', '2025-08-23 06:53:02', '2025-08-23 06:53:02', 1, NULL, 0),
(3, 'user_68a96550490588.30703287', 'test34@gmail.com', '$2y$10$3jE9.LLvdu5ZNxHxJlCEsO87ZMPsr8tDwm3aYjCQ5wu3ZpVaA4CUG', 'test34', '2025-08-23 06:53:04', '2025-08-23 06:53:04', 1, NULL, 0),
(7, 'LmJXBP4ibxOIgbkTQz5p1XWZqQk2', 'test35@gmail.com', NULL, 'Test35', '2025-08-23 06:56:08', '2025-08-23 06:56:08', 1, NULL, 0),
(8, 'test_68a9669e2172a', 'test1755932318@example.com', NULL, 'Test User 1755932318', '2025-08-23 06:58:38', '2025-08-23 06:58:38', 1, NULL, 0),
(9, 'test_68a9677ce8066', 'test1755932540@example.com', '$2y$10$RnJEck6CFiUXX9mDw482MuqPqZD5uNAOAJItWyencS4fcs70N8Hiq', 'Test User 1755932540', '2025-08-23 07:02:21', '2025-08-23 07:02:21', 1, NULL, 0),
(10, 'api_test_68a9677d17b77', 'apitest1755932541@example.com', '$2y$10$iJ7.gioDHHOp8U33tE/Tb.5f431O.52QMwj5gXD/K2mdG3T.LVmOe', 'API Test User 1755932541', '2025-08-23 07:02:21', '2025-08-23 07:02:21', 1, NULL, 0),
(11, 'test_68a9679584fda', 'test1755932565@example.com', '$2y$10$Rsti8JBTDxil1Y0qOhsfj.VYCb20VxsiloltjpGT6n0kR7WTt1lIO', 'Test User 1755932565', '2025-08-23 07:02:45', '2025-08-23 07:02:45', 1, NULL, 0),
(12, 'api_test_68a96795a2a59', 'apitest1755932565@example.com', '$2y$10$4zoBef7BIsZxshJIcmP.xuhQhBSexNFidd.CoAFszCWG8cXCohLV6', 'API Test User 1755932565', '2025-08-23 07:02:45', '2025-08-23 07:02:45', 1, NULL, 0),
(13, '5TWX9sTDGiYbLOihrJtu46SJtbb2', 'test36@gmail.com', '$2y$10$G/D4.SCDEhgfFYcOz36m7.VwM6mqiwcDaJRK0H2Kkbs8ZOZKTB1my', 'test36', '2025-08-23 07:04:37', '2025-08-23 07:04:37', 1, NULL, 0),
(14, '9M7ut3KrkvQzOHwe8N6CuRV0n7U2', 'test37@gmail.com', '$2y$10$YXvCosNJ22ydJgnKmrV1HOijH6427rr3upWL5boPQ4d0w/kuVmI/O', 'Test36', '2025-08-23 07:06:17', '2025-08-23 07:06:17', 1, NULL, 0),
(15, 'complete_test_68a969dcbf341', 'completetest1755933148@example.com', '$2y$10$kR72E/3qyu3qZLensIoB1.asHcUf69cm5Cs9iypMsg6VhY84mz.5u', 'Complete Test User 1755933148', '2025-08-23 07:12:28', '2025-08-23 07:12:28', 1, NULL, 0),
(16, '2tSy0GWY7VhLHd2mOIvOoP4WgZm2', 'test38@gmail.com', '$2y$10$7uvXMRpM2uBPETlXr25SbuBUsWTSrRD49piEdfQqLx.Mt6mTT3446', 'Test38', '2025-08-23 09:28:00', '2025-08-23 19:48:18', 1, '2025-08-23 19:48:18', 8),
(17, 'complete_test_68a989e4c45a0', 'completetest1755941348@example.com', '$2y$10$9gijVFdqmGRS4015pz5fDubCNoqYXXwBZ8gmkEwtmGQKJ92lEUMMy', 'Complete Test User 1755941348', '2025-08-23 09:29:08', '2025-08-23 15:43:15', 0, NULL, 0),
(18, 'log_test_68a98a7385aa8', 'logtest1755941491@example.com', '$2y$10$jOFUbXhptx3suqjXSltTSO7yaHnf.Tl0X0Nz6vUNi4iVABNPbQdty', 'Log Test User 1755941491', '2025-08-23 09:31:31', '2025-08-23 14:12:30', 1, NULL, 0),
(19, '77eIAgMvcXNc8t72eePIVqGulZA3', 'test39@gmail.com', '$2y$10$0jHHGYqjlbS9Zez7orcNe.DWGSvP7wP9am.DuvLXoNJLwpsQ6TeWW', 'test', '2025-08-23 10:09:36', '2025-08-23 19:16:35', 0, NULL, 0),
(20, '6tjtbZfclzVwajgs6Ik7lfGXEgi2', 'test40@gmail.com', '$2y$10$RJ6G.luq9LLN/xPF7/iKyekWO4h4rSDrrHj9a1XAzhrl/7mzhaKam', 'test40', '2025-08-23 16:25:35', '2025-08-23 19:16:27', 0, NULL, 0),
(21, 'user_1755978526684_9307', 'test41@gmail.com', '$2y$10$cOJw3IiT7Pd3m2WvQtEQruMA8PQQpiJCxQ13w.5DGxujs/G7x81Nm', 'Test1234', '2025-08-23 19:48:45', '2025-08-23 20:05:14', 1, '2025-08-23 20:05:14', 3),
(22, 'user_1755979447934_1856', 'test42@gmail.com', '$2y$10$4h5v0jZz0raleByIG0yjCOHdM2gHf7Dex2oe4Ft1tp1q9IhfweIrm', 'test42', '2025-08-23 20:04:07', '2025-08-23 20:09:36', 1, NULL, 0),
(24, 'user_1755979797690_9481', 'test43@gmail.com', '$2y$10$nkN84uhjdhwlC08JV91iOuIO/pPV.ojmo90E1Juy9DxCl42WO1Aaa', 'test43', '2025-08-23 20:09:56', '2025-08-23 20:09:56', 1, NULL, 0),
(25, 'user_1755979986200_7828', 'test44@gmail.com', '$2y$10$aKxzVYXuF.bZJjMGBR7aeegf/DkiH7EW/5TKGi8qBkqB9zKRnEifa', '444', '2025-08-23 20:13:05', '2025-08-23 20:13:05', 1, NULL, 0),
(26, 'user_1755980055816_4570', 'test45@gmail.com', '$2y$10$2EPqgnDjXckWN3yfOnVH8OUODX52HCdt1yOb/MuPh0WozEfhS8Y2e', '4545', '2025-08-23 20:14:15', '2025-08-23 20:14:15', 1, NULL, 0),
(27, 'user_1755980095805_5188', 'test46@gmail.com', '$2y$10$XpAlpX3mn0qtiGat2/7zQ.mManyBlDF4eoBGm8EjLUF9O/D769yZm', 'Test46', '2025-08-23 20:14:55', '2025-08-23 20:17:35', 1, '2025-08-23 20:17:35', 1),
(28, 'user_1755980563822_8960', 'test47@gmail.com', '$2y$10$gIKIx9xI4QenQMtyCRu8HOsQk7PZnMYda3BGw./pVKPsOZt/TcUim', '4747', '2025-08-23 20:22:43', '2025-08-23 20:23:01', 1, '2025-08-23 20:23:01', 1),
(29, 'user_1755981572237_8989', 'test48@gmail.com', '$2y$10$nJTbomOhARc6r7R0dokd7uglvZPfTHcGpgMir7Pxvxk7cuBIWJmwa', 'test48', '2025-08-23 20:39:31', '2025-08-23 20:39:45', 1, '2025-08-23 20:39:45', 1),
(30, 'user_1755981894845_9996', 'test49@gmail.com', '$2y$10$GdebYQeITIe1b0fa9/g3C.yeApOjxkJyVf6AdhlOTO8i8uquLzpde', 'test', '2025-08-23 20:44:54', '2025-08-23 20:45:15', 1, '2025-08-23 20:45:15', 1),
(31, 'user_1755982024459_4231', 'test50@gmail.com', '$2y$10$V9ri9QFLDCHoPsUXK5c4VO6yFfHvpk3dl9cyLLbTHMKMyYA0CUWpy', 'Test', '2025-08-23 20:47:03', '2025-08-23 20:47:15', 1, '2025-08-23 20:47:15', 1),
(32, 'user_1755982271787_907', 'test51@gmail.com', '$2y$10$2iHjN.6lMdwaQu6G1r/sK.uBiBLqlwgD9iDf9rJOoIr74Ik.9s/vC', 'test', '2025-08-23 20:51:10', '2025-08-23 20:51:50', 1, '2025-08-23 20:51:50', 2),
(33, 'user_1755982386210_1834', 'test52@gmail.com', '$2y$10$ES1DNDHq0rBLIvsbZFYHceteObbr/bjsOi93aqX3m5Mmgzj1RwxEi', 'jddj', '2025-08-23 20:53:05', '2025-08-23 20:57:01', 1, '2025-08-23 20:57:01', 2),
(34, 'user_1755982658835_2427', 'test53@gmail.com', '$2y$10$8B.D1nvlJZ.JIsQ8MkGkNejWRSGSEGKotOMvtOp5vPJ3ITUJPOvGi', 'Test1234', '2025-08-23 20:57:37', '2025-08-23 21:01:51', 1, '2025-08-23 21:01:51', 2),
(35, 'user_1755982956140_7944', 'test54@gmail.com', '$2y$10$EklnlW9UTwtzbejK60qyp.LjWA02WoelMe4SEKXPXqMykttpusgse', 'fgnjj', '2025-08-23 21:02:35', '2025-08-23 21:53:08', 1, '2025-08-23 21:53:08', 2),
(36, 'user_1755986019032_1727', 'test55@gmail.com', '$2y$10$KEgMccmtjmVVud0B85DCFOASrIwoPJOObTSj37GW0EPqzGP./dHD.', 'Test55', '2025-08-23 21:53:38', '2025-08-24 13:10:56', 1, '2025-08-24 13:10:56', 3),
(37, 'user_1755986637066_4374', 'test56@gmail.com', '$2y$10$VNqjHdNXyzw6qftZ3dkkXu0AcHCcJCBr9KSJIvB18u0I17TM2zM7S', 'Test1234', '2025-08-23 22:03:56', '2025-08-23 22:09:43', 1, '2025-08-23 22:09:43', 3),
(38, 'user_1755987015857_3233', 'test57@gmail.com', '$2y$10$LagUtJOBOqTGIBMorriyVegizKagxyOR9WEQCEeoceyNaPA7fDZeW', 'Test1234', '2025-08-23 22:10:14', '2025-08-23 22:10:35', 1, '2025-08-23 22:10:35', 1),
(39, 'user_1755987137259_5768', 'test58@gmail.com', '$2y$10$xzOsAzp1KxdD.bjpLWKjiOI5W9xsihriY0VxLftV9zqrFN7jXCosy', 'ghh', '2025-08-23 22:12:16', '2025-08-23 22:12:35', 1, '2025-08-23 22:12:35', 1),
(40, 'user_1755987196787_8300', 'test59@gmail.com', '$2y$10$skGR0J3U4lIWEQcaE0QO9uVQV6lhrSIOoflbHUAqyyYMxKRrlY9Xq', 'Test', '2025-08-23 22:13:15', '2025-08-23 22:13:32', 1, '2025-08-23 22:13:32', 1),
(41, 'user_1755987253417_5425', 'test60@gmail.com', '$2y$10$rEVVOR5hNfPiC/rK8WLeBOwpe4MnJq0IdfebVNZpeyxx0q8Oes6tm', 'test', '2025-08-23 22:14:12', '2025-08-23 22:14:23', 1, '2025-08-23 22:14:23', 1),
(42, 'user_1756024916114_6999', 'test61@gmail.com', '$2y$10$sySnhtjPXV8j8dLc9xipgeqbjI4KdT/5vxznou0v5ItkVs5xn5hDC', 'tedt', '2025-08-24 08:41:55', '2025-08-24 08:42:12', 1, '2025-08-24 08:42:12', 1),
(43, 'user_1756024978597_1451', 'test62@gmail.com', '$2y$10$tclrA5d/je3zTunCQFhmweso3UJ3TMyumWYsUGQSAykFZo5OmCETi', 'test', '2025-08-24 08:42:58', '2025-08-24 09:48:05', 1, '2025-08-24 09:48:05', 14),
(44, 'user_1756028922650_1801', 'test63@gmail.com', '$2y$10$UNbmAvkiTbbD6c9sBFn./utsdZgrKJM5Kh/WavGtzgnS6YHZIvM9W', 'test', '2025-08-24 09:48:42', '2025-08-24 10:29:01', 1, '2025-08-24 10:29:01', 5),
(46, 'user_1756029093669_3249', 'test64@gmail.com', '$2y$10$dCzi9T5dvzC.c4EER2LJyu6uucGcdrvVW0vQA3NIwmjWdObamaDme', 'test63@gmail.com', '2025-08-24 09:51:33', '2025-08-24 13:05:15', 1, '2025-08-24 13:05:15', 6),
(55, 'user_1756031966707_6900', 'test65@gmail.com', '$2y$10$50YrfT5vvv0y4D353VJ0nuNVB7rlDUkugbCz1uAzHaAjTKxGVZOOi', 'gff', '2025-08-24 10:39:26', '2025-08-24 13:02:40', 1, '2025-08-24 13:02:40', 6),
(59, 'user_1756040752582_4883', 'test66@gmail.com', '$2y$10$dLlPvEQ4AXRe.R/ZXEOvpeKB93o60UdDXvWwvChFjLswITwuDc2/i', 'shsh', '2025-08-24 13:05:51', '2025-08-24 13:24:17', 1, '2025-08-24 13:24:17', 7),
(60, 'user_1756040950476_3860', 'test67@gmail.com', '$2y$10$MkJBMSwI80XaEaswnl7yYepcSn6IC3Y68MryXU7PZTXZcdLZlrrZ2', 'bdbbd', '2025-08-24 13:09:08', '2025-08-24 13:14:15', 1, '2025-08-24 13:14:15', 3),
(62, 'user_1756041954475_3477', 'test68@gmail.com', '$2y$10$tL/OtB6LgWM8pscYB63P/.ClWRz250SwzEKvEWfJY..aVI8XqZwLG', 'test', '2025-08-24 13:25:52', '2025-08-24 13:36:40', 1, '2025-08-24 13:36:40', 4),
(63, 'user_1756042790814_8368', 'test69@gmail.com', '$2y$10$xwte/ZnTzwIsX2MJnptKHOK2QhKnVVtVxEZ1ynDp7V.oknLf2iUwy', 'test69', '2025-08-24 13:39:49', '2025-08-24 14:21:25', 1, '2025-08-24 14:21:25', 5),
(64, 'user_1756045218018_5790', 'test70@gmail.com', '$2y$10$QTEuMg2uiJSHqk/SIOlmF.vnw6BppMGikzvBqNkGuNbwBJlvgxxim', 'Test', '2025-08-24 14:20:16', '2025-08-29 15:42:51', 1, '2025-08-29 15:42:51', 5),
(65, 'user_1756045448877_8399', 'test71@gmail.com', '$2y$10$6.rpbQcY2c0h01PEzf.OQuBXhyd6GBcxlzNZNbkxMD4VtiXhssFOS', 'Test1234', '2025-08-24 14:24:07', '2025-08-24 14:24:17', 1, '2025-08-24 14:24:17', 1),
(66, 'user_1756482488536_2871', 'test72@gmail.com', '$2y$10$LcykSgSw1nNSQjTJG30zyevxULPAR4hVPvMPMcaa3lO4HwXugf0A6', 'Test72', '2025-08-29 15:48:06', '2025-08-29 15:48:20', 1, '2025-08-29 15:48:20', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `cards`
--
ALTER TABLE `cards`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `car_id` (`car_id`),
  ADD KEY `idx_user_id` (`user_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cat_id` (`cat_id`);

--
-- Indexes for table `documents`
--
ALTER TABLE `documents`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `document_id` (`document_id`),
  ADD KEY `idx_user_id` (`user_id`);

--
-- Indexes for table `passwords`
--
ALTER TABLE `passwords`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `pass_id` (`pass_id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_cat_id` (`cat_id`);

--
-- Indexes for table `pins`
--
ALTER TABLE `pins`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD KEY `idx_pins_user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_users_email` (`email`),
  ADD KEY `idx_users_user_id` (`user_id`),
  ADD KEY `idx_users_status` (`is_active`),
  ADD KEY `idx_users_last_login` (`last_login`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `cards`
--
ALTER TABLE `cards`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `documents`
--
ALTER TABLE `documents`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `passwords`
--
ALTER TABLE `passwords`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `pins`
--
ALTER TABLE `pins`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=64;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=67;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `pins`
--
ALTER TABLE `pins`
  ADD CONSTRAINT `pins_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
