package weberstudio.app.billigsteprodukter

import com.google.mlkit.vision.text.Text
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ParsedImageText

/**
 * Tests the shared [StoreParser.parseQuantity] helper that replaced the old
 * `line.text[0]` quantity math (which divided by the first *character*, breaking
 * multi-digit quantities like "12 x 4,00").
 */
class ParseQuantityTest {
    //Minimal StoreParser to reach the default helper; parse() is never called
    private val parser = object : StoreParser {
        override fun parse(receipt: Text): ParsedImageText = throw UnsupportedOperationException()
        override fun toString(): String = "TestParser"
    }

    @Test
    fun multiDigitQuantity_readsWholeNumber() {
        assertEquals(12f, parser.parseQuantity("12 x 4,00")!!, 0.0001f)
    }

    @Test
    fun singleDigitQuantity_isParsed() {
        assertEquals(2f, parser.parseQuantity("2 x 25,50")!!, 0.0001f)
    }

    @Test
    fun commaDecimalQuantity_isParsed() {
        assertEquals(1.5f, parser.parseQuantity("1,5 x 10,00")!!, 0.0001f)
    }

    @Test
    fun noNumber_returnsNull() {
        assertNull(parser.parseQuantity("ingen tal her"))
    }
}
