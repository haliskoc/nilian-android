# 🌿 Nilian — Personal Life Operating System
> **Mimari, Tasarım ve Geliştirme Master Planı**  
> *Hedef Platform: Android (Telefon & Tablet) | Dil: Kotlin + Jetpack Compose | Dağıtım: GitHub Actions CI/CD*

---

## 📑 İçindekiler
1. [Ürün Vizyonu ve Tasarım Felsefesi](#1-ürün-vizyonu-ve-tasarım-felsefesi)
2. [Teknik Mimari ve Teknoloji Yığını](#2-teknik-mimari-ve-teknoloji-yığını)
3. [Veri Modelleri ve Veritabanı Şeması (Room DB)](#3-veri-modelleri-ve-veritabanı-şeması-room-db)
4. [Deterministik Mantık Motoru (Yapay Zekâsız Akıllı Özellikler)](#4-deterministik-mantık-motoru-yapay-zekâsız-akıllı-özellikler)
5. [Ekran ve Kullanıcı Deneyimi (UI/UX) Mimarisi](#5-ekran-ve-kullanıcı-deneyimi-uiux-mimarisi)
6. [Telefon ve Tablet Uyarlanabilir (Adaptive) Düzenleri](#6-telefon-ve-tablet-uyarlanabilir-adaptive-düzenleri)
7. [GitHub Actions CI/CD ve APK Derleme İş Akışı](#7-github-actions-cicd-ve-apk-derleme-iş-akışı)
8. [Modüler Kod Tabanı Dizin Yapısı](#8-modüler-kod-tabanı-dizin-yapısı)
9. [Adım Adım Geliştirme Yol Haritası (Roadmap)](#9-adım-adım-geliştirme-yol-haritası-roadmap)

---

## 1. Ürün Vizyonu ve Tasarım Felsefesi

### 1.1 Temel Amaç
**Nilian**, öğrenciler ve girişimciler için tasarlanmış; günlük işleri, dersleri, toplantıları, alışkanlıkları, hedefleri ve dinlenme zamanlarını tek bir çatı altında toplayan **baskısız, sakin ve premium bir kişisel yaşam işletim sistemidir**.

### 1.2 Tasarım İlkeleri
- **Sakinlik (Calm Tech):** Kullanıcıyı sürekli bildirimlerle boğmayan, "daha fazlasını yapmalısın" stresi yerine "hayatını dingin ve kontrollü yönet" hissi veren mikro-etkileşimler.
- **Minimal & Premium Estetik:**
  - **Koyu Tema (Dark):** Derin Kömür/Gece Mavisi (`#121417`), Yumuşak Kart Zeminleri (`#1B1F24`), İnce Kenarlıklar (`#2A3038`).
  - **Açık Tema (Light):** Kırık Beyaz/Kağıt Tonu (`#F8F9FA`), Yumuşak Krem/Gri Kartlar (`#FFFFFF`), Düşük Kontrastlı Çizgiler (`#E9ECEF`).
  - **Vurgu Rengi (Accent):** Sakin Adaçayı Yeşili (`#4E876A`) veya Muted Amber (`#D99B43`).
- **Gizlilik ve Sahiplik:** %100 çevrimdışı (offline-first). Kullanıcı kaydı veya harici sunucu bağımlılığı yok. Uygulama sadece sahibinin bildiği bir Ana PIN/Şifre ile açılır.

---

## 2. Teknik Mimari ve Teknoloji Yığını

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Jetpack Compose)           │
│  - Material 3 Design System                            │
│  - Adaptive Navigation (BottomBar / NavigationRail)    │
│  - Screens: Today, Timeline, Tasks, Habits, Goals     │
└───────────────────────────▲────────────────────────────┘
                            │ (StateFlow / UIState)
┌───────────────────────────┴────────────────────────────┐
│              Presentation Layer (MVVM / MVI)           │
│  - ViewModels with Unidirectional Data Flow            │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                    Domain Layer                        │
│  - UseCases (CalculateStreaks, DetectCollisions, etc.) │
│  - Pure Kotlin Business Logic                          │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                     Data Layer                         │
│  - Repositories (TaskRepository, HabitRepository, etc.)│
│  - Room Database (Local SQLite)                        │
│  - DataStore (App PIN, Theme, User Preferences)        │
└────────────────────────────────────────────────────────┘
```

| Katman | Seçilen Teknoloji / Kütüphane | Gerekçe |
| :--- | :--- | :--- |
| **Dil** | Kotlin 2.0+ | Modern, tür güvenli, Android için birinci sınıf destek. |
| **UI** | Jetpack Compose + Material 3 | Bildirimsel arayüz, telefon/tablet adaptasyonu kolaylığı. |
| **Navigasyon** | Jetpack Navigation Compose | Tip güvenli ekran geçişleri. |
| **Veritabanı** | Room Database (SQLite) | Güçlü yerel veri yönetimi, Coroutines/Flow desteği. |
| **Ayarlar & Kilit**| Jetpack DataStore Preferences | Şifre hash'i, tema tercihleri için hafif ve güvenli depolama. |
| **Bağımlılık Yönetimi**| Kotlin DSL + Version Catalog (`libs.versions.toml`)| Temiz ve merkezi bağımlılık yönetimi. |
| **CI/CD** | GitHub Actions Workflow | GitHub üzerinde sıfır masrafla otomatik APK derleme & artifact çıkarma. |

---

## 3. Veri Modelleri ve Veritabanı Şeması (Room DB)

### 3.1 Varlıklar (Entities)

#### 1. Görevler (`TaskEntity`)
Yapılacak işler, tek seferlik aksiyonlar.
```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM, // LOW, MEDIUM, HIGH
    val estimatedDurationMinutes: Int = 30,  // Tahmini süre (dk)
    val dueDate: LocalDate? = null,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val goalId: Long? = null,                 // İlişkili hedef (opsiyonel)
    val autoRollover: Boolean = true          // Tamamlanmazsa yarına devret
)
```

#### 2. Etkinlikler (`EventEntity`)
Belirli saat aralığında gerçekleşen dersler, toplantılar veya randevular.
```kotlin
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val locationOrLink: String? = null,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val category: EventCategory = EventCategory.GENERAL, // LECTURE, MEETING, PERSONAL
    val colorHex: String? = null
)
```

#### 3. Alışkanlıklar (`HabitEntity` ve `HabitLogEntity`)
Düzenli tekrarlanan davranışlar ve bunların günlük tamamlama kayıtları.
```kotlin
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val goalId: Long? = null,
    val targetDaysOfWeek: Set<DayOfWeek>, // Örn: Pazartesi-Cuma
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: LocalDate = LocalDate.now()
)

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "date"]
)
data class HabitLogEntity(
    val habitId: Long,
    val date: LocalDate,
    val isCompleted: Boolean = true
)
```

#### 4. Zaman Blokları (`TimeBlockEntity`)
Günün 24 saatlik takviminde planlanan bloklar (Ders, Derin Odak, Spor, Uyku, Dinlenme).
```kotlin
@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val blockType: BlockType, // SLEEP, WORKOUT, STUDY, DEEP_WORK, REST, BUFFER
    val startTime: LocalTime,
    val endTime: LocalTime,
    val date: LocalDate,
    val linkedTaskId: Long? = null
)
```

#### 5. Hedefler (`GoalEntity`)
Uzun vadeli vizyon ve kilometre taşları.
```kotlin
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val targetDate: LocalDate? = null,
    val progressPercent: Float = 0f,
    val isArchived: Boolean = false
)
```

---

## 4. Deterministik Mantık Motoru (Yapay Zekâsız Akıllı Özellikler)

Yapay zekâ API'lerine veya internete ihtiyaç duymadan, uygulama içinde yerel matematiksel kurallarla çalışan sistemler:

1. **Çakışma Analizörü (Collision Detection):**
   - Takvimde üst üste binen etkinlik veya zaman bloklarını hesaplar, görsel olarak kart kenarında turuncu uyarı çizgisi ile gösterir.
2. **Boşluk / Odak Aralığı Bulucu (Free Slot Finder):**
   - Günlük 24 saatlik cetvelde 45 dakika ve üzeri boşlukları "Serbest Odak Zamanı" olarak gri kesikli bloklarla listeler.
3. **Otomatik Görev Devretme (Task Rollover):**
   - Gece 00:00 geçildiğinde veya günün ilk açılışında dün tamamlanmamış ve `autoRollover = true` olan görevleri otomatik olarak bugünün listesine taşır.
4. **Seri / Streak Algoritması:**
   - Seçilen hedef günlere göre son tamamlama geçmişini geriye doğru tarayarak gerçek zamanlı aktif seri ve en uzun seriyi hesaplar.
5. **Günün Yükü İndeksi (Workload Stress-Score):**
   - Toplam planlanan süre (Görevler + Etkinlikler) 10 saati geçtiğinde "Bugün çok yoğun, birkaç görevi ertelemek isteyebilirsin" şeklinde sakin bir bildirim rozeti sunar.

---

## 5. Ekran ve Kullanıcı Deneyimi (UI/UX) Mimarisi

```
                    [ 1. UYGULAMA KİLİT EKRANI ]
                                 │ (PIN Doğrulama)
                                 ▼
                     [ 2. BUGÜN (ANA EKRAN) ]
             ┌───────────────────┼───────────────────┐
             ▼                   ▼                   ▼
     [ 3. ZAMAN ÇİZELGESİ ] [ 4. ALIŞKANLIKLAR ] [ 5. HEDEFLER ]
     (Günlük / Haftalık)      (Seri Takibi)       (Proje Ağacı)
```

### 5.1 Ekran Detayları

- **Ekran 1: Uygulama Kilidi (App Lock)**
  - Minimal numpad veya şifre giriş alanı.
  - Sade karşılama: *"Hoş geldin. Zihnini toparlamaya hazır mısın?"*
  - Güvenli yerel doğrulama.

- **Ekran 2: Bugün Paneli (Dashboard - Varsayılan)**
  - **Tarih & Durum:** Bugünün tarihi, tamamlanma yüzdesi çemberi (Circular Progress).
  - **Sıradaki Etkinlik / Blok:** "Şu an: Deep Work (Kalan: 25 dk)" veya "Sıradaki: 14:00 Toplantı".
  - **Hızlı Görev Listesi:** Bugünün odak görevleri ve tek tıkla tamamlama.
  - **Günlük Alışkanlık Hapları:** Yatay kaydırılabilir, tek dokunuşla işaretlenen alışkanlık kartları.
  - **Görünüm Değiştirici:** Tek tuşla **Günlük Görünüm** ⇄ **Haftalık Görünüm** geçişi.

- **Ekran 3: Zaman Çizelgesi & Takvim (Timeline View)**
  - Dikey 24 saatlik akıcı zaman ekseni.
  - Zaman blokları (Uyku koyu mavi, Spor yeşil, Çalışma mor tonlarında).
  - Görevleri doğrudan takvime saat aralığı olarak atayabilme.

- **Ekran 4: Alışkanlıklar & Seri Takibi (Habits Hub)**
  - Her alışkanlık için alev/seri sayacı (Örn: 🔥 14 Gün).
  - Haftalık 7 günlük nokta matrisi (Tamamlananlar dolu, kaçırılanlar soluk).

- **Ekran 5: Hedefler (Goals & Vision)**
  - Büyük resim: Hedef kartları, altında ilişkili görevler ve alışkanlıkların ilerleme durumu.

- **Ekran 6: Ayarlar & Yerel Yedekleme**
  - Koyu/Açık tema seçimi.
  - PIN değiştirme.
  - **"Tek Tıkla JSON Yedekle / Geri Yükle"** (Verileri dosyaya aktarıp dilediğinde geri yükleme).

---

## 6. Telefon ve Tablet Uyarlanabilir (Adaptive) Düzenleri

Jetpack Compose `WindowWidthSizeClass` kullanılarak arayüz iki forma otomatik adapte edilir:

```
[ AKILLI TELEFON (Compact) ]             [ TABLET (Expanded / Dual-Pane) ]
┌──────────────────────────┐          ┌────────┬────────────────────────────┬──────────────┐
│ [Bugünün Özeti Kartı]    │          │        │ [Zaman Çizelgesi / 24h]    │ [Seçili Görev│
│                          │          │        │                            │  ve Hedef    │
│ [Sıradaki Blok]          │          │ Nav    │ 08:00 Uyku                 │  Detayları]  │
│                          │          │ Rail   │ 09:00 Kahvaltı & Planlama  │              │
│ [Görevler Listesi]       │          │ (Sol)  │ 10:00 Girişimcilik Odak    │ İlerleme:    │
│                          │          │        │ 12:00 Spor / Yürüyüş       │ %75          │
├──────────────────────────┤          │        │ 14:00 Ders / Toplantı      │              │
│ [Bugün] [Takvim] [Hedef] │          └────────┴────────────────────────────┴──────────────┘
└──────────────────────────┘
```

- **Telefon (Compact):** Alt gezinme çubuğu (`NavigationBar`), tek sütunlu kart akışı.
- **Tablet (Expanded):** Sol tarafta sabit `NavigationRail` veya kalıcı çekmece menü, merkezde genişletilmiş zaman çizelgesi, sağ bölmede görev detayları ve hedef paneli (Split-View).

---

## 7. GitHub Actions CI/CD ve APK Derleme İş Akışı

Play Store hesabı açmadan, her `main` dala kod gönderildiğinde (push) GitHub sunucularında APK otomatik derlenir ve indirilebilir dosya (**Artifact**) olarak sunulur.

### `.github/workflows/build-apk.yml`

```yaml
name: Build Nilian APK

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]
  workflow_dispatch: # GitHub arayüzünden manuel tetikleme butonu

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant Execute Permission for Gradlew
        run: chmod +x gradlew

      - name: Build Debug & Release APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload Nilian APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: Nilian-Debug-APK
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 14
```

> **Nasıl Kullanılır?**
> 1. Kod GitHub'a yüklendiğinde GitHub Actions otomatik çalışır.
> 2. `Actions` sekmesinden son derlemeye tıklanır.
> 3. `Artifacts` bölümünden `Nilian-Debug-APK` tek tıkla telefona/tablete indirilip doğrudan kurulur.

---

## 8. Modüler Kod Tabanı Dizin Yapısı

```
nilian-android/
├── .github/
│   └── workflows/
│       └── build-apk.yml               # GitHub Actions APK derleme iş akışı
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/com/nilian/app/
│               ├── NilianApp.kt        # Application sınıfı
│               ├── MainActivity.kt     # Tek Activity & NavHost
│               ├── core/
│               │   ├── database/       # Room DB, TypeConverters
│               │   ├── datastore/      # Preferences & PIN depolama
│               │   └── ui/             # Tema, Tipografi, Renkler, Temel Bileşenler
│               ├── data/
│               │   ├── local/
│               │   │   ├── dao/        # TaskDao, EventDao, HabitDao, GoalDao
│               │   │   └── entity/     # Veritabanı Entity sınıfları
│               │   └── repository/     # Repository implementasyonları
│               ├── domain/
│               │   ├── model/          # Saf veri modelleri
│               │   ├── repository/     # Repository arayüzleri (Interface)
│               │   └── usecase/        # Çakışma, seri hesabı vb. iş mantıkları
│               └── presentation/
│                   ├── navigation/     # NavHost ve ekran rotaları
│                   ├── lock/           # PIN kilit ekranı
│                   ├── today/          # Ana gösterge paneli (Dashboard)
│                   ├── timeline/       # 24h Çizelge ve Takvim
│                   ├── tasks/          # Görev yönetimi
│                   ├── habits/         # Alışkanlıklar & Seri takibi
│                   ├── goals/          # Hedefler
│                   └── settings/       # Tema & Yedekleme
├── gradle/
│   └── libs.versions.toml              # Versiyon kataloğu
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 9. Adım Adım Geliştirme Yol Haritası (Roadmap)

