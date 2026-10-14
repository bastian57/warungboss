package com.daftech.warungboss.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatRupiah(n: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
    return "Rp" + nf.format(n)
}
fun formatRupiah(n: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale("in", "ID"))
    return "Rp" + nf.format(n)
}
fun formatTime(ts: Long): String = SimpleDateFormat("HH:mm", Locale("in", "ID")).format(Date(ts))
fun formatDate(ts: Long): String = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID")).format(Date(ts))
fun todayStart(): Long {
    val c = java.util.Calendar.getInstance()
    c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0)
    c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0)
    return c.timeInMillis
}
