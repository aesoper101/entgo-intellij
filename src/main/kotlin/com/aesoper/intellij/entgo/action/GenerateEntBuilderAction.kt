package com.aesoper.intellij.entgo.action

import com.aesoper.intellij.entgo.Constants
import com.aesoper.intellij.entgo.EntBundle
import com.aesoper.intellij.entgo.notification.Notification
import com.aesoper.intellij.entgo.utils.*
import com.goide.util.GoExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys


class GenerateEntBuilderAction : AnAction(EntBundle.message("new.builder.action.name")) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val module = e.getData(PlatformDataKeys.MODULE) ?: return
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        GoExecutor.`in`(project, module)
            .disablePty()
            .withExePath(Constants.EntCmdName)
            .withParameters(listOf("generate", "./schema"))
            .withWorkDirectory(file.path)
            .withPresentableName(Constants.EntCmdName + " generate ./schema")
            .executeWithProgress {
                when (it.status) {
                    GoExecutor.ExecutionResult.Status.SUCCEEDED -> {
                        Notification.info(project, "ent generate success")
                        FileReload.reloadFromDisk(e)
                    }
                    GoExecutor.ExecutionResult.Status.FAILED -> {
                        it.message?.let { it1 -> Notification.error(project, it1) }
                    }
                    else -> {
                        it.message?.let { it1 -> Notification.info(project, it1) }
                    }
                }
            }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        when {
            e.project == null || file == null || !file.isDirectory || !EntUtils.isEntSchemaDirectory(file) -> {
                ActionEventUtils.setEnabledAndVisible(e, false)
                return
            }
        }
    }
}