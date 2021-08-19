package org.shaper.entry

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.entry.docker.main
import org.shaper.generators.model.StaticParams
import org.shaper.global.results.Results
import org.shaper.global.results.ResultsFieldsGlobal
import org.shaper.global.results.ResultsStateGlobal


class DockerEntryTest {
    @Test
    @ResourceLock("ResultsStateGlobal")
    fun `Test general run works with config json`() {
        main("src/test/Resources/TestConfig/PetTest.json")
        main("src/test/Resources/TestConfig/PetTestDefault.json") //TODO add in auth headers here to
    }

    @ResourceLock("ResultsStateGlobal")
    @Test
    fun `Test that the docker runner can handle static headers!`() {
        ResultsStateGlobal.clearResults()
        val origSize = ResultsStateGlobal.getAllResults().size
        main("src/test/Resources/TestConfig/PetHeaderTest.json")
        Assertions.assertEquals(1, ResultsStateGlobal.getAllResults().size - origSize)
        Assertions.assertEquals(
            "token",
            ResultsStateGlobal.getAllResults()[0].input.headers["api_key"] as String
        )
    }

}