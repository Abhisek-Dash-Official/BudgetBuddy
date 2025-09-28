package com.abhisekdash.budgetbuddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MySpendings : AppCompatActivity() {

    fun moveToMainActivity(view: View){
        intent = Intent(this, MainActivity::class.java);
        startActivity(intent);
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_spendings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val recyclerView = findViewById<RecyclerView>(R.id.todaySpendingList)
        val totalTv = findViewById<TextView>(R.id.todayTotal)
        val previousMonthTv = findViewById<TextView>(R.id.previousMonthSpending)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val file = File(filesDir, "spending_data.json")
        if (!file.exists() || file.readText().isBlank()) return

        val json = JSONObject(file.readText())

        // --- Previous Month ---
        val previousTotal = json.getJSONObject("previous_month").optDouble("totalSpending", 0.0)
        previousMonthTv.text = "Previous Month: ₹$previousTotal"

        // --- Today’s spending ---
        val thisMonthArray = json.getJSONObject("this_month").getJSONArray("data")
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val titles = mutableListOf<String>()
        val prices = mutableListOf<Double>()
        var total = 0.0

        for (i in 0 until thisMonthArray.length()) {
            val dayObj = thisMonthArray.getJSONObject(i)
            if (dayObj.getString("day") == today) {
                val spendings = dayObj.getJSONArray("spendings")
                for (j in 0 until spendings.length()) {
                    val item = spendings.getJSONObject(j)
                    titles.add(item.getString("title"))
                    prices.add(item.getDouble("price"))
                    total += item.getDouble("price")
                }
                break
            }
        }

        totalTv.text = "Total: ₹$total"

// --- 🔹 Monthly Summary ---
        val thisMonthSummaryList = findViewById<RecyclerView>(R.id.thisMonthSummaryList)
        thisMonthSummaryList.layoutManager = LinearLayoutManager(this)

        val summaryDays = mutableListOf<String>()
        val summaryTotals = mutableListOf<Double>()

        for (i in 0 until thisMonthArray.length()) {
            val dayObj = thisMonthArray.getJSONObject(i)
            val day = dayObj.getString("day")
            val spendings = dayObj.getJSONArray("spendings")

            var dayTotal = 0.0
            for (j in 0 until spendings.length()) {
                val item = spendings.getJSONObject(j)
                dayTotal += item.getDouble("price")
            }

            summaryDays.add(day)
            summaryTotals.add(dayTotal)
        }

        // --- Monthly Summary Adapter ---
        thisMonthSummaryList.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_day_summary, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val view = holder.itemView
                val dayTv = view.findViewById<TextView>(R.id.dayLabel)
                val totalTv = view.findViewById<TextView>(R.id.dayTotal)

                // Day ka label + date
                dayTv.text = "Day ${summaryDays[position]}"
                // Total spending
                val dayTotalValue = summaryTotals[position]
                val displayText = if (dayTotalValue >= 100_000) {
                    "99999+"
                } else {
                    dayTotalValue.toInt().toString()
                }
                totalTv.text = "₹$displayText"
            }

            override fun getItemCount() = summaryDays.size
        }

        // Simple adapter directly
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                // Inflate item_spending.xml instead of creating TextView manually
                val view = layoutInflater.inflate(R.layout.item_spending, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val view = holder.itemView
                val titleTv = view.findViewById<TextView>(R.id.title)
                val priceTv = view.findViewById<TextView>(R.id.price)

                titleTv.text = titles[position]
                priceTv.text = "₹${prices[position]}"
            }

            override fun getItemCount() = titles.size
        }
    }
}