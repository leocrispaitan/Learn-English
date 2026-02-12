package com.leopc.speakup.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.leopc.speakup.ui.components.BottomNavItem
import com.leopc.speakup.ui.components.SpeakUpBottomNavigation
import com.leopc.speakup.ui.home.*

@Composable
fun MainNavigation(
    navController: NavHostController,
    userName: String,
    userPhotoUrl: String?,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.Home.route,
            label = "Home",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            selectedIcon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ),
        BottomNavItem(
            route = Screen.Library.route,
            label = "Library",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = "Library",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            selectedIcon = {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = "Library",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ),
        BottomNavItem(
            route = Screen.Stats.route,
            label = "Stats",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Leaderboard,
                    contentDescription = "Stats",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            selectedIcon = {
                Icon(
                    imageVector = Icons.Filled.Leaderboard,
                    contentDescription = "Stats",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ),
        BottomNavItem(
            route = Screen.Profile.route,
            label = "Profile",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            selectedIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    )
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            SpeakUpBottomNavigation(
                items = bottomNavItems,
                selectedRoute = currentRoute,
                onItemSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    userName = userName,
                    userPhotoUrl = userPhotoUrl
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen()
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}
