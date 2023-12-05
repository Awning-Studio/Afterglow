package com.awning.afterglow.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.awning.afterglow.module.edusystem.EduSystem


/**
 * 运行时 ViewModel，提供 viewModelScope，
 */
object RuntimeVM : ViewModel() {
    var eduSystem by mutableStateOf<EduSystem?>(null)
    var isLoggingIn by mutableStateOf(false)
}