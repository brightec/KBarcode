package uk.co.brightec.kbarcode.processor.sort

import android.graphics.Rect
import androidx.test.filters.MediumTest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.brightec.kbarcode.Barcode

@MediumTest
internal class CentralBarcodeComparatorTest {

    private lateinit var comparator: CentralBarcodeComparator

    @Before
    fun before() {
        comparator = CentralBarcodeComparator()
        comparator.frameMetadata = mock {
            on { width } doReturn 200
            on { height } doReturn 200
        }
    }

    @Test
    fun o1Closer__compare__positiveInteger() {
        // GIVEN
        val boundingO1 = Rect(150, 150, 150, 150)
        val o1 = mock<Barcode> {
            on { boundingBox } doReturn boundingO1
        }
        val boundingO2 = Rect(200, 200, 200, 200)
        val o2 = mock<Barcode> {
            on { boundingBox } doReturn boundingO2
        }

        // WHEN
        val result = comparator.compare(o1, o2)

        // THEN
        assertTrue(result > 0)
    }

    @Test
    fun o2Closer__compare__negativeInteger() {
        // GIVEN
        val boundingO1 = Rect(150, 150, 150, 150)
        val o1 = mock<Barcode> {
            on { boundingBox } doReturn boundingO1
        }
        val boundingO2 = Rect(110, 110, 110, 110)
        val o2 = mock<Barcode> {
            on { boundingBox } doReturn boundingO2
        }

        // WHEN
        val result = comparator.compare(o1, o2)

        // THEN
        assertTrue(result < 0)
    }

    @Test
    fun o1o2Equal__compare__0() {
        // GIVEN
        val boundingO1 = Rect(150, 150, 150, 150)
        val o1 = mock<Barcode> {
            on { boundingBox } doReturn boundingO1
        }
        val boundingO2 = Rect(150, 150, 150, 150)
        val o2 = mock<Barcode> {
            on { boundingBox } doReturn boundingO2
        }

        // WHEN
        val result = comparator.compare(o1, o2)

        // THEN
        assertEquals(0, result)
    }

    @Test
    fun o1Null__compare__positiveInteger() {
        // GIVEN
        val boundingO1 = null
        val o1 = mock<Barcode> {
            on { boundingBox } doReturn boundingO1
        }
        val boundingO2 = Rect(200, 200, 200, 200)
        val o2 = mock<Barcode> {
            on { boundingBox } doReturn boundingO2
        }

        // WHEN
        val result = comparator.compare(o1, o2)

        // THEN
        assertTrue(result > 0)
    }

    @Test
    fun o1Null__compare__negativeInteger() {
        // GIVEN
        val boundingO1 = Rect(150, 150, 150, 150)
        val o1 = mock<Barcode> {
            on { boundingBox } doReturn boundingO1
        }
        val boundingO2 = null
        val o2 = mock<Barcode> {
            on { boundingBox } doReturn boundingO2
        }

        // WHEN
        val result = comparator.compare(o1, o2)

        // THEN
        assertTrue(result < 0)
    }
}
