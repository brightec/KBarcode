package uk.co.brightec.kbarcode.extension

import android.graphics.Point
import androidx.test.filters.MediumTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@MediumTest
internal class PointTest {

    private lateinit var point: Point

    @Before
    fun before() {
        point = Point()
    }

    @Test
    fun point0_pointQuadrant1__distanceTo__isCorrect() {
        // GIVEN
        point.set(0, 0)
        val other = Point(2, 2)

        // WHEN
        val result = point.distanceTo(other)

        // THEN
        assertEquals(2.82_842_712, result, 0.00_000_001)
    }

    @Test
    fun point0_pointQuadrant2__distanceTo__isCorrect() {
        // GIVEN
        point.set(0, 0)
        val other = Point(-2, 2)

        // WHEN
        val result = point.distanceTo(other)

        // THEN
        assertEquals(2.82_842_712, result, 0.00_000_001)
    }

    @Test
    fun point0_pointQuadrant3__distanceTo__isCorrect() {
        // GIVEN
        point.set(0, 0)
        val other = Point(-2, -2)

        // WHEN
        val result = point.distanceTo(other)

        // THEN
        assertEquals(2.82_842_712, result, 0.00_000_001)
    }

    @Test
    fun point0_pointQuadrant4__distanceTo__isCorrect() {
        // GIVEN
        point.set(0, 0)
        val other = Point(2, -2)

        // WHEN
        val result = point.distanceTo(other)

        // THEN
        assertEquals(2.82_842_712, result, 0.00_000_001)
    }

    @Test
    fun pointOther__distanceTo__isCorrect() {
        // GIVEN
        point.set(2, 2)
        val other = Point(20, 20)

        // WHEN
        val result = point.distanceTo(other)

        // THEN
        assertEquals(25.45_584_412, result, 0.00_000_001)
    }
}
