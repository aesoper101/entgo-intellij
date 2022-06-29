package com.aesoper.intellij.entgo.action

import com.aesoper.intellij.entgo.Constants
import com.aesoper.intellij.entgo.EntBundle
import com.aesoper.intellij.entgo.notification.Notification
import com.aesoper.intellij.entgo.utils.*
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task

class GenerateEntBuilderAction : AnAction(EntBundle.message("new.builder.action.name")) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating builder ...") {
            override fun run(indicator: ProgressIndicator) {
                ExecUtils.runCmd(object :
                    ProcessEntity(project, Constants.EntCmdName, listOf("generate", "./schema"), file.path) {
                    override fun afterRun(output: ProcessOutput) {
                        if (output.exitCode == 0) {
                            Notification.info(project, "ent generate success")
                            FileReload.reloadFromDisk(e)
                            return
                        }

                        Notification.error(project, output.stderr)
                    }
                })
            }
        })
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