```
[ Faz 1: Altyapı ]  ──►  [ Faz 2: Çekirdek UI ]  ──►  [ Faz 3: Mantık Motoru ]  ──►  [ Faz 4: CI/CD & Test ]
- Room DB Şemaları       - Tema & Bileşenler          - Seri & Devretme Mantığı      - GitHub Actions Pipeline
- DataStore & PIN        - Bugün & Zaman Çizelgesi     - Çakışma Analizi              - APK İndirme & Test
- Repository Katmanı     - Tablet NavRail             - JSON Yedekleme Sistemi       - Kişisel Kullanım
```

1. **Faz 1: Veri ve Güvenlik Temeli**
   - `Room Database` varlıklarının (`Task`, `Event`, `Habit`, `Goal`, `TimeBlock`) ve DAO'larının yazılması.
   - PIN şifreleme ve `DataStore` entegrasyonu.
2. **Faz 2: Kullanıcı Arayüzü & Navigasyon**
   - Sakin ve minimal Material 3 temasının kurulması.
   - Kilit ekranı, Bugün paneli ve Günlük/Haftalık zaman çizelgesi ekranlarının Jetpack Compose ile kodlanması.
   - Telefon ve tablet için uyarlanabilir navigasyon düzeni.
3. **Faz 3: Akıllı Mantık & Etkileşimler**
   - Çakışma algılama algoritması ve tamamlanmayan görevlerin sonraki güne devredilmesi.
   - Alışkanlık serisi (streak) hesaplama motoru.
   - Çevrimdışı JSON dışa/içe aktarma motoru.
4. **Faz 4: GitHub Actions & Canlıya Alma**
   - `.github/workflows/build-apk.yml` kurulumu.
   - APK'nın oluşturulup cihaza yüklenerek gerçek kullanım testlerinin yapılması.
