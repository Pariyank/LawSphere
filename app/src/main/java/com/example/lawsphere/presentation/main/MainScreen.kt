package com.example.lawsphere.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lawsphere.presentation.awareness.CitizenGuideScreen
import com.example.lawsphere.presentation.awareness.MapsScreen
import com.example.lawsphere.presentation.chat.AccentGold
import com.example.lawsphere.presentation.chat.ChatScreen
import com.example.lawsphere.presentation.dashboard.CaseDashboardScreen
import com.example.lawsphere.presentation.drafting.DraftingScreen
import com.example.lawsphere.presentation.explorer.RoadmapScreen
import com.example.lawsphere.presentation.explorer.SectionExplorerScreen
import com.example.lawsphere.presentation.explorer.CompareScreen // ✅ Import Compare

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    object Chat : BottomNavItem("Chat", Icons.Default.Chat)
    object Explorer : BottomNavItem("BNS", Icons.Default.Article)
    object Drafting : BottomNavItem("Draft", Icons.Default.Gavel)
    object Dashboard : BottomNavItem("Cases", Icons.Default.BusinessCenter)
    object Guide : BottomNavItem("Help", Icons.Default.Info)

    object Profile : BottomNavItem("Profile", Icons.Default.Person)
}

@Composable
fun MainScreen(userRole: String, onLogout: () -> Unit) {

    val tabs = remember(userRole) {
        if (userRole == "lawyer") {
            listOf(
                BottomNavItem.Chat,
                BottomNavItem.Explorer,
                BottomNavItem.Drafting,
                BottomNavItem.Dashboard,
                BottomNavItem.Profile
            )
        } else {
            listOf(
                BottomNavItem.Chat,
                BottomNavItem.Explorer,
                BottomNavItem.Drafting,
                BottomNavItem.Guide,
                BottomNavItem.Profile
            )
        }
    }

    var currentTab by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Chat) }

    // 🟢 Sub-screen states
    var showMap by remember { mutableStateOf(false) }
    var showRoadmap by remember { mutableStateOf(false) }
    var showCompare by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = AccentGold
            ) {
                tabs.forEach { item ->
                    NavigationBarItem(
                        selected = currentTab == item &&
                                !showMap &&
                                !showRoadmap &&
                                !showCompare,
                        onClick = {
                            currentTab = item
                            showMap = false
                            showRoadmap = false
                            showCompare = false
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = AccentGold,
                            indicatorColor = AccentGold,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {

            // 🟢 Navigation Priority Order
            when {
                showCompare -> {
                    CompareScreen(
                        onBack = { showCompare = false }
                    )
                }

                showMap -> {
                    MapsScreen(
                        onBack = { showMap = false }
                    )
                }

                showRoadmap -> {
                    RoadmapScreen(
                        onBack = { showRoadmap = false }
                    )
                }

                else -> {
                    when (currentTab) {

                        BottomNavItem.Chat ->
                            ChatScreen(onLogout = onLogout)

                        BottomNavItem.Explorer ->
                            SectionExplorerScreen(
                                onOpenRoadmap = { showRoadmap = true },
                                onOpenCompare = { showCompare = true } // ✅ New callback
                            )

                        BottomNavItem.Drafting ->
                            DraftingScreen()

                        BottomNavItem.Dashboard ->
                            CaseDashboardScreen()

                        BottomNavItem.Guide ->
                            CitizenGuideScreen(
                                onOpenMap = { showMap = true }
                            )

                        BottomNavItem.Profile -> ProfileScreen(onLogout = onLogout)
                    }
                }
            }
        }
    }
}
