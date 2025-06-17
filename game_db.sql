-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Waktu pembuatan: 17 Jun 2025 pada 11.15
-- Versi server: 8.1.0
-- Versi PHP: 8.4.7

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `game_db`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `players`
--

CREATE TABLE `players` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) DEFAULT '',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `skill` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `powerup`
--

CREATE TABLE `powerup` (
  `id` int NOT NULL,
  `namapowerup` varchar(50) DEFAULT NULL,
  `keterangan` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `powerup`
--

INSERT INTO `powerup` (`id`, `namapowerup`, `keterangan`) VALUES
(1, 'Heart', 'Menambah nyawa'),
(2, 'Shield', 'Kebal sementara dari peluru'),
(3, 'Speed', 'Gerak cepat sementara'),
(4, 'Swap', 'Tukar nyawa lawan dan tambah nyawa sendiri'),
(5, 'Lock', 'Mengunci skill lawan sementara');

-- --------------------------------------------------------

--
-- Struktur dari tabel `powerups_log`
--

CREATE TABLE `powerups_log` (
  `id` int NOT NULL,
  `player_id` int DEFAULT NULL,
  `powerup_type` varchar(50) DEFAULT NULL,
  `collected_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `powerup_id` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `scores`
--

CREATE TABLE `scores` (
  `id` int NOT NULL,
  `player_id` int DEFAULT NULL,
  `score` int NOT NULL,
  `match_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `skills`
--

CREATE TABLE `skills` (
  `id` int NOT NULL,
  `nama_skill` varchar(50) DEFAULT NULL,
  `penjelasan` text,
  `cooldown` int DEFAULT NULL,
  `sounds` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data untuk tabel `skills`
--

INSERT INTO `skills` (`id`, `nama_skill`, `penjelasan`, `cooldown`, `sounds`) VALUES
(1, 'The World', 'Menghentikan waktu sementara', 15000, 'skill1.wav'),
(2, 'Star Platinum', 'Menghapus peluru di sekitar karakter', 20000, 'skill2.wav'),
(3, 'Made in Heaven', 'Menghentikan waktu 10 detik, skor lawan perlahan turun', 30000, 'skill3.wav'),
(4, 'Crazy Diamond', 'Menambah 1 health ke player', 35000, 'skill4.wav');

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `players`
--
ALTER TABLE `players`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `fk_skill` (`skill`);

--
-- Indeks untuk tabel `powerup`
--
ALTER TABLE `powerup`
  ADD PRIMARY KEY (`id`);

--
-- Indeks untuk tabel `powerups_log`
--
ALTER TABLE `powerups_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `player_id` (`player_id`),
  ADD KEY `fk_powerup` (`powerup_id`);

--
-- Indeks untuk tabel `scores`
--
ALTER TABLE `scores`
  ADD PRIMARY KEY (`id`),
  ADD KEY `player_id` (`player_id`);

--
-- Indeks untuk tabel `skills`
--
ALTER TABLE `skills`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `players`
--
ALTER TABLE `players`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT untuk tabel `powerup`
--
ALTER TABLE `powerup`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT untuk tabel `powerups_log`
--
ALTER TABLE `powerups_log`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `scores`
--
ALTER TABLE `scores`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `skills`
--
ALTER TABLE `skills`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Ketidakleluasaan untuk tabel pelimpahan (Dumped Tables)
--

--
-- Ketidakleluasaan untuk tabel `players`
--
ALTER TABLE `players`
  ADD CONSTRAINT `fk_skill` FOREIGN KEY (`skill`) REFERENCES `skills` (`id`);

--
-- Ketidakleluasaan untuk tabel `powerups_log`
--
ALTER TABLE `powerups_log`
  ADD CONSTRAINT `fk_powerup` FOREIGN KEY (`powerup_id`) REFERENCES `powerup` (`id`),
  ADD CONSTRAINT `powerups_log_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;

--
-- Ketidakleluasaan untuk tabel `scores`
--
ALTER TABLE `scores`
  ADD CONSTRAINT `scores_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `players` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
