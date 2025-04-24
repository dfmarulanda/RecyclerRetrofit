package com.example.myapplication

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Estudiante(
    @SerializedName("birth_year") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("hair_color") val hairColor: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("height") val height: String
) : Parcelable

data class RequestEstudiantes (
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String,
    @SerializedName("previous") val previousUrl: String,
    @SerializedName("results") val results: MutableList<Estudiante>
)