package com.daftech.warungboss.util

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.daftech.warungboss.data.OrderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ESC/POS 58mm helper + Bluetooth SPP printing.
 * Aman: semua SecurityException / bluetooth error ditangkap jadi toast, tidak crash.
 */
object PrinterHelper {
    private const val PREFS = "warungboss_shop"
    private const val KEY_MAC = "printer_mac"
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    data class BtDevice(val name: String, val address: String)

    fun getMac(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MAC, "") ?: ""

    fun saveMac(context: Context, mac: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_MAC, mac).apply()
    }

    /** Daftar perangkat Bluetooth yang sudah di-pairing. Kosong bila bluetooth mati / tanpa izin. */
    fun bondedDevices(context: Context): List<BtDevice> {
        return try {
            val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                ?: return emptyList()
            val adapter = mgr.adapter ?: return emptyList()
            if (!adapter.isEnabled) return emptyList()
            adapter.bondedDevices
                .map { BtDevice(it.name ?: "Tanpa nama", it.address) }
                .sortedBy { it.name.lowercase() }
        } catch (e: SecurityException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Kirim byte ESC/POS via Bluetooth SPP (thread IO). onResult dipanggil di main thread. */
    fun sendBytes(context: Context, mac: String, data: ByteArray, onResult: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var ok = false
            var msg: String
            try {
                val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val adapter = mgr.adapter ?: throw IllegalStateException("Bluetooth tidak tersedia di HP ini")
                if (!adapter.isEnabled) throw IllegalStateException("Bluetooth HP mati — nyalakan dulu")
                val device = adapter.getRemoteDevice(mac)
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                try {
                    adapter.cancelDiscovery()
                    socket.connect()
                    val out = socket.outputStream
                    out.write(data)
                    out.flush()
                    try { Thread.sleep(400) } catch (_: InterruptedException) { }
                    ok = true
                    msg = "Terkirim ke printer"
                } finally {
                    try { socket.close() } catch (_: Exception) { }
                }
            } catch (e: SecurityException) {
                msg = "Gagal: izin Bluetooth ditolak"
            } catch (e: IllegalArgumentException) {
                msg = "Gagal: alamat printer tidak valid"
            } catch (e: Exception) {
                msg = "Gagal: ${e.message ?: "tidak diketahui"}"
            }
            val result = msg
            Handler(Looper.getMainLooper()).post { onResult(ok, result) }
        }
    }

    fun buildTestEscPos(): ByteArray {
        val esc = 0x1B.toByte()
        val out = mutableListOf<Byte>()
        fun add(vararg b: Byte) { out.addAll(b.toList()) }
        fun text(s: String) { out.addAll(s.toByteArray(Charsets.UTF_8).toList()); out.add(0x0A) }
        add(esc, 0x40) // init
        add(esc, 0x61, 0x01) // center
        add(esc, 0x21, 0x10) // double height
        text("TES CETAK")
        add(esc, 0x21, 0x00)
        text("WarungBoss")
        text("${formatDate(System.currentTimeMillis())} ${formatTime(System.currentTimeMillis())}")
        text("--------------------------------")
        text("Printer Bluetooth OK")
        text("apk by DAFTECH")
        add(esc, 0x61, 0x00)
        return out.toByteArray()
    }

    fun buildReceiptText(shopName: String, shopAddress: String, order: OrderEntity, itemsLabel: String): String {
        val sb = StringBuilder()
        sb.appendLine("        $shopName")
        if (shopAddress.isNotBlank()) sb.appendLine("        $shopAddress")
        sb.appendLine("--------------------------------")
        sb.appendLine("No: ${order.orderNo}  ${formatDate(order.createdAt)} ${formatTime(order.createdAt)}")
        sb.appendLine("Tipe: ${order.type}  ${order.tableLabel}")
        if (order.customerName.isNotBlank()) sb.appendLine("Pelanggan: ${order.customerName}")
        if (order.note.isNotBlank()) sb.appendLine("Catatan: ${order.note}")
        sb.appendLine("--------------------------------")
        sb.appendLine(itemsLabel)
        sb.appendLine("--------------------------------")
        sb.appendLine("TOTAL: ${formatRupiah(order.total)}")
        sb.appendLine("Bayar: ${order.payment} ${formatRupiah(order.paidAmount)}")
        if (order.payment == "TUNAI") sb.appendLine("Kembali: ${formatRupiah(order.changeAmount)}")
        sb.appendLine("--------------------------------")
        sb.appendLine("   Terima kasih sudah mampir!")
        sb.appendLine("       apk by DAFTECH")
        return sb.toString()
    }

    fun buildEscPos(shopName: String, shopAddress: String, order: OrderEntity, itemsLabel: String): ByteArray {
        val esc = 0x1B.toByte(); val gs = 0x1D.toByte()
        val out = mutableListOf<Byte>()
        fun add(vararg b: Byte) { out.addAll(b.toList()) }
        fun text(s: String) { out.addAll(s.toByteArray(Charsets.UTF_8).toList()); out.add(0x0A) }
        add(esc, 0x40) // init
        add(esc, 0x61, 0x01) // center
        add(esc, 0x21, 0x10) // double height
        text(shopName)
        add(esc, 0x21, 0x00)
        if (shopAddress.isNotBlank()) text(shopAddress)
        add(esc, 0x61, 0x00)
        text("--------------------------------")
        text("No: ${order.orderNo}  ${formatDate(order.createdAt)} ${formatTime(order.createdAt)}")
        text("Tipe: ${order.type}  ${order.tableLabel}")
        if (order.customerName.isNotBlank()) text("Pelanggan: ${order.customerName}")
        if (order.note.isNotBlank()) text("Catatan: ${order.note}")
        text("--------------------------------")
        itemsLabel.lines().forEach { text(it) }
        text("--------------------------------")
        add(esc, 0x21, 0x08)
        text("TOTAL: ${formatRupiah(order.total)}")
        add(esc, 0x21, 0x00)
        text("Bayar: ${order.payment} ${formatRupiah(order.paidAmount)}")
        if (order.payment == "TUNAI") text("Kembali: ${formatRupiah(order.changeAmount)}")
        text("--------------------------------")
        add(esc, 0x61, 0x01)
        text("Terima kasih sudah mampir!")
        text("apk by DAFTECH")
        add(esc, 0x61, 0x00)
        add(gs, 0x56, 0x00) // cut (sebagian printer)
        return out.toByteArray()
    }

    /**
     * Kirim struk ke printer bila MAC tersimpan; toast bila belum ada printer.
     * return true = percobaan kirim dimulai, false = tidak ada printer (toast ditampilkan).
     */
    fun printOrToast(context: Context, escPos: ByteArray): Boolean {
        val mac = getMac(context)
        if (mac.isBlank()) {
            Toast.makeText(context, "Struk siap. Hubungkan printer di Pengaturan.", Toast.LENGTH_LONG).show()
            return false
        }
        sendBytes(context, mac, escPos) { ok, msg ->
            Toast.makeText(context, msg, if (ok) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
        }
        return true
    }
}
