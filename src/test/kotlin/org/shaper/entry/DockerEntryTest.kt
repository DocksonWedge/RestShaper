package org.shaper.entry

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.entry.docker.main
import org.shaper.global.results.Results
import org.shaper.global.results.ResultsFieldsGlobal
import org.shaper.global.results.ResultsStateGlobal


class DockerEntryTest {
    @Test
    @ResourceLock("ResultsStateGlobal")
    fun `Test general run works with config json`() {
        main("src/test/Resources/TestConfig/PetTest.json")
        println(ResultsFieldsGlobal.index)
        //main("src/test/Resources/TestConfig/PetTestDefault.json")
    }

}