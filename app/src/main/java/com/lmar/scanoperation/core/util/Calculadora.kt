package com.lmar.scanoperation.core.util

import java.util.Stack



object Calculadora {

    fun evaluarExpresion(expresion: String): Int {
        val numeros = Stack<Int>()
        val operadores = Stack<Char>()
        var numero = 0
        var enNumero = false

        for (element in expresion) {
            val c: Char = element

            if (Character.isDigit(c)) {
                // Si el carácter es un número, lo añadimos al número actual
                numero = numero * 10 + (c - '0')
                enNumero = true
            } else {
                if (enNumero) {
                    // Cuando encontramos un operador después de un número, lo guardamos
                    numeros.push(numero)
                    numero = 0
                    enNumero = false
                }

                // Si encontramos un operador (+, -, *, /), lo agregamos a la pila
                when (c) {
                    '+', '-', '*', '/' -> {
                        operadores.push(c)
                    }
                    '(' -> {
                        // Si encontramos un paréntesis de apertura, lo agregamos a la pila
                        operadores.push(c)
                    }
                    ')' -> {
                        // Si encontramos un paréntesis de cierre, resolvemos la operación dentro
                        while (operadores.peek() != '(') {
                            resolverOperacion(numeros, operadores)
                        }
                        operadores.pop() // Descartamos el paréntesis de apertura
                    }
                }
            }
        }

        // Si hemos terminado de recorrer la expresión, procesamos el último número
        if (enNumero) {
            numeros.push(numero)
        }

        // Resolvemos las operaciones restantes en la pila
        while (!operadores.isEmpty()) {
            resolverOperacion(numeros, operadores)
        }

        // El resultado final estará en la pila de números
        return numeros.pop()
    }

    private fun resolverOperacion(numeros: Stack<Int>, operadores: Stack<Char>) {
        val b = numeros.pop()
        val a = numeros.pop()
        val operador = operadores.pop()

        var resultado = 0
        when (operador) {
            '+' -> resultado = a + b
            '-' -> resultado = a - b
            '*' -> resultado = a * b
            '/' -> resultado = a / b
        }

        // Guardamos el resultado en la pila
        numeros.push(resultado)
    }

}