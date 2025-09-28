package com.abhisekdash.budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    fun getCurrentMonth(): String {
        val date = Date()
        val format = SimpleDateFormat("MMMM", Locale.getDefault())
        return format.format(date)
    }
    fun saveSpending(view: View) {
        val title = findViewById<EditText>(R.id.spendTitle).text.toString()
        val priceText = findViewById<EditText>(R.id.spendPrice).text.toString()

        if (title.isBlank() || priceText.isBlank()) return
        val price = priceText.toDoubleOrNull() ?: return

        val file = File(this.filesDir, "spending_data.json")
        val json: JSONObject

        // Initialize JSON if file doesn't exist or is blank
        if (!file.exists() || file.readText().isBlank()) {
            json = JSONObject()
            val thisMonth = JSONObject()
            thisMonth.put("month", SimpleDateFormat("MMMM", Locale.getDefault()).format(Date()))
            thisMonth.put("data", JSONArray())
            thisMonth.put("totalSpending_thisMonth", 0.0)

            val previousMonth = JSONObject()
            previousMonth.put("totalSpending", 0.0)

            json.put("this_month", thisMonth)
            json.put("previous_month", previousMonth)
        } else {
            json = JSONObject(file.readText())
        }

        val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
        val thisMonthObj = json.getJSONObject("this_month")
        val previousMonthObj = json.getJSONObject("previous_month")

        // Check if month changed
        if (thisMonthObj.getString("month") != currentMonth) {
            // Move last month total to previous_month
            previousMonthObj.put("totalSpending", thisMonthObj.getDouble("totalSpending_thisMonth"))
            // Reset this_month
            thisMonthObj.put("month", currentMonth)
            thisMonthObj.put("data", JSONArray())
            thisMonthObj.put("totalSpending_thisMonth", 0.0)
        }

        // Prepare new spending
        val newSpending = JSONObject()
        newSpending.put("title", title)
        newSpending.put("price", price)

        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val thisMonthArray = thisMonthObj.getJSONArray("data")
        var dayFound = false

        // Check if today exists
        for (i in 0 until thisMonthArray.length()) {
            val dayObj = thisMonthArray.getJSONObject(i)
            if (dayObj.getString("day") == today) {
                dayObj.getJSONArray("spendings").put(newSpending)
                dayFound = true
                break
            }
        }

        // If today not found, create new day object
        if (!dayFound) {
            val newDayObj = JSONObject()
            newDayObj.put("day", today)
            val spendingsArray = JSONArray()
            spendingsArray.put(newSpending)
            newDayObj.put("spendings", spendingsArray)
            thisMonthArray.put(newDayObj)
        }

        // Update total spending
        val total = thisMonthObj.getDouble("totalSpending_thisMonth") + price
        thisMonthObj.put("totalSpending_thisMonth", total)

        // Save JSON to file
        file.writeText(json.toString())
        findViewById<EditText>(R.id.spendTitle).text.clear()
        findViewById<EditText>(R.id.spendPrice).text.clear()

    }
    fun moveToSpendingActivity(view: View){
        val intent = Intent(this, MySpendings::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}