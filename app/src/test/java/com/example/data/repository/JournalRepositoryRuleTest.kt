package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_authenticatedUser_createsProfileAndLeaderboard() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = JournalRepository(firestore, auth)

        val profile = withTimeout(TIMEOUT_MS) {
            repo.ensureUserProfile(uid, "Alice", ALICE_EMAIL, null)
        }
        assertEquals(uid, profile.userId)
        assertEquals("student", profile.role)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null }
        }
        assertEquals(uid, observed?.userId)
    }

    @Test
    fun awardXpOnce_deduplicatesRepeatedActivityKey() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = JournalRepository(firestore, auth)
        withTimeout(TIMEOUT_MS) {
            repo.ensureUserProfile(uid, "Alice", ALICE_EMAIL, null)
        }

        val key = "quiz_${UUID.randomUUID()}"
        val firstClaim = withTimeout(TIMEOUT_MS) {
            repo.awardXpOnce(key, "quiz", 50L, "হিরাগানা কুইজ")
        }
        val secondClaim = withTimeout(TIMEOUT_MS) {
            repo.awardXpOnce(key, "quiz", 50L, "হিরাগানা কুইজ")
        }

        assertTrue(firstClaim)
        assertFalse(secondClaim)
    }

    @Test
    fun observeUserProfile_crossUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = JournalRepository(firestore, auth)
        withTimeout(TIMEOUT_MS) {
            aliceRepo.ensureUserProfile(aliceUid, "Alice", ALICE_EMAIL, null)
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = JournalRepository(firestore, auth)
        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeUserProfile(aliceUid).first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        val fsError = checkNotNull(caught?.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $caught" }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError.code)
    }

    @Test
    fun observeLeaderboard_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = JournalRepository(firestore, auth)
        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeLeaderboard("anon").first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        val fsError = checkNotNull(caught?.unwrapFirestoreException()) { "Expected FirebaseFirestoreException, got $caught" }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice.learner@test.com"
        const val BOB_EMAIL = "bob.learner@test.com"
        const val TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
