package com.omang.app.ui.imageViewer.viewmodel

import android.app.Application
import com.omang.app.data.repository.MediaRepository
import com.omang.app.ui.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    application: Application
) : BaseViewModel(application)