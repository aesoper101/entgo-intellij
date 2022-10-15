package com.aesoper.intellij.entgo.action


import com.aesoper.intellij.entgo.Constants
import com.aesoper.intellij.entgo.EntBundle
import com.aesoper.intellij.entgo.notification.Notification
import com.aesoper.intellij.entgo.utils.*
import com.goide.util.GoExecutor
import com.goide.util.GoGetPackageUtil
import com.goide.vgo.VgoUtil
import com.intellij.ide.actions.CreateFileAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import java.io.File


class NewEntSchemaAction : AnAction(
        EntBundle.message("new.schema.action.name"), EntBundle.message("new.schema.action.description"), null
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val module = e.getData(PlatformDataKeys.MODULE) ?: return
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        GoExecutor.`in`(project, module)
//            .disablePty()
                .withExePath(Constants.EntCmdName)
                .withParameters(
                        listOf("init")
                )
                .withWorkDirectory(file.path)
                .withPresentableName(Constants.EntCmdName + " init")
                .executeWithOutput {
                    when (it.status) {
                        GoExecutor.ExecutionResult.Status.SUCCEEDED -> {
                            Notification.info(project, "create schema success")

                            val parent = VgoUtil.findModuleRoot(file)
                            if (parent != null) {
                                GoGetPackageUtil.addDependency("entgo.io/ent", project, parent.path)
                            }
                            VfsUtil.markDirtyAndRefresh(
                                    true, true, true, file.parent
                            )
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
            e.project == null || file == null || !file.isDirectory || EntUtils.isEntSchemaDirectory(file) || EntUtils.isEntSchemaDirectory(
                    file.parent
            ) -> {
                ActionEventUtils.setEnabledAndVisible(e, false)
                return
            }
        }
    }

}


