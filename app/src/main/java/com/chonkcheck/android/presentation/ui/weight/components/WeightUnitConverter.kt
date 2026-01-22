package com.chonkcheck.android.presentation.ui.weight.components

import com.chonkcheck.android.domain.model.WeightUnit
import kotlin.math.floor

object WeightUnitConverter {
    private const val LB_PER_KG = 2.20462
    private const val KG_PER_STONE = 6.35029
    private const val LB_PER_STONE = 14

    fun kgToUnit(kg: Double, unit: WeightUnit): Double {
        return when (unit) {
            WeightUnit.KG -> kg
            WeightUnit.LB -> kg * LB_PER_KG
            WeightUnit.ST -> kg / KG_PER_STONE
        }
    }

    fun unitToKg(value: Double, unit: WeightUnit): Double {
        return when (unit) {
            WeightUnit.KG -> value
            WeightUnit.LB -> value / LB_PER_KG
            WeightUnit.ST -> value * KG_PER_STONE
        }
    }

    fun formatWeight(kg: Double, unit: WeightUnit): String {
        return when (unit) {
            WeightUnit.KG -> String.format("%.1f kg", kg)
            WeightUnit.LB -> String.format("%.1f lb", kg * LB_PER_KG)
            WeightUnit.ST -> {
                val totalStones = kg / KG_PER_STONE
                val stones = floor(totalStones).toInt()
                val remainderKg = kg - (stones * KG_PER_STONE)
                val pounds = (remainderKg * LB_PER_KG).toInt()
                "$stones st $pounds lb"
            }
        }
    }

    fun formatWeightValue(kg: Double, unit: WeightUnit): String {
        return when (unit) {
            WeightUnit.KG -> String.format("%.1f", kg)
            WeightUnit.LB -> String.format("%.1f", kg * LB_PER_KG)
            WeightUnit.ST -> {
                val totalStones = kg / KG_PER_STONE
                String.format("%.1f", totalStones)
            }
        }
    }

    fun formatChange(changeKg: Double, unit: WeightUnit): String {
        val prefix = if (changeKg > 0) "+" else ""
        return when (unit) {
            WeightUnit.KG -> String.format("%s%.1f kg", prefix, changeKg)
            WeightUnit.LB -> String.format("%s%.1f lb", prefix, changeKg * LB_PER_KG)
            WeightUnit.ST -> {
                val changeSt = changeKg / KG_PER_STONE
                String.format("%s%.1f st", prefix, changeSt)
            }
        }
    }

    fun getStonesPounds(kg: Double): Pair<Int, Int> {
        val totalStones = kg / KG_PER_STONE
        val stones = floor(totalStones).toInt()
        val remainderKg = kg - (stones * KG_PER_STONE)
        val pounds = (remainderKg * LB_PER_KG).toInt()
        return Pair(stones, pounds)
    }

    fun stonePoundsToKg(stones: Int, pounds: Int): Double {
        return (stones * KG_PER_STONE) + (pounds / LB_PER_KG)
    }
}
