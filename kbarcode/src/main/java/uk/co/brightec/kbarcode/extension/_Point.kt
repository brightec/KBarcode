package uk.co.brightec.kbarcode.extension

import android.graphics.Point

internal fun Point.distanceTo(point: Point) = Math.sqrt(
    Math.pow((this.x - point.x).toDouble(), 2.toDouble()) +
            Math.pow((this.y - point.y).toDouble(), 2.toDouble())
)
