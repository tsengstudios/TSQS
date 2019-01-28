package me.tseng.studios.tchores.kotlin.util

import android.content.Context
import me.tseng.studios.tchores.R
import me.tseng.studios.tchores.kotlin.model.chore
import java.util.Arrays
import java.util.Locale
import java.util.Random

/**
 * Utilities for chores.
 */
object choreUtil {

    private const val chore_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png"
    private const val MAX_IMAGE_NUM = 22

    private val NAME_FIRST_WORDS = arrayOf(
            "Foo", "Bar", "Baz", "Qux", "Fire", "Sam's", "World Famous", "Google", "The Best")

    private val NAME_SECOND_WORDS = arrayOf(
            "Chore", "Cafe", "Spot", "Eatin' Place", "Eatery", "Drive Thru", "Diner")

    /**
     * Create a random Chore POJO.
     */
    fun getRandom(context: Context): chore {
        val chore = chore()
        val random = Random()

        // Cities (first elemnt is 'Any')
        var cities = context.resources.getStringArray(R.array.cities)
        cities = Arrays.copyOfRange(cities, 1, cities.size)

        // Categories (first element is 'Any')
        var categories = context.resources.getStringArray(R.array.categories)
        categories = Arrays.copyOfRange(categories, 1, categories.size)

        val prices = intArrayOf(1, 2, 3)

        chore.name = getRandomName(random)
        chore.city = getRandomString(cities, random)
        chore.category = getRandomString(categories, random)
        chore.photo = getRandomImageUrl(random)
        chore.price = getRandomInt(prices, random)
        chore.numRatings = random.nextInt(20)

        // Note: average rating intentionally not set

        return chore
    }

    /**
     * Get a random image.
     */
    private fun getRandomImageUrl(random: Random): String {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        val id = random.nextInt(MAX_IMAGE_NUM) + 1

        return String.format(Locale.getDefault(), chore_URL_FMT, id)
    }

    /**
     * Get price represented as dollar signs.
     */
    fun getPriceString(chore: chore): String {
        return getPriceString(chore.price)
    }

    /**
     * Get price represented as dollar signs.
     */
    fun getPriceString(priceInt: Int): String {
        when (priceInt) {
            1 -> return "$"
            2 -> return "$$"
            3 -> return "$$$"
            else -> return "$$$"
        }
    }

    private fun getRandomName(random: Random): String {
        return (getRandomString(NAME_FIRST_WORDS, random) + " " +
                getRandomString(NAME_SECOND_WORDS, random))
    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }

    private fun getRandomInt(array: IntArray, random: Random): Int {
        val ind = random.nextInt(array.size)
        return array[ind]
    }
}
