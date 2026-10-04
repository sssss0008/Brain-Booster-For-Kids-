package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.model.ProfileEntity
import com.example.model.Avatars
import com.example.model.BrainRanks
import com.example.model.ChildAvatar

@Composable
fun ProfileManagerDialog(
    currentProfile: ProfileEntity?,
    allProfiles: List<ProfileEntity>,
    onSwitchProfile: (Long) -> Unit,
    onCreateProfile: (name: String, nickname: String, age: Int, grade: String, avatarId: String) -> Unit,
    onUpdateProfile: (name: String, nickname: String, age: Int, grade: String, avatarId: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(false) }
    var isEditingCurrent by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCreatingNew) "Add New Profile" else if (isEditingCurrent) "Edit Profile" else "Child Profiles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isCreatingNew || isEditingCurrent) {
                    val initialProfile = if (isEditingCurrent) currentProfile else null
                    ProfileForm(
                        initialName = initialProfile?.name ?: "",
                        initialNickname = initialProfile?.nickname ?: "",
                        initialAge = initialProfile?.age?.toFloat() ?: 7f,
                        initialGrade = initialProfile?.grade ?: "2nd Grade",
                        initialAvatarId = initialProfile?.avatarId ?: "brain_hero",
                        submitLabel = if (isEditingCurrent) "Save Changes" else "Create Profile",
                        onCancel = {
                            isCreatingNew = false
                            isEditingCurrent = false
                        },
                        onSubmit = { name, nickname, age, grade, avatarId ->
                            if (isEditingCurrent) {
                                onUpdateProfile(name, nickname, age, grade, avatarId)
                                isEditingCurrent = false
                            } else {
                                onCreateProfile(name, nickname, age, grade, avatarId)
                                isCreatingNew = false
                            }
                        }
                    )
                } else {
                    // Profile List
                    Text(
                        text = "Select active player or create a new profile for siblings:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    allProfiles.forEach { p ->
                        val isSelected = p.id == currentProfile?.id
                        val avatar = Avatars.getById(p.avatarId)
                        val rank = BrainRanks.all.getOrNull(p.currentRankIndex) ?: BrainRanks.all[0]

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSwitchProfile(p.id) }
                                .testTag("profile_item_${p.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(avatar.bgHex))
                                ) {
                                    Text(text = avatar.emoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Age ${p.age} • ${p.grade} • ${rank.icon} ${rank.title}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    IconButton(onClick = { isEditingCurrent = true }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { isCreatingNew = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("add_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Add Another Child Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileForm(
    initialName: String,
    initialNickname: String,
    initialAge: Float,
    initialGrade: String,
    initialAvatarId: String,
    submitLabel: String,
    onCancel: () -> Unit,
    onSubmit: (name: String, nickname: String, age: Int, grade: String, avatarId: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var nickname by remember { mutableStateOf(initialNickname) }
    var age by remember { mutableFloatStateOf(initialAge) }
    var grade by remember { mutableStateOf(initialGrade) }
    var selectedAvatar by remember { mutableStateOf(Avatars.getById(initialAvatarId)) }
    var error by remember { mutableStateOf(false) }

    val grades = listOf("Preschool", "Kindergarten", "1st Grade", "2nd Grade", "3rd Grade", "4th Grade", "5th Grade", "6th Grade")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Choose Avatar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Avatars.list) { av ->
                val isSel = av.id == selectedAvatar.id
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(av.bgHex))
                        .border(
                            width = if (isSel) 3.dp else 1.dp,
                            color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedAvatar = av }
                ) {
                    Text(text = av.emoji, fontSize = 26.sp)
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                error = false
            },
            label = { Text("Child's First Name *") },
            singleLine = true,
            isError = error,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Hero Nickname (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Age: ${age.toInt()} years", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Slider(
            value = age,
            onValueChange = { age = it },
            valueRange = 4f..12f,
            steps = 7,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Grade / Level:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(grades) { g ->
                val isSel = grade == g
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { grade = g }
                ) {
                    Text(
                        text = g,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = true
                    } else {
                        onSubmit(name.trim(), nickname.trim(), age.toInt(), grade, selectedAvatar.id)
                    }
                },
                modifier = Modifier.weight(1.5f)
            ) {
                Text(submitLabel)
            }
        }
    }
}
