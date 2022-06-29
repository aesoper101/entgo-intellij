package com.aesoper.intellij.entgo.utils

import com.intellij.openapi.actionSystem.AnActionEvent

object ActionEventUtils {
    fun setEnabledAndVisible(e: AnActionEvent, flag: Boolean) {
        e.presentation.isEnabled = flag
        e.presentation.isVisible = flag
    }
}