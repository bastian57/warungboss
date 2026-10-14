package com.daftech.warungboss.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daftech.warungboss.data.MenuItemEntity
import com.daftech.warungboss.data.OrderEntity
import com.daftech.warungboss.theme.Bata
import com.daftech.warungboss.theme.Emas
import com.daftech.warungboss.theme.Hijau
import com.daftech.warungboss.theme.Kuning
import com.daftech.warungboss.util.formatRupiah
import com.daftech.warungboss.util.formatTime

@Composable
fun DashboardScreen(vm: MainViewModel, onNav: (String) -> Unit) {
    val orders by vm.orders.collectAsState()
    val menu by vm.menu.collectAsState()
    val stock by vm.stock.collectAsState()
    val today = vm.todayOrders()
    val omzet = vm.todayOmzet()
    val low = stock.filter { it.quantity <= it.minQty }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Beranda", vm.shopName + "  •  Hari ini")
        PremiumCard {
            Text("Omzet Hari Ini", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(formatRupiah(omzet), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Bata)
            Text("${today.size} transaksi  •  ${today.size} order", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val bars = listOf(40, 65, 50, 80, 70, 95, 100)
                bars.forEach { h ->
                    Box(modifier = Modifier.size(width = 22.dp, height = (h * 0.5f).dp).clip(RoundedCornerShape(6.dp)).background(Emas))
                }
            }
            Text("Grafik 7 hari (mini) — detail di Laporan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PremiumCard(modifier = Modifier.weight(1f)) { Text("Pesanan Aktif", fontWeight = FontWeight.Bold); Text("${orders.count { it.status != "SELESAI" }}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Bata) }
            PremiumCard(modifier = Modifier.weight(1f)) { Text("Stok Menipis", fontWeight = FontWeight.Bold); Text("${low.size}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Kuning) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PremiumCard(modifier = Modifier.weight(1f)) { Text("Menu Aktif", fontWeight = FontWeight.Bold); Text("${menu.count { it.visible }}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) }
            PremiumCard(modifier = Modifier.weight(1f)) { Text("Total Menu", fontWeight = FontWeight.Bold); Text("${menu.size}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) }
        }
        SectionTitle("Menu Cepat")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Kasir" to "kasir", "Pesanan" to "pesanan", "Stok" to "stok", "Laporan" to "laporan", "Menu" to "menu", "Pengaturan" to "pengaturan").forEach { (label, route) ->
                Button(onClick = { onNav(route) }, colors = ButtonDefaults.buttonColors(containerColor = Bata), shape = RoundedCornerShape(16.dp)) { Text(label) }
            }
        }
        SectionTitle("Pesanan Terbaru")
        if (today.isEmpty()) EmptyState("🍲", "Belum ada pesanan hari ini", "Mulai dari Kasir — tap menu, pilih variasi, bayar.")
        else today.take(5).forEach { o -> OrderCard(o, vm) }
    }
}

