package org.shaper.global.results

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.entry.*

class ResultsFieldsGlobalTest {
    @TestFactory
    @ResourceLock("ResultsStateGlobal")
    fun `Test results fields gets saved`() = listOf(
        ::petStorePostOrder,
    ).map { configFun ->
        DynamicTest.dynamicTest(
            "when I run '${configFun}' I find properties in the ResultsFieldsGlobal "
        ) {
            ResultsFieldsGlobal.initGlobals(reset = true)
            configFun(1)
            print(ResultsFieldsGlobal.index) //todo why array list?
            print(ResultsFieldsGlobal.multiIndex)
        }
    }

}