package com.kendang.realpads.skin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kendang.realpads.CtrlPanelColor
import com.kendang.realpads.PadTextColor
import com.kendang.realpads.PanelColor
import com.kendang.realpads.TextDim
import com.kendang.realpads.ThemeState
import kotlinx.coroutines.launch

/**
 * Pemilih skin. Daftar = APK Asli/Klasik (default) + skin bawaan APK + katalog dari repository skin
 * (online, atau daftar tersimpan bila offline). Aksi per skin: UNDUH (+ langsung dipakai), PAKAI (sudah terunduh), UPDATE.
 * Skin yang gagal diunduh / ditolak validator tidak mengubah skin yang sedang dipakai.
 * Dipanggil dari tombol EXIT di skin (function skin.pick) dan tombol SKIN di UI klasik.
 *
 * Gaya SENGAJA disamakan dengan dialog lain di app (OnlinePresetListDialog dkk): AlertDialog dengan
 * containerColor = PanelColor, judul PadTextColor tebal + ikon refresh di kanan, baris daftar
 * CtrlPanelColor (rounded 8dp), ikon/aksi warna aksen tema, tombol tutup "TUTUP" CtrlPanelColor.
 */
@Composable
fun SkinPickerDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<SkinRepository.Entry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var online by remember { mutableStateOf(false) }
    var busyId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf(SkinManager.selectedId(context)) }
    var tick by remember { mutableStateOf(0) } // naik tiap status unduhan berubah
    val configured = remember { SkinRepository.indexUrl(context) != null }
    // Nama skin yang dibundel di assets (null bila tidak ada / tidak terbaca) - ditampilkan sebagai pilihan offline.
    val bundledName = remember {
        try { SkinLoader.loadFromAssets(context).name.ifBlank { null } } catch (_: Throwable) { null }
    }

    fun refresh() {
        scope.launch {
            loading = true
            entries = SkinManager.refreshCatalog(context)
            online = SkinRepository.lastRefreshOnline
            loading = false
        }
    }
    LaunchedEffect(Unit) { refresh() }

    fun finish(ok: Boolean, okMsg: String, failMsg: String) {
        selected = SkinManager.selectedId(context)
        tick++
        message = if (ok) okMsg else failMsg
        busyId = null
    }

    fun useInstalled(id: String, name: String) {
        if (busyId != null) return
        busyId = id; message = null
        scope.launch {
            val ok = SkinManager.useSkin(context, id)
            finish(ok, "Skin \"$name\" dipakai", "Skin \"$name\" tidak bisa dipakai; skin sebelumnya tetap aktif")
        }
    }

    fun download(e: SkinRepository.Entry) {
        if (busyId != null) return
        busyId = e.id; message = null
        scope.launch {
            val err = SkinManager.downloadAndUse(context, e)
            finish(err == null, "Skin \"${e.name}\" diunduh dan dipakai", "Gagal: $err")
        }
    }

    AlertDialog(
        // Selama unduh/pakai berjalan, dialog tidak boleh tertutup tak sengaja.
        onDismissRequest = { if (busyId == null) onDismiss() },
        containerColor = PanelColor,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Pilih Skin",
                    color = PadTextColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (loading) {
                    CircularProgressIndicator(
                        color = ThemeState.accentColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    IconButton(onClick = { if (busyId == null) refresh() }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Muat ulang daftar skin",
                            tint = TextDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.width(300.dp)) {
                // status daftar
                val status = when {
                    loading -> "Mengambil daftar skin..."
                    !configured && entries.isEmpty() -> "Repository skin belum dikonfigurasi. Hanya pilihan bawaan yang tersedia."
                    !online && entries.isEmpty() -> "Tidak ada koneksi dan belum ada daftar tersimpan."
                    !online -> "Offline - menampilkan daftar tersimpan."
                    else -> null
                }
                if (status != null) {
                    Text(status, color = TextDim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Default: tampilan ASLI APK (tanpa skin). Selalu ada, offline, tanpa loading.
                    item(key = SkinManager.CLASSIC_ID) {
                        val isActive = selected == SkinManager.CLASSIC_ID
                        SkinRow(
                            title = "APK Asli (Klasik)", subtitle = "Tampilan bawaan APK, tanpa skin",
                            active = isActive,
                            action = if (isActive) null else "PAKAI",
                            busy = busyId == SkinManager.CLASSIC_ID,
                            enabled = busyId == null
                        ) { useInstalled(SkinManager.CLASSIC_ID, "APK Asli") }
                    }
                    // Skin yang dibundel di APK (offline). Disembunyikan bila repository sudah menyediakan skin dengan nama yang sama.
                    if (bundledName != null && entries.none { it.name == bundledName }) {
                        item(key = SkinManager.BUNDLED_ID) {
                            val isActive = selected == SkinManager.BUNDLED_ID
                            SkinRow(
                                title = bundledName, subtitle = "Skin bawaan APK (offline)",
                                active = isActive,
                                action = if (isActive) null else "PAKAI",
                                busy = busyId == SkinManager.BUNDLED_ID,
                                enabled = busyId == null
                            ) { useInstalled(SkinManager.BUNDLED_ID, bundledName) }
                        }
                    }
                    items(entries, key = { it.id }) { e ->
                        // tick di key remember: dihitung ulang setelah unduhan selesai
                        val downloaded = remember(tick, e.id) { SkinRepository.isDownloaded(context, e.id) }
                        val cachedVer = remember(tick, e.id) { if (downloaded) SkinRepository.cachedVersion(context, e.id) else null }
                        val needsUpdate = downloaded && cachedVer != null && cachedVer != e.version
                        val isActive = selected == e.id
                        val pads = if (e.padMin == e.padMax) "${e.padMax} pad" else "${e.padMin}-${e.padMax} pad"
                        val sub = buildString {
                            append("v${e.version}")
                            if (e.author.isNotBlank()) append(" · ${e.author}")
                            append(" · $pads")
                            if (needsUpdate) append(" · update dari v$cachedVer")
                            if (!downloaded) append(" · belum diunduh")
                        }
                        val action = when {
                            needsUpdate -> "UPDATE"
                            !downloaded -> "UNDUH"
                            isActive -> null
                            else -> "PAKAI"
                        }
                        SkinRow(
                            title = e.name, subtitle = sub, active = isActive, action = action,
                            busy = busyId == e.id, enabled = busyId == null
                        ) {
                            if (!downloaded || needsUpdate) download(e) else useInstalled(e.id, e.name)
                        }
                    }
                }

                if (message != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(message!!, color = PadTextColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                enabled = busyId == null,
                colors = ButtonDefaults.buttonColors(containerColor = CtrlPanelColor, contentColor = PadTextColor)
            ) {
                Text("TUTUP", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    )
}

// Baris skin: sama bentuknya dengan OnlinePresetRow (CtrlPanelColor, rounded 8dp, seluruh baris bisa di-tap).
// Kanan: spinner aksen saat proses, ikon unduh untuk UNDUH/UPDATE, teks aksen untuk PAKAI, centang untuk yang aktif.
@Composable
private fun SkinRow(
    title: String, subtitle: String, active: Boolean, action: String?, busy: Boolean, enabled: Boolean,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(CtrlPanelColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled && action != null, onClick = onAction)
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (!enabled && !busy) TextDim else PadTextColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(subtitle, color = TextDim, fontSize = 10.sp, maxLines = 2)
        }
        Box(Modifier.width(8.dp))
        when {
            busy -> CircularProgressIndicator(
                color = ThemeState.accentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
            action == "UNDUH" || action == "UPDATE" -> Icon(
                imageVector = Icons.Filled.CloudDownload,
                contentDescription = if (action == "UPDATE") "Update skin" else "Unduh skin",
                tint = if (enabled) ThemeState.accentColor else TextDim,
                modifier = Modifier.size(18.dp)
            )
            action != null -> Text(
                action,
                color = if (enabled) ThemeState.accentColor else TextDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            active -> Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Sedang dipakai",
                tint = ThemeState.accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