@Composable
fun OrderCard(o: OrderEntity, vm: MainViewModel) {
    val color = when (o.status) { "BARU" -> Bata; "DIMASAK" -> Kuning; "SIAP" -> Hijau; else -> Color.Gray }
    PremiumCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(o.orderNo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(o.status, color = color, fontWeight = FontWeight.Bold)
        }
        Text("${o.type} ${o.tableLabel} • ${formatTime(o.createdAt)}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Text(o.itemsLabel)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatRupiah(o.total), fontWeight = FontWeight.ExtraBold)
            Text(o.payment, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CashierScreen(vm: MainViewModel, onCheckout: () -> Unit, onReceipt: () -> Unit) {
    val menu by vm.menu.collectAsState()
    val cart by vm.cart.collectAsState()
    val stock by vm.stock.collectAsState()
    var cat by remember { mutableStateOf("Semua") }
    val cats = listOf("Semua", "Bakso", "Mie Ayam", "Minuman", "Tambahan")
    val filtered = menu.filter { (cat == "Semua" || it.category == cat) && it.visible }
    val low = stock.filter { it.quantity <= it.minQty }
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (low.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3C4)), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text("⚠️ Stok menipis: " + low.joinToString(", ") { it.name }, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            cats.forEach { c ->
                FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Emas))
            }
        }
        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) EmptyState("🍜", "Menu kosong", "Tambah menu di Kelola Menu.")
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(filtered) { m ->
                val habis = m.stock <= 0
                Card(
                    modifier = Modifier.fillMaxWidth().alpha(if (habis) 0.45f else 1f).clickable(enabled = !habis) { vm.addToCart(m) },
                    shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(m.emoji, fontSize = 34.sp)
                        Text(m.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(m.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(formatRupiah(m.price), fontWeight = FontWeight.ExtraBold, color = Bata)
                        Text(if (habis) "HABIS" else "Stok: ${m.stock}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (cart.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Bata), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().clickable { onCheckout() }) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${cart.sumOf { it.qty }} item  •  ${formatRupiah(vm.cartTotal())}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Text("BAYAR →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CheckoutScreen(vm: MainViewModel, onDone: () -> Unit, onCancel: () -> Unit) {
    val cart by vm.cart.collectAsState()
    var type by remember { mutableStateOf("MAKAN DI TEMPAT") }
    var table by remember { mutableStateOf("Meja 1") }
    var payment by remember { mutableStateOf("TUNAI") }
    var customer by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var paidStr by remember { mutableStateOf("") }
    var pedas by remember { mutableStateOf("Pedas Lv.1") }
    var extra by remember { mutableStateOf(setOf<String>()) }
    val total = vm.cartTotal()
    val paid = paidStr.toIntOrNull() ?: 0
    val kembali = (paid - total).coerceAtLeast(0)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Pembayaran", "Cek pesanan lalu konfirmasi")
        cart.forEach { line ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${line.qty}x ${line.menu.name}")
                Text(formatRupiah(line.menu.price * line.qty), fontWeight = FontWeight.Bold)
            }
        }
        StatRow("Total", formatRupiah(total))
        Text("Tipe Order", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("MAKAN DI TEMPAT", "BUNGKUS", "OJOL").forEach { t ->
                FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t, fontSize = 12.sp) })
            }
        }
        if (type == "MAKAN DI TEMPAT") {
            Text("Meja", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                (1..10).forEach { i ->
                    val label = "Meja $i"
                    FilterChip(selected = table == label, onClick = { table = label }, label = { Text(label) })
                }
            }
        }
        Text("Variasi", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Pedas Lv.1", "Pedas Lv.2", "Pedas Lv.3").forEach { p ->
                FilterChip(selected = pedas == p, onClick = { pedas = p }, label = { Text(p) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Kuah Pisah", "Tanpa Sayur", "Setengah Porsi").forEach { e ->
                FilterChip(selected = extra.contains(e), onClick = { extra = if (extra.contains(e)) extra - e else extra + e }, label = { Text(e) })
            }
        }
        Text("Pembayaran", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("TUNAI", "TRANSFER", "BELUM_BAYAR").forEach { p ->
                FilterChip(selected = payment == p, onClick = { payment = p }, label = { Text(p, fontSize = 12.sp) })
            }
        }
        if (payment == "TUNAI") {
            OutlinedTextField(value = paidStr, onValueChange = { paidStr = it.filter { ch -> ch.isDigit() } }, label = { Text("Uang dibayar") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(20000, 50000, 100000).forEach { v ->
                    Button(onClick = { paidStr = v.toString() }) { Text(formatRupiah(v)) }
                }
            }
            Text("Kembalian: ${formatRupiah(kembali)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Hijau)
        }
        if (payment == "TRANSFER") Text("Transfer ke: ${vm.shopRek} — konfirmasi manual oleh kasir.", fontWeight = FontWeight.Bold)
        if (payment == "BELUM_BAYAR") {
            OutlinedTextField(value = customer, onValueChange = { customer = it }, label = { Text("Nama pelanggan") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("No HP") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan dapur") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {
            val fullNote = (listOf(pedas) + extra.sorted() + if (note.isNotBlank()) listOf(note) else emptyList()).joinToString(", ")
            vm.checkout(type, if (type == "MAKAN DI TEMPAT") table else type, payment, customer, phone, fullNote, if (payment == "TUNAI") paid else total) { onDone() }
        }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Bata)) { Text("Konfirmasi  •  ${formatRupiah(total)}", fontSize = 17.sp) }
        TextButton(onClick = onCancel) { Text("Batal") }
    }
}

@Composable
fun OrdersScreen(vm: MainViewModel) {
    val orders by vm.orders.collectAsState()
    val active = orders.filter { it.status != "SELESAI" }
    var showDialog by remember { mutableStateOf<OrderEntity?>(null) }
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        SectionTitle("Pesanan & Dapur", "Tap kartu untuk pindah status")
        if (active.isEmpty()) EmptyState("👨‍🍳", "Dapur kosong", "Order baru dari Kasir akan muncul di sini.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(active) { o ->
                Card(modifier = Modifier.fillMaxWidth().clickable { showDialog = o }, shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(o.orderNo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text(o.status, fontWeight = FontWeight.Bold, color = when (o.status) { "BARU" -> Bata; "DIMASAK" -> Kuning; "SIAP" -> Hijau; else -> Color.Gray })
                        }
                        Text("${o.type} ${o.tableLabel} • ${formatTime(o.createdAt)}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        Text(o.itemsLabel)
                        if (o.note.isNotBlank()) Text("Catatan: ${o.note}", fontWeight = FontWeight.Bold)
                        Text(formatRupiah(o.total), fontWeight = FontWeight.ExtraBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("BARU", "DIMASAK", "SIAP", "SELESAI").forEach { s ->
                                Button(onClick = { vm.setOrderStatus(o, s) }, colors = ButtonDefaults.buttonColors(containerColor = if (o.status == s) Bata else Color.LightGray)) { Text(s, fontSize = 11.sp, color = if (o.status == s) Color.White else Color.Black) }
                            }
                        }
                    }
                }
            }
        }
    }
    showDialog?.let { o ->
        AlertDialog(onDismissRequest = { showDialog = null }, title = { Text(o.orderNo) }, text = { Text(o.itemsLabel + "\n" + o.note) }, confirmButton = { TextButton(onClick = { showDialog = null }) { Text("Tutup") } })
    }
}
