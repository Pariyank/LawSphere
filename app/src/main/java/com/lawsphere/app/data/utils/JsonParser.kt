package com.lawsphere.app.data.utils

import android.content.Context
import com.lawsphere.app.domain.model.BnsSection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object JsonParser {
    fun loadBnsSections(context: Context): List<BnsSection> {
        val jsonString: String
        try {
            jsonString = context.assets.open("bns_sections.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return emptyList()
        }

        val listType = object : TypeToken<List<BnsSection>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }

    fun loadActList(context: Context): List<String> {
        val jsonString: String
        try {
            jsonString = context.assets.open("legal_acts.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            return emptyList()
        }

        val mapType = object : TypeToken<Map<String, List<String>>>() {}.type
        val data: Map<String, List<String>> = Gson().fromJson(jsonString, mapType)
        return data["acts"] ?: emptyList()
    }
}