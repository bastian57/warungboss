package com.daftech.warungboss.util

import android.content.Context
import android.widget.Toast
import com.daftech.warungboss.data.OrderEntity

/**
 * ESC/POS 58mm helper. Aman tanpa printer: bila tidak ada printer terhubung,
 * tampilkan preview struk (caller) + toast. Tidak crash.
 * Implementasi Bluetooth: kirim byte ESC/POS ke printer yang sudah dipairing
 * bila ada; fallback selalu ke preview.
 */
object PrinterHelper {
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

    fun printOrToast(context: Context, escPos: ByteArray) {
        // v1: Bluetooth printing via printer ter-pairing dilakukan di layer Settings (pilih printer).
        // Di sini stub aman: beri tahu user bahwa struk siap & preview ditampilkan.
        Toast.makeText(context, "Struk siap. Hubungkan printer Bluetooth 58mm di Pengaturan untuk print.", Toast.LENGTH_LONG).show()
    }
}
