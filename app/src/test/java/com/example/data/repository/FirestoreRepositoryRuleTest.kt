package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun unauthenticated_writeNote_throwsException() = runBlocking {
        auth.signOut()
        val repository = FirestoreRepository(firestore)
        var failed = false
        try {
            repository.saveNote(NoteItem(title = "Nota Sin Auth", content = "Test"))
        } catch (e: Exception) {
            failed = true
        }
        assertTrue("Write by unauthenticated caller must fail", failed)
    }

    @Test
    fun authenticatedUser_canSaveAndObserveNote() = runBlocking {
        val uid = signInTestUser("alice_rule@example.com")
        val repository = FirestoreRepository(firestore)

        val note = NoteItem(
            id = "note_test_1",
            userId = uid,
            title = "Prueba de Seguridad",
            content = "Contenido protegido",
            category = "Seguridad"
        )
        repository.saveNote(note)

        val notes = withTimeout(5000L) {
            repository.observeNotes().first { list -> list.any { it.id == "note_test_1" } }
        }
        val found = notes.firstOrNull { it.id == "note_test_1" }
        assertNotNull(found)
        assertEquals("Prueba de Seguridad", found?.title)
    }

    @Test
    fun authenticatedUser_canSaveAndObserveTask() = runBlocking {
        val uid = signInTestUser("bob_rule@example.com")
        val repository = FirestoreRepository(firestore)

        val task = TaskItem(
            id = "task_test_1",
            userId = uid,
            title = "Optimizar batería",
            isCompleted = false,
            priority = "HIGH"
        )
        repository.saveTask(task)

        val tasks = withTimeout(5000L) {
            repository.observeTasks().first { list -> list.any { it.id == "task_test_1" } }
        }
        val found = tasks.firstOrNull { it.id == "task_test_1" }
        assertNotNull(found)
        assertEquals("Optimizar batería", found?.title)
        assertEquals(false, found?.isCompleted)
    }
}
