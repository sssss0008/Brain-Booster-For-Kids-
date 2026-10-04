package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.ProfileEntity
import com.example.model.Avatars
import com.example.model.BrainRanks

@Composable
fun KidDrawer(
    profile: ProfileEntity?,
    isDarkMode: Boolean,
    soundEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateAchievements: () -> Unit,
    onNavigateRankings: () -> Unit,
    onNavigateCertificate: () -> Unit,
    onNavigateParentDashboard: () -> Unit,
    onNavigateSettings: () -> Unit,
    onBackupData: () -> Unit,
    onRestoreData: () -> Unit,
    onNavigateAbout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val avatar = Avatars.getById(profile?.avatarId ?: "brain_hero")
    val rank = BrainRanks.all.getOrNull(profile?.currentRankIndex ?: 0) ?: BrainRanks.all[0]

    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCloseDrawer()
                        onNavigateProfile()
                    }
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(avatar.bgHex))
                    ) {
                        Text(text = avatar.emoji, fontSize = 30.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = profile?.name ?: "Brain Hero",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Age ${profile?.age ?: 7} • ${profile?.grade ?: "2nd Grade"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = rank.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = rank.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Section: Play & Progress
            Text(
                text = "YOUR JOURNEY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            DrawerItem(
                icon = Icons.Default.Person,
                title = "Child Profiles & Edit",
                tag = "drawer_profile",
                onClick = {
                    onCloseDrawer()
                    onNavigateProfile()
                }
            )

            DrawerItem(
                icon = Icons.Default.EmojiEvents,
                title = "Achievements & Badges",
                tag = "drawer_achievements",
                onClick = {
                    onCloseDrawer()
                    onNavigateAchievements()
                }
            )

            DrawerItem(
                icon = Icons.Default.Leaderboard,
                title = "Brain Ranking & Trophies",
                tag = "drawer_ranking",
                onClick = {
                    onCloseDrawer()
                    onNavigateRankings()
                }
            )

            DrawerItem(
                icon = Icons.Default.CardMembership,
                title = "Certificate of Hero",
                tag = "drawer_certificate",
                onClick = {
                    onCloseDrawer()
                    onNavigateCertificate()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Section: Controls & Parents
            Text(
                text = "CONTROLS & PARENT ZONE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            DrawerItem(
                icon = Icons.Default.Lock,
                title = "Parent Dashboard",
                tag = "drawer_parent_dashboard",
                onClick = {
                    onCloseDrawer()
                    onNavigateParentDashboard()
                }
            )

            // Sound Effects switch row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Sound Effects",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = onToggleSound,
                    modifier = Modifier.testTag("drawer_switch_sound")
                )
            }

            // Dark Mode switch row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Dark Theme",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    modifier = Modifier.testTag("drawer_switch_dark_mode")
                )
            }

            DrawerItem(
                icon = Icons.Default.Save,
                title = "Backup Progress (Export JSON)",
                tag = "drawer_backup",
                onClick = {
                    onCloseDrawer()
                    onBackupData()
                }
            )

            DrawerItem(
                icon = Icons.Default.Restore,
                title = "Restore Progress (Import JSON)",
                tag = "drawer_restore",
                onClick = {
                    onCloseDrawer()
                    onRestoreData()
                }
            )

            DrawerItem(
                icon = Icons.Default.Info,
                title = "About Developer & Mission",
                tag = "drawer_about",
                onClick = {
                    onCloseDrawer()
                    onNavigateAbout()
                }
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
