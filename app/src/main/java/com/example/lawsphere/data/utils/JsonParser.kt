package com.example.lawsphere.data.utils

import android.content.Context
import com.example.lawsphere.domain.model.BnsSection
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
}