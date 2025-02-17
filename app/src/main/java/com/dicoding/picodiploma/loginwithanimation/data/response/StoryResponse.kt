package com.dicoding.picodiploma.loginwithanimation.data.response

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class StoryResponse(
	@field:SerializedName("listStory")
	val listStory: List<ListStoryItem> = emptyList(),
	@field:SerializedName("error")
	val error: Boolean? = null,
	@field:SerializedName("message")
	val message: String? = null
) : Parcelable

@Parcelize
data class ListStoryItem(
	@field:SerializedName("photoUrl")
	val photoUrl: String? = null,
	@field:SerializedName("createdAt")
	val createdAt: String? = null,
	@field:SerializedName("name")
	val name: String? = null,
	@field:SerializedName("description")
	val description: String? = null,
	@field:SerializedName("lon")
	val lon: Double? = null,
	@field:SerializedName("id")
	val id: String? = null,
	@field:SerializedName("lat")
	val lat: Double? = null
) : Parcelable
