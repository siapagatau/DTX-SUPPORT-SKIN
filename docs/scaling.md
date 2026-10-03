# Scaling & Responsive (v3.2)

Kode: `skin/SkinLayout.kt` (`SkinZone`, `SkinZones`, `SkinLayout`), `skin/HitTester.kt` (ruang layar), `skin/SkinHost.kt`.
- SELURUH skin (zona pad + zona panel) di-stretch memenuhi area skin (skala X/Y boleh beda) - tanpa area kosong, tanpa pemisahan zona.
- Panel disembunyikan -> hanya zona pad yang di-stretch memenuhi area; kontrol panel tidak tampil dan tidak bisa disentuh.
- Render: sekali pada ukuran acuan, TIDAK bergantung status panel; render ulang hanya bila ukuran area keluar dari 60%..120% acuan. Hasil di-cache ke disk.
- Knob diputar dulu lalu di-stretch bersama skin. Sentuh: hit area dipetakan ke layar; tombol/knob kecil diperluas sampai 40dp, pad tidak diperluas.
- Bar sistem disembunyikan (immersive); inset ditangani PadScreen (`safeDrawing`), SkinHost tidak menambah inset.
