package org.shaper.entry

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.global.results.ResultsStateGlobal

// Hits external endpoint!
@ResourceLock("ResultsStateGlobal")
class SimpleRunnerTest {
    @Test
    fun `Test SimpleRunner runs a simple test!`() {
        val origSize = ResultsStateGlobal.getAllResults().size
        dataAtWorkRun(2)
        Assertions.assertEquals(4, ResultsStateGlobal.getAllResults().size - origSize)
    }
}