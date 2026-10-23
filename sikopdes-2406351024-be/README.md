# SIKopDes - Modul KD1: Keanggotaan dan Simpanan (Back-End)

Repositori ini merupakan implementasi back-end untuk **SIKopDes (Sistem Informasi Koperasi Desa Merah Biru)** - Modul **KD1: Keanggotaan dan Simpanan**, yang dikembangkan menggunakan **Spring Boot 4 / Java 25** dan basis data **PostgreSQL**.

- **Nama**: Raihan Maulana Heriandry
- **NPM**: 2406351024
- **Mata Kuliah**: Arsitektur Perangkat Lunak Terintegrasi (APAP) - Gasal 2026/2027

---

## 🏗️ 1. Arsitektur & Penerapan Konsep Layering

Aplikasi dirancang dengan pendekatan arsitektur berorientasi lapisan (*Layered Architecture*) yang memisahkan tanggung jawab secara tegas (*Separation of Concerns*) dan menerapkan DI.

```mermaid
flowchart TD
    Client["Front-End / HTTP Client (Vue.js / Browser)"]
    
    subgraph SpringBootApp ["Spring Boot Back-End (Port 8080)"]
        Controller["Controller Layer (REST Endpoints)\n- AnggotaController\n- SimpananController\n- KepatuhanController\n- RiwayatStatusController\n- DashboardController\n- StatistikController"]
        
        Service["Service Layer (Business Logic & Transactions)\n- AnggotaService / Impl\n- SimpananService / Impl\n- KepatuhanService / Impl\n- RiwayatStatusService / Impl\n- DashboardService / Impl\n- StatistikService / Impl"]
        
        Scheduler["Scheduler (Scheduled Background Tasks)\n- ComplianceJob (@Scheduled Asia/Jakarta)"]
        
        Repository["Repository Layer (Spring Data JPA)\n- AnggotaRepository\n- SimpananRepository\n- RiwayatStatusAnggotaRepository\n- PemeriksaanKepatuhanRepository"]
        
        Model["Domain Model & Entities\n- Anggota\n- Simpanan\n- RiwayatStatusAnggota\n- PemeriksaanKepatuhan"]
        
        DTO["DTO & Exception Handling\n- Request & Response DTOs\n- GlobalExceptionHandler\n- BaseResponse<T>"]
    end
    
    Database[("PostgreSQL Database (Docker)\nDatabase: sikopdes (Port 5432)")]

    Client -->|HTTP REST JSON| Controller
    Controller -->|DTOs / Invocations| Service
    Scheduler -->|Triggers Evaluation| Service
    Service -->|Entities & Queries| Repository
    Repository -->|Hibernate / JDBC| Database
    Service -->|Populates| DTO
    Controller -->|BaseResponse<T>| Client
```

### Penjelasan Lapisan Arsitektur:
1. **Controller Layer**: Bertanggung jawab menerima request HTTP, melakukan validasi payload DTO awal, memanggil service terkait, dan membungkus hasil dalam format standar `BaseResponse<T>`.
2. **Service Layer**: Mengenkapsulasi seluruh aturan bisnis, validasi logika domain, state machine status keanggotaan, pengecekan kepatuhan kalender, dan transaksi atomik database menggunakan `@Transactional`.
3. **Repository Layer**: Abstraksi akses data menggunakan Spring Data JPA dengan query kustom JPQL/Criteria untuk pencarian dinamis, pagination, dan aggregasi numerik/statistik.
4. **Model/Entity Layer**: Pemetaan objek relasional database menggunakan JPA/Hibernate (`Anggota`, `Simpanan`, `RiwayatStatusAnggota`, `PemeriksaanKepatuhan`) beserta constraint keunikan dan integritas referensial.
5. **Scheduler Component**: Eksekusi periodik kepatuhan simpanan wajib otomatis berbasis cron `@Scheduled(zone = "Asia/Jakarta")` yang bersifat idempoten.

---

## 📂 2. Struktur Repositori

