package com.example.calculatorc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    var expression = MutableLiveData<String>()
    var sortedExpression = MutableLiveData<MutableList<Any>>()
    var expressionToCalculate = MutableLiveData<MutableList<Any>>()

    fun setExpression(expression: String) {
        this.expression.value = expression
    }

    fun setSortedExpression(list: MutableList<Any>) {
        sortedExpression.value = list
    }

    fun setExpToCalculate(exp: MutableList<Any>) {
        expressionToCalculate.value = exp
    }
}