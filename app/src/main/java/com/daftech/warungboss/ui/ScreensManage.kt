package com.daftech.warungboss.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftech.warungboss.data.MenuItemEntity
import com.daftech.warungboss.data.StockItemEntity
import com.daftech.warungboss.data.StaffEntity
import com.daftech.warungboss.theme.Bata
import com.daftech.warungboss.theme.Emas
import com.daftech.warungboss.theme.Hijau
import com.daftech.warungboss.util.BackupHelper
import com.daftech.warungboss.util.PrinterHelper
import com.daftech.warungboss.util.formatRupiah
import com.daftech.warungboss.util.formatTime
import com.google.gson.Gson

@Composable
fun MenuManageScreen(vm: MainViewModel) {
    val menu by vm.menu.collectAsState()
    var editing by remember { mutableStateOf<MenuItemEntity?>(null) }
    var isNew by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        SectionTitle("Kelola Menu", "Tambah / edit / on-off tampil. Preview live di form.")
        Button(onClick = { editing = MenuItemEntity(name = "", category = "Bakso", price = 0, stock = 99, emoji = "🍲"); isNew = true }, colors = ButtonDefaults.buttonColors(containerColor = Bata), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("+ Tambah Menu") }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(menu) { m ->
                PremiumCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${m.emoji}  ${m.name}", fontWeight = FontWeight.Bold)
                            Text("${m.category} • ${formatRupiah(m.price)} • Stok ${m.stock}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        }
                        Switch(checked = m.visible, onCheckedChange = { vm.saveMenu(m.copy(visible = it), false) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { editing = m; isNew = false }) { Text("Edit") }
                        Button(onClick = { vm.deleteMenu(m.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) { Text("Hapus", color = Color.Black) }
                    }
                }
            }
        }
    }
    editing?.let { m0 ->
        var name by remember { mutableStateOf(m0.name) }
        var cat by remember { mutableStateOf(m0.category) }
        var price by remember { mutableStateOf(m0.price.toString()) }
        var stockS by remember { mutableStateOf(m0.stock.toString()) }
        var emoji by remember { mutableStateOf(m0.emoji) }
        AlertDialog(onDismissRequest = { editing = null }, title = { Text(if (isNew) "Tambah Menu" else "Edit Menu") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumCard { Text("Preview: $emoji  ${if (name.isBlank()) "Nama Menu" else name}"); Text(formatRupiah(price.toIntOrNull() ?: 0), fontWeight = FontWeight.ExtraBold, color = Bata) }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, singleLine = true)
                OutlinedTextField(value = cat, onValueChange = { cat = it }, label = { Text("Kategori (Bakso/Mie Ayam/Minuman/Tambahan)") }, singleLine = true)
                OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() } }, label = { Text("Harga") }, singleLine = true)
                OutlinedTextField(value = stockS, onValueChange = { stockS = it.filter { c -> c.isDigit() } }, label = { Text("Stok") }, singleLine = true)
                OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("Emoji (🍲🍜🧋...)") }, singleLine = true)
            }
        }, confirmButton = {
            Button(onClick = {
                vm.saveMenu(m0.copy(name = name, category = cat, price = price.toIntOrNull() ?: 0, stock = stockS.toIntOrNull() ?: 0, emoji = emoji.ifBlank { "🍲" }), isNew)
                editing = null
            }) { Text("Simpan") }
        }, dismissButton = { TextButton(onClick = { editing = null }) { Text("Batal") } })
    }
}

