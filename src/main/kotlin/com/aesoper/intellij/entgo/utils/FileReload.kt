package com.aesoper.intellij.entgo.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys


object FileReload {
    fun reloadFromDisk(e: AnActionEvent?) {
        if (e == null) {
            return
        }
        val project = e.project ?: return

        project.save()

        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        file.fileSystem.refresh(true)
    }
}
