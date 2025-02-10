package com.lmar.scanoperation.core.util

import net.objecthunter.exp4j.ExpressionBuilder

object Expression {

    fun addAsteriskBetweenDigitsAndParentheses(expression: String): String {
        val regex1 = Regex("(\\d)\\(")   // Dígito seguido de '('
        val regex2 = Regex("\\)(\\d)")   // ')' seguido de dígito
        val regex3 = Regex("\\)\\(")     // ')' seguido de '('

        return expression.replace(regex1, "$1*(")
            .replace(regex2, ")*$1")
            .replace(regex3, ")*(")
            .replace("x", "*")
    }

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