```
sikopdes-2406351024-be/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/apap/ti/_6/sikopdes_2406351024_be/
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java               # Konfigurasi CORS & integrasi ke Front-End
│   │   │   ├── controller/
│   │   │   │   ├── AnggotaController.java        # REST Controller manajemen anggota
│   │   │   │   ├── DashboardController.java      # REST Controller metrik beranda
│   │   │   │   ├── KepatuhanController.java      # REST Controller kepatuhan simpanan wajib
│   │   │   │   ├── RiwayatStatusController.java  # REST Controller audit trail riwayat status
│   │   │   │   ├── SimpananController.java       # REST Controller transaksi simpanan
│   │   │   │   └── StatistikController.java      # REST Controller agregasi statistika
│   │   │   ├── dto/
│   │   │   │   ├── request/                      # DTO payload permintaan (Request Body)
│   │   │   │   └── response/                     # DTO respons & BaseResponse<T>
│   │   │   ├── exception/
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── GlobalExceptionHandler.java   # Penanganan error global terpusat
│   │   │   │   ├── InvalidActionException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── model/                            # Entitas domain JPA / Hibernate
│   │   │   │   ├── Anggota.java
│   │   │   │   ├── PemeriksaanKepatuhan.java
│   │   │   │   ├── RiwayatStatusAnggota.java
│   │   │   │   └── Simpanan.java
│   │   │   ├── repository/                       # Interface Spring Data JPA Repository
│   │   │   │   ├── AnggotaRepository.java
│   │   │   │   ├── PemeriksaanKepatuhanRepository.java
│   │   │   │   ├── RiwayatStatusAnggotaRepository.java
│   │   │   │   └── SimpananRepository.java
│   │   │   ├── scheduler/
│   │   │   │   └── ComplianceJob.java            # Job otomatis evaluasi kepatuhan (@Scheduled)
│   │   │   ├── seeder/
│   │   │   │   └── DataSeeder.java               # Inisialisasi data realistis (DataFaker)
│   │   │   ├── service/                          # Interface Service Layer
│   │   │   │   ├── impl/                         # Implementasi logika bisnis & transaksi
│   │   │   │   ├── AnggotaService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── KepatuhanService.java
│   │   │   │   ├── RiwayatStatusService.java
│   │   │   │   ├── SimpananService.java
│   │   │   │   └── StatistikService.java
│   │   │   └── Sikopdes2406351024BeApplication.java # Entry point aplikasi Spring Boot
│   │   └── resources/
│   │       ├── application.yaml                  # Konfigurasi aplikasi, env vars, dan datasource
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/apap/ti/_6/sikopdes_2406351024_be/
│           └── Sikopdes2406351024BeApplicationTests.java
├── .env                                          # Environment variables lokal (DB_URL, dll.)
├── .gitattributes
├── .gitignore                                    # Mengabaikan build/, .gradle, .env, dsb.
├── build.gradle                                  # Definisi dependensi & task build Gradle
├── gradlew                                       # Script wrapper Gradle untuk Linux/macOS
├── gradlew.bat                                   # Script wrapper Gradle untuk Windows
└── settings.gradle
```

---

## 🗄️ 3. Diagram Relasi Entitas (ERD)

```mermaid
erDiagram
    ANGGOTA ||--o{ SIMPANAN : "memiliki"
    ANGGOTA ||--o{ RIWAYAT_STATUS_ANGGOTA : "memiliki riwayat"
    PEMERIKSAAN_KEPATUHAN ||--o{ RIWAYAT_STATUS_ANGGOTA : "menghasilkan"

    ANGGOTA {
        string id PK "Format: ANGxxxx"
        string nomorAnggota UK "Format: KOP-YYYY-xxxx"
        string nama
        string nik "16 digit angka, unik selain Ditolak"
        string email "Format email, unik selain Ditolak"
        string telepon "10-15 digit angka"
        string alamat "Text"
        datetime tanggalDaftar
        datetime tanggalAktivasi "Diisi saat pertama kali Aktif"
        string statusKeanggotaan "MENUNGGU_VERIFIKASI, MENUNGGU_SETORAN_POKOK, AKTIF, DIBEKUKAN, NONAKTIF, DITOLAK, KELUAR"
        datetime createdAt
        datetime updatedAt
    }

    SIMPANAN {
        string id PK "Format: SMPxxxx"
        string anggotaId FK
        string jenisSimpanan "POKOK, WAJIB, SUKARELA"
        long nominal ">=100k pokok, >=20k wajib, >0 sukarela"
        date tanggalSetor "Tidak boleh di masa depan"
        string periode "YYYY-MM (hanya untuk WAJIB)"
        datetime createdAt
        datetime updatedAt
    }

    RIWAYAT_STATUS_ANGGOTA {
        string id PK "Format: RSTxxxx"
        string anggotaId FK
        string pemeriksaanId FK "Null jika oleh pengurus"
        string statusSebelumnya
        string statusBaru
        datetime tanggalPerubahan
        string alasan
        string diubahOleh "PENGURUS / SISTEM"
        datetime createdAt
        datetime updatedAt
    }

    PEMERIKSAAN_KEPATUHAN {
        string id PK "Format: PKPxxxx"
        datetime waktuEksekusi
        string periodeAwal "YYYY-MM"
        string periodeAkhir "YYYY-MM"
        int jumlahDiperiksa
        int jumlahDinonaktifkan
        datetime createdAt
        datetime updatedAt
    }
```