@Composable
fun StockScreen(vm: MainViewModel) {
    val stock by vm.stock.collectAsState()
    var editing by remember { mutableStateOf<StockItemEntity?>(null) }
    var isNew by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        SectionTitle("Stok Harian", "Per porsi. Merah = segera habis.")
        Button(onClick = { editing = StockItemEntity(name = "", unit = "porsi", quantity = 0, minQty = 10); isNew = true }, colors = ButtonDefaults.buttonColors(containerColor = Bata), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("+ Tambah Bahan") }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(stock) { s ->
                val low = s.quantity <= s.minQty
                PremiumCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s.name, fontWeight = FontWeight.Bold)
                        Text("${s.quantity} ${s.unit}", fontWeight = FontWeight.ExtraBold, color = if (low) Bata else Hijau)
                    }
                    Text(if (low) "⚠️ Segera habis (min ${s.minQty})" else "Aman (min ${s.minQty})", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.saveStock(s.copy(quantity = (s.quantity - 1).coerceAtLeast(0)), false) }) { Text("−") }
                        Button(onClick = { vm.saveStock(s.copy(quantity = s.quantity + 1), false) }) { Text("+") }
                        Button(onClick = { editing = s; isNew = false }) { Text("Edit") }
                        Button(onClick = { vm.deleteStock(s.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) { Text("Hapus", color = Color.Black) }
                    }
                }
            }
        }
    }
    editing?.let { s0 ->
        var name by remember { mutableStateOf(s0.name) }
        var qty by remember { mutableStateOf(s0.quantity.toString()) }
        var minQ by remember { mutableStateOf(s0.minQty.toString()) }
        AlertDialog(onDismissRequest = { editing = null }, title = { Text("Stok") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama bahan") }, singleLine = true)
                OutlinedTextField(value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() } }, label = { Text("Jumlah") }, singleLine = true)
                OutlinedTextField(value = minQ, onValueChange = { minQ = it.filter { c -> c.isDigit() } }, label = { Text("Batas habis") }, singleLine = true)
            }
        }, confirmButton = { Button(onClick = { vm.saveStock(s0.copy(name = name, quantity = qty.toIntOrNull() ?: 0, minQty = minQ.toIntOrNull() ?: 10), isNew); editing = null }) { Text("Simpan") } }, dismissButton = { TextButton(onClick = { editing = null }) { Text("Batal") } })
    }
}

