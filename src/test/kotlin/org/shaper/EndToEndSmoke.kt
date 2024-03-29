package org.shaper

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.ResourceLock
import org.shaper.entry.*

class EndToEndSmoke {
    @TestFactory
    @ResourceLock("ResultsStateGlobal")
    fun `Test E2E test run without error`() = listOf(
        ::petStorePostPet,
        ::petStoreDeletePet,
        ::petStoreGetPet,
//        ::petStoreFindByStatus,
        ::petStoreGetOrder,
        ::petStoreGetOrderFromFile,
        ::petStorePostOrder,
        ::petStoreCreateWithArray
    ).map { configFun ->
        DynamicTest.dynamicTest(
            "when I run '${configFun}' I get no error "
        ) {
            Assertions.assertDoesNotThrow {
                configFun(1)
            }

        }
    }
}