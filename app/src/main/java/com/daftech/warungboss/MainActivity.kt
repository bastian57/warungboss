package com.daftech.warungboss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daftech.warungboss.license.LicenseManager
import com.daftech.warungboss.theme.WarungBossTheme
import com.daftech.warungboss.ui.ActivationScreen
import com.daftech.warungboss.ui.CashierScreen
import com.daftech.warungboss.ui.CheckoutScreen
import com.daftech.warungboss.ui.DashboardScreen
import com.daftech.warungboss.ui.LoginScreen
import com.daftech.warungboss.ui.MainViewModel
import com.daftech.warungboss.ui.MenuManageScreen
import com.daftech.warungboss.ui.OrdersScreen
import com.daftech.warungboss.ui.ReceiptScreen
import com.daftech.warungboss.ui.ReportScreen
import com.daftech.warungboss.ui.SettingsScreen
import com.daftech.warungboss.ui.SplashScreen
import com.daftech.warungboss.ui.StockScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MainViewModel = viewModel()
            val dark by vm.dark.collectAsState()
            var stage by remember { mutableStateOf("splash") }
            val prefs = LicenseManager.prefs(this)
            val activated = LicenseManager.isActivated(prefs)
            if (stage == "splash") {
                WarungBossTheme(darkTheme = dark) {
                    SplashScreen { stage = if (activated) "login" else "activation" }
                }
                return@setContent
            }
            WarungBossTheme(darkTheme = dark) {
                when (stage) {
                    "activation" -> ActivationScreen(deviceId = LicenseManager.getDeviceId(this)) { stage = "login" }
                    "login" -> LoginScreen { role ->
                        vm.setRole(role)
                        stage = "main"
                    }
                    else -> MainShell(vm = vm, onLogout = { stage = "login" })
                }
            }
        }
    }
}

data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MainShell(vm: MainViewModel, onLogout: () -> Unit) {
    val nav = rememberNavController()
    var current by remember { mutableStateOf("beranda") }
    val dark by vm.dark.collectAsState()
    val tabs = listOf(
        Tab("beranda", "Beranda", Icons.Filled.Home),
        Tab("kasir", "Kasir", Icons.Filled.ShoppingCart),
        Tab("pesanan", "Pesanan", Icons.Filled.List),
        Tab("stok", "Stok", Icons.Filled.Star),
        Tab("laporan", "Laporan", Icons.Filled.Star)
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = current == t.route,
                        onClick = { current = t.route; nav.navigate(t.route) { popUpTo("beranda"); launchSingleTop = true } },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad)) {
            NavHost(navController = nav, startDestination = "beranda") {
                composable("beranda") {
                    DashboardScreen(vm) { route ->
                        when (route) {
                            "menu" -> { current = "menu"; nav.navigate("menu") }
                            "pengaturan" -> { current = "pengaturan"; nav.navigate("pengaturan") }
                            "struk" -> { current = "struk"; nav.navigate("struk") }
                            else -> { current = route; nav.navigate(route) }
                        }
                    }
                }
                composable("kasir") {
                    CashierScreen(vm, onCheckout = { nav.navigate("checkout") }, onReceipt = { nav.navigate("struk") })
                }
                composable("checkout") {
                    CheckoutScreen(vm, onDone = { nav.navigate("struk") { popUpTo("kasir") } }, onCancel = { nav.popBackStack() })
                }
                composable("pesanan") { OrdersScreen(vm) }
                composable("stok") { StockScreen(vm) }
                composable("laporan") { ReportScreen(vm) }
                composable("menu") { MenuManageScreen(vm) }
                composable("pengaturan") { SettingsScreen(vm, dark, { vm.setDark(it) }, onLogout) }
                composable("struk") { ReceiptScreen(vm) { } }
            }
        }
    }
}
