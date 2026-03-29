package com.lawsphere.app.domain.model

import com.google.gson.annotations.SerializedName

data class BnsSection(
    @SerializedName("section")
    val section: String? = "",

    @SerializedName("title")
    val title: String? = "",

    @SerializedName("description")
    val description: String? = "",

    @SerializedName("punishment")
    val punishment: String? = "",

    @SerializedName("cognizable")
    val cognizable: String? = "",

    @SerializedName("bailable")
    val bailable: String? = "",

    @SerializedName("chapter")
    val chapter: String? = "",

    @SerializedName("cases")
    val cases: List<String?>? = emptyList(),

    @SerializedName("category")
    val category: String? = ""
)