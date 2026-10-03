# APK PROJECT CHECKPOINT (Skin Engine)
Status: Stage 05 — kode Skin Engine ditulis dan diintegrasikan; BELUM dikompilasi (sandbox tanpa Android SDK/kotlinc/jaringan).
Detail arsitektur: docs/skin-engine.md. Root CHECKPOINT.md = checkpoint proyek keseluruhan.
Default skin (fallback offline): app/src/main/assets/skins/default/{skin.svg,skin.json} = salinan skins/skin-01 v1.1.
Dependency baru: com.caverock:androidsvg-aar:1.4 (app/build.gradle.kts).
Cek pertama oleh user: build lewat GitHub Actions (.github/workflows/build-apk.yml), salin log error kompilasi ke AI berikutnya bila ada.

Update Stage 08: tambah skin/SkinRepository.kt + integrasi SkinManager (unduh/cache offline), assets/config/skin-repo.json, docs/skin-repo.md. Belum dikompilasi. Lihat root CHECKPOINT.md.

Update Stage 09: validator diperluas + rantai fallback di SkinManager; docs/validation.md. Belum dikompilasi.

Update Stage 10: PROMPT_SKIN.md lengkap di skins-repo/. Tidak ada perubahan kode APK.

Update Stage 11: README + docs dirapikan (architecture, mapping-table, unsupported-elements); default skin tanpa c2pa.

Update Stage 12-13: validasi final (validation/stage12-final.md di arsip proyek). Proyek siap dibuild; lihat root CHECKPOINT.md di arsip.

Update Stage 14: pemilih skin (SkinPickerDialog, menu melayang baris SKIN). Belum dibuild.

Update Stage 15 (Skin v2): renderer ditulis ulang (skala via Canvas, root SVG dinormalisasi), SkinHost ditulis ulang (sentuhan tidak restart, tanpa inset ganda, tanpa menu melayang), NativeFloatingMenu DIHAPUS, 8 tombol skin-01 yang dulu dekoratif jadi kontrol (skin v1.2), FunctionRegistry + dispatcher diperluas, MusicPanelDialog baru. Lihat docs/skin-v2.md. BELUM dikompilasi.

Update Stage 16 (Skin v3): default = UI klasik (bukan skin), dialog buatan dihapus (pakai panel asli APK), layout 2 zona (pad melar, panel skin bisa disembunyikan), tab kanan 3 tombol, skin.json v1.3 (layout). Lihat docs/skin-v3.md. BELUM dikompilasi.

Update Stage 17 (Skin v3.1): latar panel melar + isi seragam (tinggi maks 150dp), render tidak lagi bergantung status panel (bug panel hilang), render bertahap + cache disk, skin.json v1.4. Lihat docs/skin-v3.1.md. BELUM dikompilasi.

Update Stage 18 (Skin v3.2): SELURUH skin di-stretch (satu gambar, tanpa pemisahan zona/latar panel), knob diputar lalu di-stretch, skin.json v1.5. Lihat docs/skin-v3.2.md. BELUM dikompilasi.
