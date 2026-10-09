package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.BackupSnapshot
import com.example.data.model.ClipboardItem
import com.example.data.model.DeviceInfo
import com.example.data.model.HabitItem
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class OperationType {
    CREATE, READ, UPDATE, DELETE, LIST
}

fun handleFirestoreError(error: Throwable, operation: OperationType, path: String) {
    Log.e("FirestoreError", "Operation $operation on path $path failed: ${error.message}", error)
}

class FirestoreRepository(private val db: FirebaseFirestore) {

    companion object {
        fun create(context: Context): FirestoreRepository {
            val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return FirestoreRepository(firestore)
        }
    }

    private val auth = Firebase.auth

    fun currentUserId(): String? = auth.currentUser?.uid

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado en la nube")
    }

    // --- NOTES ---
    fun observeNotes(): Flow<List<NoteItem>> = flow {
        val uid = requireUserId()
        val path = "notes"
        val query = db.collection("notes").whereEqualTo("userId", uid)
        query.snapshots()
            .map { snap -> snap.toObjects(NoteItem::class.java).sortedByDescending { it.isPinned } }
            .catch { e ->
                handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun saveNote(note: NoteItem) {
        val uid = requireUserId()
        val noteId = if (note.id.isNotBlank()) note.id else UUID.randomUUID().toString()
        val docRef = db.collection("notes").document(noteId)
        val data = mapOf(
            "id" to noteId,
            "userId" to uid,
            "title" to note.title,
            "content" to note.content,
            "category" to note.category,
            "isPinned" to note.isPinned,
            "colorHex" to note.colorHex,
            "createdAt" to (note.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "notes/$noteId")
            throw e
        }
    }

    suspend fun deleteNote(noteId: String) {
        requireUserId()
        try {
            db.collection("notes").document(noteId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "notes/$noteId")
            throw e
        }
    }

    // --- TASKS ---
    fun observeTasks(): Flow<List<TaskItem>> = flow {
        val uid = requireUserId()
        val path = "tasks"
        val query = db.collection("tasks").whereEqualTo("userId", uid)
        query.snapshots()
            .map { snap -> snap.toObjects(TaskItem::class.java).sortedBy { it.isCompleted } }
            .catch { e ->
                handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun saveTask(task: TaskItem) {
        val uid = requireUserId()
        val taskId = if (task.id.isNotBlank()) task.id else UUID.randomUUID().toString()
        val docRef = db.collection("tasks").document(taskId)
        val data = mapOf(
            "id" to taskId,
            "userId" to uid,
            "title" to task.title,
            "description" to task.description,
            "isCompleted" to task.isCompleted,
            "priority" to task.priority,
            "category" to task.category,
            "dueDate" to task.dueDate,
            "createdAt" to (task.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "tasks/$taskId")
            throw e
        }
    }

    suspend fun toggleTask(task: TaskItem) {
        val uid = requireUserId()
        val docRef = db.collection("tasks").document(task.id)
        val data = mapOf(
            "id" to task.id,
            "userId" to uid,
            "title" to task.title,
            "description" to task.description,
            "isCompleted" to !task.isCompleted,
            "priority" to task.priority,
            "category" to task.category,
            "dueDate" to task.dueDate,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "tasks/${task.id}")
            throw e
        }
    }

    suspend fun deleteTask(taskId: String) {
        requireUserId()
        try {
            db.collection("tasks").document(taskId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "tasks/$taskId")
            throw e
        }
    }

    // --- HABITS ---
    fun observeHabits(): Flow<List<HabitItem>> = flow {
        val uid = requireUserId()
        val path = "habits"
        val query = db.collection("habits").whereEqualTo("userId", uid)
        query.snapshots()
            .map { snap -> snap.toObjects(HabitItem::class.java) }
            .catch { e ->
                handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun saveHabit(habit: HabitItem) {
        val uid = requireUserId()
        val habitId = if (habit.id.isNotBlank()) habit.id else UUID.randomUUID().toString()
        val docRef = db.collection("habits").document(habitId)
        val data = mapOf(
            "id" to habitId,
            "userId" to uid,
            "name" to habit.name,
            "targetDaysPerWeek" to habit.targetDaysPerWeek,
            "streakCount" to habit.streakCount,
            "lastCompletedDate" to habit.lastCompletedDate,
            "createdAt" to (habit.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "habits/$habitId")
            throw e
        }
    }

    suspend fun completeHabitToday(habit: HabitItem, todayDate: String) {
        val uid = requireUserId()
        val docRef = db.collection("habits").document(habit.id)
        val newStreak = habit.streakCount + 1
        val data = mapOf(
            "id" to habit.id,
            "userId" to uid,
            "name" to habit.name,
            "streakCount" to newStreak,
            "lastCompletedDate" to todayDate,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "habits/${habit.id}")
            throw e
        }
    }

    // --- CLIPBOARD ---
    fun observeClipboard(): Flow<List<ClipboardItem>> = flow {
        val uid = requireUserId()
        val path = "clipboard"
        val query = db.collection("clipboard").whereEqualTo("userId", uid)
        query.snapshots()
            .map { snap -> snap.toObjects(ClipboardItem::class.java) }
            .catch { e ->
                handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun saveClipboard(snippet: String, source: String = "App") {
        val uid = requireUserId()
        val clipId = UUID.randomUUID().toString()
        val docRef = db.collection("clipboard").document(clipId)
        val data = mapOf(
            "id" to clipId,
            "userId" to uid,
            "snippet" to snippet,
            "sourceLabel" to source,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "clipboard/$clipId")
            throw e
        }
    }

    suspend fun deleteClipboard(clipId: String) {
        requireUserId()
        try {
            db.collection("clipboard").document(clipId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "clipboard/$clipId")
            throw e
        }
    }

    // --- CONNECTED DEVICES & SYNC ---
    fun observeDevices(): Flow<List<DeviceInfo>> = flow {
        val uid = requireUserId()
        val path = "devices"
        val query = db.collection("devices").whereEqualTo("userId", uid)
        query.snapshots()
            .map { snap -> snap.toObjects(DeviceInfo::class.java) }
            .catch { e ->
                handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun registerCurrentDevice(deviceId: String, deviceName: String, model: String) {
        val uid = requireUserId()
        val docRef = db.collection("devices").document(deviceId)
        val data = mapOf(
            "id" to deviceId,
            "userId" to uid,
            "deviceName" to deviceName,
            "model" to model,
            "lastSyncTime" to FieldValue.serverTimestamp(),
            "appVersion" to "AndroSuite Pro 1.0",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "devices/$deviceId")
        }
    }

    suspend fun pingDeviceSync(deviceId: String) {
        val uid = requireUserId()
        val docRef = db.collection("devices").document(deviceId)
        try {
            docRef.update("lastSyncTime", FieldValue.serverTimestamp(), "updatedAt", FieldValue.serverTimestamp()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "devices/$deviceId")
        }
    }

    // --- BACKUPS & SNAPSHOTS ---
    fun observeBackups(): Flow<List<BackupSnapshot>> = flow {
        val uid = requireUserId()
        val path = "backups"
        val query = db.collection("backups").whereEqualTo("userId", uid)
        query.snapshots()
            .map { snap -> snap.toObjects(BackupSnapshot::class.java) }
            .catch { e ->
                handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
            .collect { emit(it) }
    }

    suspend fun createCloudBackup(name: String, notesCount: Int, tasksCount: Int, habitsCount: Int): BackupSnapshot {
        val uid = requireUserId()
        val backupId = UUID.randomUUID().toString()
        val totalItems = notesCount + tasksCount + habitsCount
        val summary = "$notesCount notas, $tasksCount tareas, $habitsCount hábitos sincronizados"
        val docRef = db.collection("backups").document(backupId)
        val data = mapOf(
            "id" to backupId,
            "userId" to uid,
            "backupName" to name,
            "itemCount" to totalItems,
            "payloadSummary" to summary,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            return BackupSnapshot(
                id = backupId,
                userId = uid,
                backupName = name,
                itemCount = totalItems,
                payloadSummary = summary
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "backups/$backupId")
            throw e
        }
    }
}
