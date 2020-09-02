package org.shaper.tester

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.forAll
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.int


class TestRunner: StringSpec({
    "a"{
        forAll(2, Arb.int(1..2), Arb.int(1..2)) { a, b  ->
            (a + b) == a + b
        }
    }
})