@Composable
fun ReportScreen(vm: MainViewModel) {
    val orders by vm.orders.collectAsState()
    val ctx = LocalContext.current
    val today = vm.todayOrders()
    val omzet = vm.todayOmzet()
    val top = vm.topMenu()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Laporan", "Omzet, grafik 7 hari, menu terlaris")
        PremiumCard {
            Text("Omzet Hari Ini", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(formatRupiah(omzet), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Bata)
            StatRow("Transaksi hari ini", "${today.size}")
            StatRow("Total order (semua)", "${orders.size}")
            StatRow("Total omzet (semua)", formatRupiah(orders.sumOf { it.total }))
        }
        PremiumCard {
            Text("Top 5 Menu Terlaris", fontWeight = FontWeight.Bold)
            if (top.isEmpty()) Text("Belum ada penjualan — mulai dari Kasir.")
            else top.forEachIndexed { i, (n, q) -> StatRow("${i + 1}. $n", "$q porsi") }
        }
        PremiumCard {
            Text("Riwayat Terbaru", fontWeight = FontWeight.Bold)
            if (orders.isEmpty()) Text("Belum ada order.")
            else orders.take(8).forEach { o -> StatRow("${o.orderNo} • ${formatTime(o.createdAt)} • ${o.payment}", formatRupiah(o.total)) }
        }
        Button(onClick = {
            val text = "REKAP ${vm.shopName}\nOmzet hari ini: ${formatRupiah(omzet)}\nTransaksi: ${today.size}\nTop: " + top.joinToString(", ") { "${it.first} (${it.second})" } + "\n\napk by DAFTECH"
            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
            ctx.startActivity(Intent.createChooser(intent, "Share rekap via WA"))
        }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Emas)) { Text("Share Rekap ke WA", color = Color.Black) }
        Text("Print 58mm & PDF: gunakan tombol di layar Struk / Pengaturan (backup).", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun SettingsScreen(vm: MainViewModel, dark: Boolean, onDark: (Boolean) -> Unit, onLogout: () -> Unit) {
    val ctx = LocalContext.current
    val staff by vm.staff.collectAsState()
    var name by remember { mutableStateOf(vm.shopName) }
    var addr by remember { mutableStateOf(vm.shopAddress) }
    var rek by remember { mutableStateOf(vm.shopRek) }
    var printer by remember { mutableStateOf(vm.printerName) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Pengaturan", "Profil toko, karyawan, printer, backup")
        PremiumCard {
            Text("Profil Toko", fontWeight = FontWeight.Bold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama warung") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = addr, onValueChange = { addr = it }, label = { Text("Alamat") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = rek, onValueChange = { rek = it }, label = { Text("No. rekening transfer") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = { vm.shopName = name; vm.shopAddress = addr; vm.shopRek = rek; Toast.makeText(ctx, "Profil disimpan", Toast.LENGTH_SHORT).show() }, colors = ButtonDefaults.buttonColors(containerColor = Bata)) { Text("Simpan Profil") }
        }
        PremiumCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mode Gelap (semua layer)", fontWeight = FontWeight.Bold)
                Switch(checked = dark, onCheckedChange = onDark)
            }
            OutlinedTextField(value = printer, onValueChange = { printer = it }, label = { Text("Printer Bluetooth 58mm") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = { vm.printerName = printer; Toast.makeText(ctx, "Printer disimpan: $printer", Toast.LENGTH_SHORT).show() }) { Text("Simpan Printer") }
        }
        PremiumCard {
            Text("Karyawan", fontWeight = FontWeight.Bold)
            staff.forEach { s -> StatRow("${s.name} (${s.role})", "PIN ${s.pin}") }
            Text("Karyawan dikelola di database. Default: Owner 1234, Kasir 1111, Darurat 9999.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        PremiumCard {
            Text("Backup Lokal", fontWeight = FontWeight.Bold)
            Text("Backup JSON disimpan di folder app (offline).", fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val json = Gson().toJson(mapOf("shop" to vm.shopName, "orders" to vm.orders.value.size, "menu" to vm.menu.value.size, "ts" to System.currentTimeMillis()))
                    val f = BackupHelper.exportJson(ctx, json)
                    Toast.makeText(ctx, "Backup tersimpan: ${f.name}", Toast.LENGTH_LONG).show()
                }) { Text("Backup") }
                Button(onClick = {
                    Toast.makeText(ctx, "Restore: pilih file backup JSON dari file manager, lalu import di versi berikutnya / hubungi DAFTECH.", Toast.LENGTH_LONG).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) { Text("Restore", color = Color.Black) }
            }
        }
        PremiumCard {
            Text("Tentang", fontWeight = FontWeight.Bold)
            Text("WarungBoss v1.0.0 • Offline 100% • Tanpa internet, tanpa iklan")
            Text("Lisensi: Rp1.000.000 / device (device-bound)")
            Text("apk by DAFTECH", fontWeight = FontWeight.Bold)
        }
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) { Text("Keluar (Ganti PIN / User)", color = Color.Black) }
    }
}

@Composable
fun ReceiptScreen(vm: MainViewModel, onPrint: () -> Unit) {
    val last by vm.lastOrder.collectAsState()
    val ctx = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Struk 58mm", "Preview persis hasil print")
        if (last == null) {
            EmptyState("🧾", "Belum ada transaksi", "Selesaikan 1 order di Kasir, struk akan muncul di sini.")
        } else {
            val o = last!!
            PremiumCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(vm.shopName, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(vm.shopAddress, fontSize = 13.sp)
                    Text("--------------------------------", fontSize = 13.sp)
                }
                Text("No: ${o.orderNo}  •  ${formatTime(o.createdAt)}")
                Text("Tipe: ${o.type}  ${o.tableLabel}")
                if (o.customerName.isNotBlank()) Text("Pelanggan: ${o.customerName}")
                if (o.note.isNotBlank()) Text("Catatan: ${o.note}")
                Text("--------------------------------")
                Text(o.itemsLabel)
                Text("--------------------------------")
                Text("TOTAL: ${formatRupiah(o.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Bata)
                Text("Bayar: ${o.payment} ${formatRupiah(o.paidAmount)}")
                if (o.payment == "TUNAI") Text("Kembali: ${formatRupiah(o.changeAmount)}")
                Text("--------------------------------")
                Text("Terima kasih sudah mampir!", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("apk by DAFTECH", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
            }
            Button(onClick = {
                val esc = PrinterHelper.buildEscPos(vm.shopName, vm.shopAddress, o, o.itemsLabel)
                PrinterHelper.printOrToast(ctx, esc)
                onPrint()
            }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Bata)) { Text("Print 58mm") }
            Button(onClick = {
                val text = PrinterHelper.buildReceiptText(vm.shopName, vm.shopAddress, o, o.itemsLabel)
                val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                ctx.startActivity(Intent.createChooser(intent, "Share struk"))
            }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Emas)) { Text("Share Struk (WA)", color = Color.Black) }
        }
    }
}
