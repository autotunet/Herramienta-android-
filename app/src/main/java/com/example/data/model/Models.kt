package com.example.data.model

import com.google.firebase.Timestamp

data class NoteItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "General",
    val isPinned: Boolean = false,
    val colorHex: String = "#00E5FF",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class TaskItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val category: String = "Productividad",
    val dueDate: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class HabitItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val targetDaysPerWeek: Int = 7,
    val streakCount: Int = 0,
    val lastCompletedDate: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class ClipboardItem(
    val id: String = "",
    val userId: String = "",
    val snippet: String = "",
    val sourceLabel: String = "Portapapeles",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class DeviceInfo(
    val id: String = "",
    val userId: String = "",
    val deviceName: String = "",
    val model: String = "",
    val lastSyncTime: Timestamp? = null,
    val appVersion: String = "1.0 Pro",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class BackupSnapshot(
    val id: String = "",
    val userId: String = "",
    val backupName: String = "",
    val itemCount: Int = 0,
    val payloadSummary: String = "",
    val createdAt: Timestamp? = null
)
