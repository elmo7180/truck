package com.kdy_soft.truck

import com.google.android.gms.maps.model.LatLng
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testLatLng() {
        println("${LatLng(0.0, 0.0)}")
    }
}