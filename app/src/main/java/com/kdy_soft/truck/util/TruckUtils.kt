package com.kdy_soft.truck.util

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object TruckUtils {
    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yy.MM.dd")

    @SuppressLint("SimpleDateFormat")
    private val stf = SimpleDateFormat("HH:mm")

    object Locations {
        val SEOUL = LatLng(37.566826004661, 126.978652258309)
        val CURRENT_LOCATION = LatLng(
            36.8552891216491, 127.118187815307
        )
    }

    fun tomorrow() = Calendar.getInstance().apply {
        time = Date()
        val day = get(Calendar.DAY_OF_YEAR) + 1
        set(Calendar.DAY_OF_YEAR, day)
        set(Calendar.HOUR_OF_DAY, 18)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun distanceString(meter: Int) = if (meter > 1000) "${meter / 1000}km" else "${meter}m"
    fun getDateString(date: Date): String = sdf.format(date)
    fun getTimeString(date: Date): String = stf.format(date)
    fun priceString(price: Int): String {
        val result = StringBuilder(price.toString())
        val len = result.length

        for (i in 1 until len) {
            if (i % 3 == 0) {
                result.insert(len - i, ", ")
            }
        }
        result.insert(0, "₩ ")
        return result.toString()
    }

    private fun timeStringForDate(
        second: Int,
        forRestTime: Boolean = false
    ): String {
        val sec = second % 60
        val min = (second / 60) % 60

        var hour = (second / 3600)
        val day = hour / 24
        hour %= 24

        val week = day / 7
        val month = day / 30
        val year = month / 12

        return StringBuilder().apply {
            if (year > 0) {
                append("${year}년 ")
                if (forRestTime) return@apply
            }

            if (month > 0) {
                append("${month}개월 ")
                if (forRestTime) return@apply
            }

            if (week > 1) {
                append("${week}주 ")
                if (forRestTime) return@apply
            }

            if (day > 0) {
                append("${day}일 ")
                if (forRestTime) return@apply
            }

            if (hour > 0) {
                append("${hour}시간 ")
                if (forRestTime) return@apply
            }

            if (min > 0) {
                append("${min}분 ")
                if (forRestTime) return@apply
            }

            if (sec > 0) {
                append("${sec}초")
                if (forRestTime) return@apply
            }
        }.toString()
    }

    fun timeString(
        second: Int,
        forRestTime: Boolean = false,
        forDate: Boolean = false
    ): String {
        if (forDate) {
            return timeStringForDate(second, forRestTime)
        }
        val sec = second % 60
        val min = (second / 60) % 60
        val hour = (second / 3600)

        return StringBuilder().apply {
            if (hour > 0) {
                append("${hour}시간 ")
                if (forRestTime) return@apply
            }

            if (min > 0) {
                append("${min}분 ")
                if (forRestTime) return@apply
            }

            if (sec > 0) {
                append("${sec}초")
                if (forRestTime) return@apply
            }
        }.toString()
    }

    fun getScaledBitmap(path: String, activity: Activity): Bitmap {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        return getScaledBitmap(path, size.x, size.y)
    }

    fun getDistance(src: LatLng, dest: LatLng): Int {
        return SphericalUtil.computeDistanceBetween(src, dest).roundToInt()
    }

    private fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
        //TODO use ViewTreeObserver
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        val srcWidth = options.outWidth.toFloat()
        val srcHeight = options.outHeight.toFloat()

        var inSampleSize = 1
        if (srcHeight > destHeight || srcWidth > destWidth) {
            val heightScale = srcHeight / destHeight
            val widthScale = srcWidth / destWidth

            val sampleScale = if (heightScale > widthScale) {
                heightScale
            } else {
                widthScale
            }

            inSampleSize = sampleScale.roundToInt()
        }

        options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize

        return BitmapFactory.decodeFile(path, options)
    }

    fun getRestTimeString(time: Long): String {
        val restTime = time - System.currentTimeMillis()
        return if (restTime > 0) {
            var result = restTime / 1000
            val seconds = result % 60
            result /= 60
            val minutes = result % 60
            result /= 60
            val hours = result % 24
            result /= 24

            val days = result
            "${days}일 ${hours}:${minutes}:${seconds}"
        } else {
            "마감됨"
        }
    }

    //TODO For emulate
    fun addDataForAdmin() {

    }

    @SuppressLint("SimpleDateFormat")
    fun getRecentDate(date: Date): String {
        val current = Date()
        val calendar = Calendar.getInstance()
        calendar.time = current

        val currentYear = calendar.get(Calendar.YEAR)
        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.time = date
        val inputYear = calendar.get(Calendar.YEAR)
        val inputDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        return if (inputYear == currentYear) {
            if (currentDayOfYear == inputDayOfYear) {
                //ex) AM 11:00
                val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                    "오전"
                } else {
                    "오후"
                }
                SimpleDateFormat("$amPm hh:mm").format(date)
            } else {
                //ex ) 어제 OR 1월 29일
                if (currentDayOfYear - 1 == inputDayOfYear) {
                    "어제"
                } else {
                    SimpleDateFormat("MM월 dd일").format(date)
                }
            }
        } else {
            //ex 2018.12.22
            SimpleDateFormat("yyyy년 MM월 dd일").format(date)
        }
    }

    fun getCurrentMonth(date: Date): Calendar {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}