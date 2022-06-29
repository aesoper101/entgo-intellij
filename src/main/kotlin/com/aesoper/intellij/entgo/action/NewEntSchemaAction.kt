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


class NewEntSchemaAction :
    AnAction(
        EntBundle.message("new.schema.action.name"),
        EntBundle.message("new.schema.action.description"), null
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating schema ...") {
            override fun run(indicator: ProgressIndicator) {
                ExecUtils.runCmd(object :
                    ProcessEntity(project, Constants.EntCmdName, listOf("init"), file.path) {
                    override fun afterRun(output: ProcessOutput) {
                        if (output.exitCode == 0) {
                            Notification.info(project, "create schema success")
                            FileReload.reloadFromDisk(e)
                            return
                        }

                        Notification.error(project, output.stderr)
                    }
                })

            }
        })

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Go mod tidy ...") {
            override fun run(indicator: ProgressIndicator) {
                ExecUtils.runCmd(object : ProcessEntity(project, Constants.GoCmdName, listOf("mod", "tidy"), file.path) {})
            }
        })


    }


    override fun update(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        when {
            e.project == null || file == null || !file.isDirectory || EntUtils.isEntSchemaDirectory(file) || EntUtils.isEntSchemaDirectory(
                file.parent
            ) -> {
                ActionEventUtils.setEnabledAndVisible(e, false)
                return
            }
        }
    }

}


