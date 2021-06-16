package uk.co.brightec.kbarcode.extension

import android.graphics.Point
import kotlin.math.pow
import kotlin.math.sqrt

internal fun Point.distanceTo(point: Point) = sqrt(
    (this.x - point.x).toDouble().pow(2.toDouble()) +
        (this.y - point.y).toDouble().pow(2.toDouble())
)
