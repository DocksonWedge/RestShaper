package org.shaper

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.shaper.entry.*

class EndToEndSmoke {
    @TestFactory
    fun `Test E2E stest run without error`() = listOf(
        ::petStorePostPet,
        ::petStoreDeletePet,
        ::petStoreGetPet,
        ::petStoreFindByStatus,
        ::petStoreGetOrder,
        ::petStoreGetOrderFromFile,
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