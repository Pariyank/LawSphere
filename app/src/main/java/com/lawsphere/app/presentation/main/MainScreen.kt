package com.lawsphere.app.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.lawsphere.app.presentation.awareness.CitizenGuideScreen
import com.lawsphere.app.presentation.awareness.MapsScreen
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.ChatScreen
import com.lawsphere.app.presentation.community.CommunityScreen
import com.lawsphere.app.presentation.community.PrivateChatScreen
import com.lawsphere.app.presentation.dashboard.CaseDashboardScreen
import com.lawsphere.app.presentation.drafting.DraftingScreen
import com.lawsphere.app.presentation.explorer.CompareScreen
import com.lawsphere.app.presentation.explorer.RoadmapScreen
import com.lawsphere.app.presentation.explorer.SectionExplorerScreen

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    object Chat : BottomNavItem("Chat", Icons.Default.Chat)
    object Explorer : BottomNavItem("Library", Icons.Default.Article)
    object Community : BottomNavItem("Forum", Icons.Default.Groups)
    object Drafting : BottomNavItem("Draft", Icons.Default.Gavel)
    object Dashboard : BottomNavItem("Cases", Icons.Default.BusinessCenter)
    object Guide : BottomNavItem("Help", Icons.Default.Info)
    object Profile : BottomNavItem("Me", Icons.Default.Person)
}

@Composable
fun MainScreen(userRole: String, onLogout: () -> Unit) {

    val tabs = remember(userRole) {
        if (userRole.equals("lawyer", ignoreCase = true)) {
            listOf(
                BottomNavItem.Chat,
                BottomNavItem.Explorer,
                BottomNavItem.Community,
                BottomNavItem.Drafting,
                BottomNavItem.Dashboard,
                BottomNavItem.Profile
            )
        } else {
            listOf(
                BottomNavItem.Chat,
                BottomNavItem.Explorer,
                BottomNavItem.Community,
                BottomNavItem.Drafting,
                BottomNavItem.Guide,
                BottomNavItem.Profile
            )
        }
    }

    var currentTab by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Chat) }

    var showMap by remember { mutableStateOf(false) }
    var showRoadmap by remember { mutableStateOf(false) }
    var showCompare by remember { mutableStateOf(false) }

    var activeChatId by remember { mutableStateOf<String?>(null) }
    var activeChatName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = AccentGold
            ) {
                tabs.forEach { item ->
                    val isSelected = currentTab == item &&
                            !showMap &&
                            !showRoadmap &&
                            !showCompare &&
                            (activeChatId == null)

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = item
                            showMap = false
                            showRoadmap = false
                            showCompare = false
                            activeChatId = null
                            activeChatName = null
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = {
                            if (isSelected) {
                                Text(
                                    text = item.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        alwaysShowLabel = false,
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

            when {
                activeChatId != null && activeChatName != null -> {
                    PrivateChatScreen(
                        otherUserId = activeChatId!!,
                        otherUserName = activeChatName!!,
                        onBack = {
                            activeChatId = null
                            activeChatName = null
                        }
                    )
                }

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
                                onOpenCompare = { showCompare = true }
                            )

                        BottomNavItem.Community ->
                            CommunityScreen(
                                userRole = userRole,
                                onChatClick = { id, name ->
                                    activeChatId = id
                                    activeChatName = name
                                }
                            )

                        BottomNavItem.Drafting ->
                            DraftingScreen()

                        BottomNavItem.Dashboard ->
                            CaseDashboardScreen()

                        BottomNavItem.Guide ->
                            CitizenGuideScreen(
                                onOpenMap = { showMap = true }
                            )

                        BottomNavItem.Profile ->
                            ProfileScreen(onLogout = onLogout)
                    }
                }
            }
        }
    }
}