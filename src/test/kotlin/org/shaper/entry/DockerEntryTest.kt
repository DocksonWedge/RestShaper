package org.shaper.entry

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.entry.docker.main


class DockerEntryTest {
    @Test
    @ResourceLock("ResultsStateGlobal")
    fun `Test general run works with config json`() {
        main("src/test/Resources/TestConfig/PetTest.json")
        main("src/test/Resources/TestConfig/PetTestDefault.json")
    }

}