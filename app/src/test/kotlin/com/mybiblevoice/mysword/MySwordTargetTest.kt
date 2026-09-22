package com.mybiblevoice.mysword

import com.mybiblevoice.domain.bible.BibleBookRegistry
import com.mybiblevoice.domain.bible.BibleReference
import com.mybiblevoice.target.TargetError
import com.mybiblevoice.target.TargetResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MySwordTargetTest {

    private val reference = BibleReference(book = BibleBookRegistry.findById("john")!!, chapter = 3, startVerse = 16)

    private fun fakeLauncher(result: LaunchResult) = object : MySwordLauncher {
        override fun open(reference: BibleReference): LaunchResult = result
    }

    // Block bodies (not "= runBlocking { ... }") on purpose: if the last statement inside
    // runBlocking were an assertIs<T> call, the block's inferred type would be T (assertIs
    // returns the narrowed value), making the whole test function return non-Unit - which
    // JUnit4 rejects with "should be void".
    @Test
    fun `exact passage launch has no warning`() {
        runBlocking {
            val target = MySwordTarget(fakeLauncher(LaunchResult.Launched(exactPassage = true)))
            val result = target.open(reference)
            assertIs<TargetResult.Success>(result)
            assertNull(result.warning)
        }
    }

    @Test
    fun `generic fallback launch preserves a warning instead of being dropped`() {
        runBlocking {
            val target = MySwordTarget(fakeLauncher(LaunchResult.Launched(exactPassage = false)))
            val result = target.open(reference)
            assertIs<TargetResult.Success>(result)
            assertEquals(
                "Opened MySword, but could not navigate to the exact passage on this MySword version.",
                result.warning
            )
        }
    }

    @Test
    fun `not installed maps to NotAvailable`() {
        runBlocking {
            val target = MySwordTarget(fakeLauncher(LaunchResult.NotInstalled))
            val result = target.open(reference)
            assertIs<TargetResult.Failure>(result)
            assertIs<TargetError.NotAvailable>(result.error)
        }
    }

    @Test
    fun `failed launch maps to Unexpected with the original reason`() {
        runBlocking {
            val target = MySwordTarget(fakeLauncher(LaunchResult.Failed("Unable to launch MySword.")))
            val result = target.open(reference)
            assertIs<TargetResult.Failure>(result)
            assertEquals("Unable to launch MySword.", result.error.message)
        }
    }
}