---

## ⚙️ 4. Prasyarat & Instalasi

### Prasyarat Sistem:
- **Java**: OpenJDK / Oracle JDK versi 21 atau 25
- **Docker & Docker Compose**: Untuk menjalankan basis data PostgreSQL
- **Gradle**: Tersedia via wrapper `./gradlew`

### Langkah Menjalankan:
1. Pastikan container basis data PostgreSQL berjalan:
   ```bash
   # Di direktori root proyek
   docker compose up -d
   ```
2. Pastikan file `.env` tersedia pada direktori `sikopdes-2406351024-be/.env`:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/sikopdes
   DB_USERNAME=sikopdes
   DB_PASSWORD=sikopdes
   ```
3. Jalankan aplikasi Spring Boot:
   ```bash
   cd sikopdes-2406351024-be
   ./gradlew bootRun
   ```
4. Aplikasi akan menyala pada `http://localhost:8080`. Seeder otomatis (`DataSeeder`) menggunakan DataFaker akan mengisi data awal mencakup seluruh status keanggotaan, histori, setoran, dan data demo kepatuhan saat database masih kosong.

---

## 📡 5. Dokumentasi API (Endpoints)

Seluruh response API menggunakan struktur dasar standar berikut:
```json
{
  "status": 200,
  "message": "Success",
  "timestamp": "2026-10-23T15:30:00.000+07:00",
  "data": ...
}
```

### Ringkasan Daftar Endpoint

| Modul | Method | URL Path | Deskripsi |
| :--- | :--- | :--- | :--- |
| **Dashboard** | `GET` | `/api/dashboard/stats` | Statistik ringkasan kartu status anggota & total setoran bulan berjalan |
| **Anggota** | `GET` | `/api/anggota` | Daftar semua anggota (query: `search`, `status`) |
| **Anggota** | `GET` | `/api/anggota/{id}` | Detail anggota lengkap dengan saldo simpanan & riwayat |
| **Anggota** | `POST` | `/api/anggota` | Tambah anggota baru (status awal `MENUNGGU_VERIFIKASI`) |
| **Anggota** | `PUT` | `/api/anggota/{id}` | Ubah profil anggota (nama, email unik, no. telp, alamat) |
| **Anggota** | `POST` | `/api/anggota/{id}/verifikasi` | Verifikasi (`VERIFIKASI`) atau Tolak (`TOLAK` dengan alasan wajib) |
| **Anggota** | `PUT` | `/api/anggota/{id}/status` | Bekukan (`DIBEKUKAN`) atau Aktifkan kembali (`AKTIF` dengan validasi tunggakan) |
| **Anggota** | `DELETE` | `/api/anggota/{id}/keluar` | Soft delete bisnis anggota (status `KELUAR`, alasan wajib) |
| **Simpanan** | `GET` | `/api/simpanan` | Daftar transaksi simpanan (filter: `search`, `jenis`, `periode`, `nominalMin`, `nominalMax`) |
| **Simpanan** | `GET` | `/api/simpanan/{id}` | Detail satu transaksi simpanan (read-only) |
| **Simpanan** | `POST` | `/api/simpanan/pokok` | Pencatatan simpanan pokok (aktivasi anggota & catat riwayat atomik) |
| **Simpanan** | `POST` | `/api/simpanan` | Pencatatan simpanan Wajib / Sukarela |
| **Kepatuhan** | `GET` | `/api/kepatuhan` | Rekap kepatuhan 3 periode kalender selesai & riwayat 5 eksekusi terakhir |
| **Kepatuhan** | `POST` | `/api/kepatuhan/trigger` | Trigger manual job pemeriksaan kepatuhan |
| **Riwayat** | `GET` | `/api/riwayat-status` | Audit trail perubahan status (filter: `search`, `statusBaru`, `startDate`, `endDate`) |
| **Riwayat** | `GET` | `/api/riwayat-status/{id}` | Detail audit trail perubahan status |
| **Statistik** | `GET` | `/api/statistik` | Data agregasi visualisasi (anggota baru, status, jenis simpanan, tren bulanan) |

---

## 🧪 6. Menjalankan Pengujian & Build

```bash
# Menjalankan unit & integration tests
./gradlew test

# Menghasilkan executable bootJar SIKD12406351024.jar
./gradlew bootJar
```
Berkas `.jar` yang dapat langsung dijalankan akan dihasilkan di `build/libs/SIKD12406351024.jar`.
