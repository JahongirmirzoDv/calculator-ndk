package com.example.calculatorc

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.calculatorc.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import soup.neumorphism.LightSource
import soup.neumorphism.NeumorphButton
import soup.neumorphism.NeumorphImageButton

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    private val TAG = "MainActivity"
    var ison = false

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(this.window, false)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("data")) {
                binding.switch1.isOn = true
            }
        }

        binding.switch1.setOnToggledListener { toggleableView, isOn ->
            if (isOn) {
                ison = true
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            } else {
                ison = false
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }


        val btns = binding.gridLayout.children
        for (i in btns) {
            i.setOnClickListener(this)
        }

        binding.expression.addTextChangedListener {
            val text = it.toString().trim()
            viewModel.setExpression(text)
        }
        binding.expression.setText("0")

        viewModel.expression.observe(this) {
            val sorted = digitsOperators()
            viewModel.setSortedExpression(sorted)
        }
        viewModel.sortedExpression.observe(this) {
            Log.d(TAG, "onCreate: ${it.toString()}")
            val newList = mutableListOf<Any>()
            if (it.last() !is Float) {
                for (i in it) {
                    if (it.indexOf(i) != it.lastIndex) {
                        newList.add(i)
                    }
                }
            } else {
                newList.addAll(it)
            }
            for (i in newList) {
                if (i == '÷') {
                    if (it[it.indexOf(i) + 1] == 0f) {
                        binding.result.text = "cannot divide to 0"
                        return@observe
                    }
                }
            }
            viewModel.setExpToCalculate(newList)
            val result = calculateResults()
            binding.result.text = result
            if (result.contains('.')) {
                val substring = result.substring(0, result.indexOf('.'))
                binding.result.text = substring
            } else {
                binding.result.text = result
            }
        }
    }

    override fun onClick(p0: View?) {
        if (p0?.id != R.id.backscape) {
            p0 as NeumorphButton
            when (p0.id) {
                R.id.division -> {
                    addOperator("÷")
                }
                R.id.multiplication -> {
                    addOperator("×")
                }
                R.id.substraction -> {
                    addOperator("−")
                }
                R.id.add -> {
                    addOperator("+")
                }
                R.id.clear -> clearAction()
                R.id.add_subs -> {
                    if (plusMinusAddable()) {
                        val stringBuilder = StringBuilder()
                        val sortedlist = viewModel.sortedExpression.value
                        for (i in sortedlist!!) {
                            if (i is Float) {
                                if (sortedlist.indexOf(i) == sortedlist.lastIndex) {
                                    stringBuilder.append(removeDecimal(i * -1))
                                } else {
                                    stringBuilder.append(removeDecimal(i))
                                }
                            } else {
                                stringBuilder.append(i)
                            }
                        }
                        val str = stringBuilder.toString()
                        binding.expression.setText(str)
                    }
                }
                R.id.percent -> {
                    val list = viewModel.sortedExpression.value
                    val last = list?.last()
                    val stringBuilder = StringBuilder()
                    if (last is Float) {
                        val newNumber = last / 100
                        for (i in list) {
                            if (i is Float) {
                                if (list.indexOf(i) == list.lastIndex) {
                                    stringBuilder.append(removeDecimal(newNumber))
                                } else {
                                    stringBuilder.append(removeDecimal(i))
                                }
                            } else stringBuilder.append(i)
                        }
                        binding.expression.setText(stringBuilder.toString())
                    }
                }
                R.id.equals -> {
                    try {
                        val toFloat = binding.result.text.toString().toFloat()
                        val result = toFloat.toString()
                        if (result.contains('.')) {
                            val substring = result.substring(0, result.indexOf('.'))
                            binding.expression.setText(substring)
                        } else {
                            binding.expression.setText(result)
                        }
                    } catch (e: Exception) {
                        return
                    }
                }
                R.id.dot -> {
                    val last = viewModel.sortedExpression.value?.last()
                    if (last is Float) {
                        val prevText = binding.expression.text.toString()
                        if (prevText.last() == '.') return
                        val decimalAddable = decimalAddable(last)
                        if (decimalAddable) binding.expression.setText("$prevText.")
                    }
                }
                else -> {
                    val digit = p0.text.toString()
                    val expression = binding.expression.text.toString()
                    if (digit == "0" && expression.length == 1 && expression[0] == '0') {
                        return
                    }
                    if (expression.length == 1 && expression[0] == '0') {
                        binding.expression.setText(digit)
                        return
                    }
                    binding.expression.setText(expression + digit)
                }
            }
        } else {
            backspaceAction()
        }
    }

    private fun calculateResults(): String {
        val list = mutableListOf<Any>()
        list.addAll(viewModel.expressionToCalculate.value!!)
        if (list.isEmpty()) return ""

        val timesDivision = timesDivisionCalculate(list)
        if (timesDivision.isEmpty()) return ""

        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }

    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex) {
                val operator = passedList[i]
                val nextDigit = passedList[i + 1] as Float
                if (operator == '+')
                    result += nextDigit
                if (operator == '−')
                    result -= nextDigit
            }
        }

        return result
    }

    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        while (list.contains('÷') || list.contains('×')) {
            list = calcTimesDiv(list)
        }
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex && i < restartIndex) {
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float
                when (operator) {
                    '×' -> {
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i + 1
                    }
                    '÷' -> {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }
                    else -> {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }

            if (i > restartIndex)
                newList.add(passedList[i])
        }

        return newList
    }

    external fun multiJNI(number1: Float, number2: Float): Float
    external fun divJNI(number1: Float, number2: Float): Float
    external fun addJNI(number1: Float, number2: Float): Float
    external fun subJNI(number1: Float, number2: Float): Float

    fun addOperator(operator: String) {
        val prevText = binding.expression.text.toString()
        val sorted = viewModel.sortedExpression.value
        try {
            sorted?.last().toString().toFloat()
            if (binding.expression.text.toString().last() == '.') {
                binding.expression.setText(prevText.substring(0, prevText.lastIndex) + operator)
            } else {
                binding.expression.setText(prevText + operator)
            }
        } catch (e: Exception) {
            binding.expression.setText(prevText.substring(0, prevText.lastIndex) + operator)
        }
    }

    fun clearAction() {
        binding.expression.setText("0")
    }

    fun backspaceAction() {
        val text = binding.expression.text.toString()
        if (text == "0") return
        if (text.length == 1) {
            binding.expression.setText("0")
            return
        }
        if (text.reversed()[1] == '-' && text.length == 2) {
            binding.expression.setText("0")
            return
        }
        if (text.reversed()[1] == '-') {
            binding.expression.setText(text.substring(0, text.lastIndex - 1))
            return
        }
        val newText = text.substring(0, text.lastIndex)
        binding.expression.setText(newText)
    }

    fun digitsOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        val expression = binding.expression.text.toString()

        var currentDigit = ""
        for (i in expression) {
            if (i.isDigit() || i == '.' || i == '-') {
                currentDigit += i
            } else {
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(i)
            }
        }

        if (currentDigit != "") {
            list.add(currentDigit.toFloat())
        }
        return list
    }

    fun decimalAddable(number: Float): Boolean {
        val strNumber = number.toString()
        val expression = binding.expression.text
        try {
            val startIndex = binding.expression.text.length - strNumber.length
            val substring = expression.substring(startIndex)
            if (substring == strNumber) return false
        } catch (e: Exception) {
            return true
        }
        return try {
            val afterDot = strNumber.substring(strNumber.indexOf(".") + 1).toInt()
            afterDot == 0
        } catch (e: Exception) {
            Toast.makeText(this, "Too big number to add decimal", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun plusMinusAddable(): Boolean {
        return viewModel.sortedExpression.value?.last() is Float
    }

    fun removeDecimal(number: Float): String {
        val str = number.toString()
        val afterDot = str.substring(str.indexOf(".") + 1)
        return if (afterDot == "0") {
            val newStr = str.substring(0, str.indexOf("."))
            newStr
        } else {
            str
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("data", ison)
        super.onSaveInstanceState(outState)
    }

    companion object {
        init {
            System.loadLibrary("calculatorc")
        }
    }

}