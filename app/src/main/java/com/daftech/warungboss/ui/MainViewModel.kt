package com.daftech.warungboss.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daftech.warungboss.WarungBossApp
import com.daftech.warungboss.data.DefaultData
import com.daftech.warungboss.data.MenuItemEntity
import com.daftech.warungboss.data.OrderEntity
import com.daftech.warungboss.data.StockItemEntity
import com.daftech.warungboss.data.StaffEntity
import com.daftech.warungboss.util.todayStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartLine(val menu: MenuItemEntity, var qty: Int, val note: String = "")

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as WarungBossApp).db
    val shopPrefs = app.getSharedPreferences("warungboss_shop", Context.MODE_PRIVATE)

    val menu = MutableStateFlow<List<MenuItemEntity>>(emptyList())
    val orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val stock = MutableStateFlow<List<StockItemEntity>>(emptyList())
    val staff = MutableStateFlow<List<StaffEntity>>(emptyList())

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()
    private val _lastOrder = MutableStateFlow<OrderEntity?>(null)
    val lastOrder: StateFlow<OrderEntity?> = _lastOrder.asStateFlow()
    private val _role = MutableStateFlow("OWNER")
    val role: StateFlow<String> = _role.asStateFlow()
    private val _dark = MutableStateFlow(false)
    val dark: StateFlow<Boolean> = _dark.asStateFlow()

    var shopName: String get() = shopPrefs.getString("shop_name", "Warung Pakde") ?: "Warung Pakde"
        set(v) { shopPrefs.edit().putString("shop_name", v).apply() }
    var shopAddress: String get() = shopPrefs.getString("shop_address", "Jl. Raya No. 1") ?: ""
        set(v) { shopPrefs.edit().putString("shop_address", v).apply() }
    var shopRek: String get() = shopPrefs.getString("shop_rek", "BCA 1234567890 a.n. Warung") ?: ""
        set(v) { shopPrefs.edit().putString("shop_rek", v).apply() }
    var printerName: String get() = shopPrefs.getString("printer_name", "Belum dipilih") ?: "Belum dipilih"
        set(v) { shopPrefs.edit().putString("printer_name", v).apply() }
    var printerMac: String get() = shopPrefs.getString("printer_mac", "") ?: ""
        set(v) { shopPrefs.edit().putString("printer_mac", v).apply() }

    init {
        viewModelScope.launch {
            db.menuDao().all().collect { list ->
                menu.value = list
                if (list.isEmpty() && db.menuDao().count() == 0) seed()
            }
        }
        viewModelScope.launch { db.orderDao().all().collect { orders.value = it } }
        viewModelScope.launch { db.stockDao().all().collect { stock.value = it } }
        viewModelScope.launch { db.staffDao().all().collect { staff.value = it } }
    }

    private suspend fun seed() {
        DefaultData.menu().forEach { db.menuDao().insert(it) }
        DefaultData.stock().forEach { db.stockDao().insert(it) }
        DefaultData.staff().forEach { db.staffDao().insert(it) }
    }

    fun setRole(r: String) { _role.value = r }
    fun setDark(v: Boolean) { _dark.value = v }
    fun addToCart(m: MenuItemEntity) {
        val cur = _cart.value.toMutableList()
        val idx = cur.indexOfFirst { it.menu.id == m.id }
        if (idx >= 0) cur[idx] = cur[idx].copy(qty = cur[idx].qty + 1) else cur.add(CartLine(m, 1))
        _cart.value = cur
    }
    fun decCart(m: MenuItemEntity) {
        val cur = _cart.value.toMutableList()
        val idx = cur.indexOfFirst { it.menu.id == m.id }
        if (idx >= 0) {
            if (cur[idx].qty <= 1) cur.removeAt(idx) else cur[idx] = cur[idx].copy(qty = cur[idx].qty - 1)
            _cart.value = cur
        }
    }
    fun clearCart() { _cart.value = emptyList() }
    fun cartTotal(): Int = _cart.value.sumOf { it.menu.price * it.qty }

    fun checkout(type: String, table: String, payment: String, customer: String, phone: String, note: String, paid: Int, onDone: (OrderEntity) -> Unit) {
        viewModelScope.launch {
            val total = cartTotal()
            val change = if (payment == "TUNAI") (paid - total).coerceAtLeast(0) else 0
            val label = _cart.value.joinToString("\n") { "${it.qty}x ${it.menu.name}  ${com.daftech.warungboss.util.formatRupiah(it.menu.price * it.qty)}" }
            val no = "A-%03d".format((orders.value.size + 1))
            val order = OrderEntity(
                orderNo = no, type = type, tableLabel = table, itemsJson = label, itemsLabel = label,
                total = total, payment = payment, customerName = customer, customerPhone = phone,
                status = "BARU", note = note, paidAmount = if (payment == "TUNAI") paid else total, changeAmount = change
            )
            val id = db.orderDao().insert(order)
            _lastOrder.value = order.copy(id = id.toInt())
            // Kurangi stok menu sederhana
            _cart.value.forEach { line ->
                val m = line.menu
                if (m.stock > 0) db.menuDao().update(m.copy(stock = (m.stock - line.qty).coerceAtLeast(0)))
            }
            clearCart()
            onDone(_lastOrder.value!!)
        }
    }

    fun setOrderStatus(o: OrderEntity, s: String) {
        viewModelScope.launch { db.orderDao().update(o.copy(status = s)) }
    }
    fun todayOrders(): List<OrderEntity> = orders.value.filter { it.createdAt >= todayStart() }
    fun todayOmzet(): Int = todayOrders().sumOf { it.total }
    fun topMenu(): List<Pair<String, Int>> {
        val map = mutableMapOf<String, Int>()
        orders.value.forEach { o ->
            o.itemsLabel.lines().forEach { line ->
                val m = Regex("(\\d+)x (.+)  ").find(line)
                if (m != null) {
                    val qty = m.groupValues[1].toIntOrNull() ?: 0
                    val name = m.groupValues[2]
                    map[name] = (map[name] ?: 0) + qty
                }
            }
        }
        return map.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }
    }

    fun saveMenu(m: MenuItemEntity, isNew: Boolean) {
        viewModelScope.launch { if (isNew) db.menuDao().insert(m.copy(id = 0)) else db.menuDao().update(m) }
    }
    fun deleteMenu(id: Int) { viewModelScope.launch { db.menuDao().delete(id) } }
    fun saveStock(s: StockItemEntity, isNew: Boolean) {
        viewModelScope.launch { if (isNew) db.stockDao().insert(s.copy(id = 0)) else db.stockDao().update(s) }
    }
    fun deleteStock(id: Int) { viewModelScope.launch { db.stockDao().delete(id) } }
    fun saveStaff(s: StaffEntity, isNew: Boolean) {
        viewModelScope.launch { if (isNew) db.staffDao().insert(s.copy(id = 0)) else db.staffDao().update(s) }
    }
    fun deleteStaff(id: Int) { viewModelScope.launch { db.staffDao().delete(id) } }
}
