package com.lawsphere.app.data.utils

import android.content.Context
import android.util.Log
import com.lawsphere.app.domain.model.BnsSection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object JsonParser {
    fun loadActList(context: Context): List<String> {
        return try {
            val jsonString = context.assets.open("legal_acts.json")
                .bufferedReader().use { it.readText() }

            val gson = Gson()

            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            val data: Map<String, List<String>> = gson.fromJson(jsonString, type)

            if (data.containsKey("acts")) {
                data["acts"] ?: emptyList()
            } else {
                val listType = object : TypeToken<List<String>>() {}.type
                gson.fromJson(jsonString, listType) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("JsonParser", "Error loading legal_acts.json: ${e.message}")
            emptyList()
        }
    }
}