/**
 * Databinding utility class
 */

package com.omang.app.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.omang.app.R
import com.omang.app.data.model.resources.OptionItem
import com.omang.app.utils.extensions.DateTimeFormat
import com.omang.app.utils.extensions.convertTimestampToLocale

@BindingAdapter("placeholder", "url")
fun setImage(image: ImageView, placeHolder: Drawable, url: String?) {

    if (!url.isNullOrEmpty()) {
        image.load(url) {
            placeholder(R.drawable.place_holder)
            error(R.drawable.place_holder)
            crossfade(true)
            // transformations(CircleCropTransformation())
        }
    } else {
        image.setImageDrawable(placeHolder)
    }
}

@BindingAdapter("placeholder", "url")
fun setShapeableImageView(image: ShapeableImageView, placeHolder: Drawable, url: String?) {

    if (!url.isNullOrEmpty()) {
        image.load(url) {
            placeholder(R.drawable.place_holder)
            error(R.drawable.place_holder)
            // transformations(CircleCropTransformation())
        }
    } else {
        image.setImageDrawable(placeHolder)
    }
}

@BindingAdapter("placeholder", "imageAbsolutePath")
fun setImageUsingGlide(image: ImageView, placeHolder: Drawable, url: String) {
    if (url.isNotEmpty()) {
        Glide.with(image.context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .error(placeHolder)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(image)
    } else {
        image.setImageDrawable(placeHolder)
    }
}

@BindingAdapter("track_progress")
fun setProgressBarTrackColor(image: CircularProgressIndicator, progress: Int) {
    val (_, progressSecondary) = DrawableUtil.getProgressTint(progress)
    val progressColor = image.context.resources.getColor(progressSecondary, null)
    image.trackColor = progressColor
}

@BindingAdapter("indicator_progress")
fun setProgressBarIndicatorColor(image: CircularProgressIndicator, progress: Int) {
    val (progressMain, _) = DrawableUtil.getProgressTint(progress)
    val progressColor = image.context.resources.getColor(progressMain, null)
    image.setIndicatorColor(progressColor)
}

@BindingAdapter("card_background")
fun setCardBackGroundColor(card: CardView, progress: Int) {
    val (progressMain, _) = DrawableUtil.getProgressTint(progress)
    val progressColor = card.context.resources.getColor(progressMain, null)
    card.setCardBackgroundColor(progressColor)
}

@BindingAdapter("last_updated_text")
fun setLastSyncedOn(textView: TextView, dateFromServer: String) {
    val convertedText = convertTimestampToLocale(
        dateFromServer, DateTimeFormat.TO_LOCALE_TIME_N_DATE
    )
    textView.text = if (ValidationUtil.isNotNullOrEmpty(convertedText))
        textView.context.getString(R.string.last_updated, convertedText)
    else
        ""

}

@BindingAdapter("question_answer")
fun setQuestionAnswer(textView: TextView, optionsList: List<OptionItem>) {
    textView.text = "Not Attempted"
    optionsList.map { optionItem ->
        if (optionItem.isSelected)
            textView.text = optionItem.option
        return@map
    }
}
