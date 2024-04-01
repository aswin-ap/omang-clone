package com.omang.app.data.model.resources

import com.google.gson.annotations.SerializedName
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.model.test.McqResponse
import com.omang.app.data.model.userAssign.Psm
import com.omang.app.utils.extensions.toJson
import timber.log.Timber

data class ResourceResponse(
    @field:SerializedName("data")
    val data: Data?,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("statusCode")
    val statusCode: Int,

    )

data class Data(
    @field:SerializedName("deleted")
    val deleted: Deleted,

    @field:SerializedName("added")
    val added: Added,

    @field:SerializedName("id")
    val id: Int,

    )

data class Added(
    @field:SerializedName("webPlatforms")
    val webPlatforms: List<WebsiteItem>,

    @field:SerializedName("books")
    val books: List<MediaItem>,

    @field:SerializedName("videos")
    val videos: List<MediaItem>,

    @field:SerializedName("units")
    val units: List<UnitsItem>,

    @field:SerializedName("Psm")
    val psm: Psm? = null,

    @field:SerializedName("mcqs")
    val mcqs: McqData,

)

data class McqData(
    @SerializedName("attended")
    val attendedMCQs: List<McqResponse.AttendedMCQ>,
    @SerializedName("expired")
    val expiredMCQs: List<McqResponse.ExpiredMCQ>,
    @SerializedName("toBeAttended")
    val notAttendedMCQs: List<McqResponse.NotAttendedMCQ>
)

data class WebsiteItem(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("logo")
    val logo: String,


    )

data class MediaItem(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("logo")
    val logo: String,

    @field:SerializedName("type")
    val type: String,

    @field:SerializedName("file")
    val file: String,

    )

data class UnitsItem(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("completed")
    val completed: Boolean = false,

    @field:SerializedName("objective")
    val objective: String,

    @field:SerializedName("lessons")
    val lessons: List<LessonsItem>,

    @field:SerializedName("mcqs")
    val mcqs: List<McqItem>,

    )

data class LessonsItem(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String?,

    @field:SerializedName("logo")
    val logo: String,

    @field:SerializedName("file")
    val file: String,

    @field:SerializedName("webPlatforms")
    val webPlatforms: List<WebsiteItem>,

    @field:SerializedName("createdBy")
    val createdBy: CreatedBy?,

    )

data class CreatedBy(
    @field:SerializedName("firstName")
    val firstName: String?,

    @field:SerializedName("lastName")
    val lastName: String?,

    @field:SerializedName("avatar")
    val avatar: String?,

    )

data class Deleted(
    @field:SerializedName("webPlatforms")
    val webPlatforms: List<Int>,

    @field:SerializedName("books")
    val books: List<MediaItem>,

    @field:SerializedName("videos")
    val videos: List<MediaItem>,

    @field:SerializedName("units")
    val units: List<Any>,

    )

data class McqItem(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("instructions")
    val instructions: String,

    @field:SerializedName("parentId")
    val parentId: Int? = null,

    @field:SerializedName("unit")
    val unit: Int,

    @field:SerializedName("questions")
    val questions: List<QuestionItem>,
)

data class QuestionItem(

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("question")
    val question: String,

    @field:SerializedName("score")
    val score: Int,

    @field:SerializedName("questionType")
    val questionType: Int,

    @field:SerializedName("questionURL")
    val questionUrl: String? = null,

    @field:SerializedName("options")
    val options: List<OptionItem>,


    )

data class OptionItem(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("option")
    val option: String,

    @field:SerializedName("optionURL")
    val optionUrl: String? = null,

    @field:SerializedName("isAnswer")
    val isAnswer: Boolean,

    @SerializedName("answered")
    val answered: Boolean,
) {
    var isSelected: Boolean = false
}

fun List<OptionItem>.updateSelectedId(selectedId: Int) = map { item ->
    item.isSelected = item.id == selectedId
}

fun QuestionEntity.addOrRemoveElementFromSelectedOptions(selectedId: OptionItem) {
    selectedOptions.apply {
        if (isNullOrEmpty()) {
            add(selectedId)
        } else if (contains(selectedId)) {
            remove(selectedId)
        } else {
            add(selectedId)
        }
    }
    Timber.d("Multiple options: ${selectedOptions.toJson()}")
}




