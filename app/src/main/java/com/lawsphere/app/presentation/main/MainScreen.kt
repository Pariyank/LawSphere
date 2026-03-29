package com.lawsphere.app.presentation.main

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.lawsphere.app.data.utils.BiometricHelper
import com.lawsphere.app.domain.model.LawyerProfile
import com.lawsphere.app.presentation.awareness.CitizenGuideScreen
import com.lawsphere.app.presentation.awareness.MapsScreen
import com.lawsphere.app.presentation.chat.AccentGold
import com.lawsphere.app.presentation.chat.ChatScreen
import com.lawsphere.app.presentation.chat.GlassDark
import com.lawsphere.app.presentation.community.CommunityScreen
import com.lawsphere.app.presentation.community.PrivateChatScreen
import com.lawsphere.app.presentation.dashboard.CaseDashboardScreen
import com.lawsphere.app.presentation.drafting.DraftingScreen
import com.lawsphere.app.presentation.explorer.CompareScreen
import com.lawsphere.app.presentation.explorer.RoadmapScreen
import com.lawsphere.app.presentation.explorer.SectionExplorerScreen
import com.lawsphere.app.presentation.components.CoolNotificationPopup

sealed class BottomNavItem(val title: String, val icon: ImageVector) {
    object Chat : BottomNavItem("Chat", Icons.Default.Chat)
    object Explorer : BottomNavItem("Library", Icons.Default.Article)
    object Community : BottomNavItem("Forum", Icons.Default.Groups)
    object Drafting : BottomNavItem("Draft", Icons.Default.Gavel)
    object Dashboard : BottomNavItem("Cases", Icons.Default.BusinessCenter)
    object Guide : BottomNavItem("Help", Icons.Default.Info)
    object Profile : BottomNavItem("Me", Icons.Default.Person)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(userRole: String, onLogout: () -> Unit) {

    val context = LocalContext.current as FragmentActivity
    val biometricHelper = remember { BiometricHelper(context) }

    val tabs = remember(userRole) {
        if (userRole.equals("lawyer", ignoreCase = true)) {
            listOf(
                BottomNavItem.Chat,
                BottomNavItem.Explorer,
                BottomNavItem.Community.apply{},
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
                contentColor = AccentGold,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { item ->
                    val isSelected = currentTab == item &&
                            !showMap && !showRoadmap && !showCompare && (activeChatId == null)

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (item == BottomNavItem.Community || item == BottomNavItem.Dashboard) {
                                if (biometricHelper.canAuthenticate()) {
                                    biometricHelper.showBiometricPrompt(
                                        onSuccess = {
                                            currentTab = item
                                            showMap = false; showRoadmap = false; showCompare = false
                                            activeChatId = null; activeChatName = null
                                        },
                                        onError = {
                                        }
                                    )
                                } else {
                                    currentTab = item
                                    showMap = false; showRoadmap = false; showCompare = false
                                    activeChatId = null; activeChatName = null
                                }
                            } else {
                                currentTab = item
                                showMap = false; showRoadmap = false; showCompare = false
                                activeChatId = null; activeChatName = null
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = {
                            if (isSelected) {
                                val labelText = if (item == BottomNavItem.Community) {
                                    if (userRole.equals("lawyer", true)) "Inbox" else "Consult"
                                } else {
                                    item.title
                                }
                                Text(text = labelText, maxLines = 1)
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

        Surface(modifier = Modifier.fillMaxSize(), color = GlassDark) {
            Box(modifier = Modifier.padding(padding)) {

                CoolNotificationPopup()

                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        val initialIdx = tabs.indexOf(initialState)
                        val targetIdx = tabs.indexOf(targetState)

                        if (targetIdx > initialIdx) {
                            (slideInHorizontally(animationSpec = tween(400, easing = FastOutSlowInEasing), initialOffsetX = { it / 10 }) + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(400, easing = FastOutSlowInEasing), targetOffsetX = { -it / 10 }) + fadeOut(tween(300)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(400, easing = FastOutSlowInEasing), initialOffsetX = { -it / 10 }) + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(400, easing = FastOutSlowInEasing), targetOffsetX = { it / 10 }) + fadeOut(tween(300)))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "MainViewTransition"
                ) { targetTab ->
                    Box(modifier = Modifier.fillMaxSize().background(GlassDark)) {
                        when {
                            activeChatId != null && activeChatName != null -> {
                                PrivateChatScreen(
                                    otherUserId = activeChatId!!,
                                    otherUserName = activeChatName!!,
                                    onBack = { activeChatId = null; activeChatName = null }
                                )
                            }
                            showCompare -> CompareScreen(onBack = { showCompare = false })
                            showMap -> MapsScreen(onBack = { showMap = false })
                            showRoadmap -> RoadmapScreen(onBack = { showRoadmap = false })

                            else -> {
                                when (targetTab) {
                                    BottomNavItem.Chat -> ChatScreen()
                                    BottomNavItem.Explorer -> SectionExplorerScreen(
                                        onOpenRoadmap = { showRoadmap = true },
                                        onOpenCompare = { showCompare = true }
                                    )
                                    BottomNavItem.Community -> CommunityScreen(
                                        userRole = userRole,
                                        onChatClick = { id, name -> activeChatId = id; activeChatName = name }
                                    )
                                    BottomNavItem.Drafting -> DraftingScreen()
                                    BottomNavItem.Dashboard -> CaseDashboardScreen()
                                    BottomNavItem.Guide -> CitizenGuideScreen(onOpenMap = { showMap = true })
                                    BottomNavItem.Profile -> ProfileScreen(onLogout = onLogout)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}