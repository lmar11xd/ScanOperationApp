package com.lmar.scanoperation.core.util

import net.objecthunter.exp4j.ExpressionBuilder

object Expression {

    fun isValidExpression(expression: String): Boolean {
        return try {
            ExpressionBuilder(expression).build() // Construye la expresión
            true // Si no lanza excepción, es válida
        } catch (e: Exception) {
            false // Si hay error, la expresión es inválida
        }
    }

    fun evaluateExpression(expression: String): Double {
        return ExpressionBuilder(expression).build().evaluate()
    }
}