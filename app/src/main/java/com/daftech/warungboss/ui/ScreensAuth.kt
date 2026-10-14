package com.daftech.warungboss.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftech.warungboss.license.LicenseManager
import com.daftech.warungboss.license.LicenseResult
import com.daftech.warungboss.theme.Bata
import com.daftech.warungboss.theme.Emas

@Composable
fun SplashScreen(onDone: () -> Unit) {
    // Splash premium: langsung tampil, tap untuk lanjut (tanpa delay lama agar kasir cepat buka)
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).clickable { onDone() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Box(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(28.dp)).background(Bata), contentAlignment = Alignment.Center) {
                Text("🍲", fontSize = 56.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("WarungBoss", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Text("Kasir Warung Bakso & Mie Ayam", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            Text("Ketuk layar untuk masuk  •  apk by DAFTECH", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f))
        }
    }
}

@Composable
fun ActivationScreen(deviceId: String, onActivated: (String) -> Unit) {
    val ctx = LocalContext.current
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("🍲", fontSize = 48.sp)
        Text("Aktivasi WarungBoss", style = MaterialTheme.typography.displayLarge)
        Text("1 device = 1 lisensi Rp1.000.000. Kirim Device ID ke penjual, masukkan kode aktivasi di bawah.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        PremiumCard {
            Text("Device ID", fontWeight = FontWeight.Bold)
            Text(deviceId, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Bata)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
                Toast.makeText(ctx, "Device ID disalin", Toast.LENGTH_SHORT).show()
            }, colors = ButtonDefaults.buttonColors(containerColor = Emas)) { Text("Salin Device ID", color = androidx.compose.ui.graphics.Color.Black) }
            Text("Tap baris ini juga menyalin.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.clickable {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
                Toast.makeText(ctx, "Device ID disalin", Toast.LENGTH_SHORT).show()
            })
        }
        OutlinedTextField(value = code, onValueChange = { code = it; error = "" }, label = { Text("Kode aktivasi (WB1-...)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (error.isNotBlank()) Text(error, color = Bata, fontWeight = FontWeight.Bold)
        Button(onClick = {
            when (val r = LicenseManager.validateLicense(code, deviceId)) {
                is LicenseResult.Valid -> {
                    LicenseManager.saveLicense(LicenseManager.prefs(ctx), code, r.buyerName, r.expiry)
                    Toast.makeText(ctx, "Aktivasi berhasil. Selamat datang, ${r.buyerName}", Toast.LENGTH_LONG).show()
                    onActivated(r.buyerName)
                }
                is LicenseResult.Invalid -> error = r.reason
            }
        }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Bata)) { Text("Aktifkan", fontSize = 17.sp) }
        Text("Demo: hubungi penjual untuk kode aktivasi. Tanpa kode valid, aplikasi tidak bisa dipakai.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("apk by DAFTECH", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        Text("🍲", fontSize = 56.sp)
        Text("Selamat Datang", style = MaterialTheme.typography.displayLarge)
        Text("Masukkan PIN untuk masuk kasir.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Text(if (pin.isEmpty()) "••••" else pin.map { "•" }.joinToString(""), fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
        if (error.isNotBlank()) Text(error, color = Bata)
        val keys = listOf("1","2","3","4","5","6","7","8","9","C","0","⌫")
        keys.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { k ->
                    Button(onClick = {
                        when (k) {
                            "C" -> pin = ""
                            "⌫" -> pin = pin.dropLast(1)
                            else -> if (pin.length < 6) pin += k
                        }
                    }, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp)) { Text(k, fontSize = 20.sp) }
                }
            }
        }
        Button(onClick = {
            when (pin) {
                "1234" -> onLogin("OWNER")
                "1111" -> onLogin("KASIR")
                "9999" -> { Toast.makeText(ctx, "PIN darurat dipakai — ganti PIN di Pengaturan", Toast.LENGTH_LONG).show(); onLogin("OWNER") }
                else -> error = "PIN salah. Coba 1234 (Owner) / 1111 (Kasir) / 9999 (Darurat)."
            }
        }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Bata)) { Text("Masuk", fontSize = 17.sp) }
        Text("PIN default: Owner 1234 • Kasir 1111 • Darurat 9999\napk by DAFTECH", fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}
