package com.daftech.warungboss.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Int,
    val stock: Int = 99,
    val visible: Boolean = true,
    val emoji: String = "🍲"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNo: String,
    val type: String,
    val tableLabel: String = "",
    val itemsJson: String,
    val itemsLabel: String = "",
    val total: Int,
    val payment: String,
    val customerName: String = "",
    val customerPhone: String = "",
    val status: String = "BARU",
    val note: String = "",
    val paidAmount: Int = 0,
    val changeAmount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_items")
data class StockItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val unit: String = "porsi",
    val quantity: Int = 0,
    val minQty: Int = 10
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String = "KASIR",
    val pin: String = "1234",
    val active: Boolean = true
)

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_items ORDER BY category, name") fun all(): Flow<List<MenuItemEntity>>
    @Query("SELECT * FROM menu_items WHERE visible=1 ORDER BY category, name") fun visible(): Flow<List<MenuItemEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: MenuItemEntity)
    @Update suspend fun update(item: MenuItemEntity)
    @Query("DELETE FROM menu_items WHERE id=:id") suspend fun delete(id: Int)
    @Query("SELECT COUNT(*) FROM menu_items") suspend fun count(): Int
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC") fun all(): Flow<List<OrderEntity>>
    @Query("SELECT * FROM orders WHERE createdAt>=:since ORDER BY createdAt DESC") fun since(since: Long): Flow<List<OrderEntity>>
    @Insert suspend fun insert(o: OrderEntity): Long
    @Update suspend fun update(o: OrderEntity)
    @Query("SELECT COUNT(*) FROM orders") suspend fun count(): Int
}

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_items ORDER BY name") fun all(): Flow<List<StockItemEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: StockItemEntity)
    @Update suspend fun update(s: StockItemEntity)
    @Query("DELETE FROM stock_items WHERE id=:id") suspend fun delete(id: Int)
    @Query("SELECT COUNT(*) FROM stock_items") suspend fun count(): Int
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff ORDER BY name") fun all(): Flow<List<StaffEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: StaffEntity)
    @Update suspend fun update(s: StaffEntity)
    @Query("DELETE FROM staff WHERE id=:id") suspend fun delete(id: Int)
    @Query("SELECT COUNT(*) FROM staff") suspend fun count(): Int
}

@Database(entities = [MenuItemEntity::class, OrderEntity::class, StockItemEntity::class, StaffEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun orderDao(): OrderDao
    abstract fun stockDao(): StockDao
    abstract fun staffDao(): StaffDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(ctx: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "warungboss.db")
                    .fallbackToDestructiveMigration().build()
                INSTANCE = inst; inst
            }
        }
    }
}

object DefaultData {
    fun menu(): List<MenuItemEntity> = listOf(
        MenuItemEntity(name = "Bakso Urat", category = "Bakso", price = 15000, stock = 120, emoji = "🍲"),
        MenuItemEntity(name = "Bakso Telur", category = "Bakso", price = 17000, stock = 60, emoji = "🥚"),
        MenuItemEntity(name = "Bakso Mercon", category = "Bakso", price = 18000, stock = 40, emoji = "🌶️"),
        MenuItemEntity(name = "Bakso Jumbo", category = "Bakso", price = 20000, stock = 30, emoji = "🍲"),
        MenuItemEntity(name = "Bakso Campur", category = "Bakso", price = 19000, stock = 50, emoji = "🍲"),
        MenuItemEntity(name = "Mie Ayam Original", category = "Mie Ayam", price = 13000, stock = 40, emoji = "🍜"),
        MenuItemEntity(name = "Mie Ayam +Bakso", category = "Mie Ayam", price = 18000, stock = 35, emoji = "🍜"),
        MenuItemEntity(name = "Mie Ayam +Pangsit", category = "Mie Ayam", price = 17000, stock = 30, emoji = "🍜"),
        MenuItemEntity(name = "Mie Ayam +Ceker", category = "Mie Ayam", price = 17000, stock = 25, emoji = "🍜"),
        MenuItemEntity(name = "Mie Ayam Jumbo", category = "Mie Ayam", price = 17000, stock = 20, emoji = "🍜"),
        MenuItemEntity(name = "Es Teh", category = "Minuman", price = 5000, stock = 80, emoji = "🧋"),
        MenuItemEntity(name = "Es Jeruk", category = "Minuman", price = 7000, stock = 60, emoji = "🍊"),
        MenuItemEntity(name = "Kerupuk", category = "Tambahan", price = 2000, stock = 100, emoji = "🍘"),
        MenuItemEntity(name = "Pangsit Goreng", category = "Tambahan", price = 5000, stock = 45, emoji = "🥟")
    )
    fun stock(): List<StockItemEntity> = listOf(
        StockItemEntity(name = "Bakso (butir)", unit = "butir", quantity = 240, minQty = 50),
        StockItemEntity(name = "Mie (porsi)", unit = "porsi", quantity = 40, minQty = 10),
        StockItemEntity(name = "Ayam suwir (porsi)", unit = "porsi", quantity = 35, minQty = 10),
        StockItemEntity(name = "Pangsit", unit = "pcs", quantity = 60, minQty = 15),
        StockItemEntity(name = "Ceker", unit = "pcs", quantity = 30, minQty = 10),
        StockItemEntity(name = "Kuah (porsi)", unit = "porsi", quantity = 80, minQty = 20)
    )
    fun staff(): List<StaffEntity> = listOf(
        StaffEntity(name = "Owner", role = "OWNER", pin = "1234"),
        StaffEntity(name = "Kasir 1", role = "KASIR", pin = "1111")
    )
}
