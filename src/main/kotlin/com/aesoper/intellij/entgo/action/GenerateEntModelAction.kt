package com.aesoper.intellij.entgo.action

import com.aesoper.intellij.entgo.Constants
import com.aesoper.intellij.entgo.EntBundle
import com.aesoper.intellij.entgo.notification.Notification
import com.aesoper.intellij.entgo.utils.*
import com.goide.util.GoExecutor
import com.intellij.execution.process.ProcessOutput
import com.intellij.ide.actions.CreateFileAction
import com.intellij.ide.ui.newItemPopup.NewItemPopupUtil
import com.intellij.ide.ui.newItemPopup.NewItemSimplePopupPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import java.util.*

class GenerateEntModelAction : AnAction(EntBundle.message("new.asserts.action.name"), "", null) {


    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val directory = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val module = e.getData(PlatformDataKeys.MODULE) ?: return
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val contentPanel = NewItemSimplePopupPanel()
        val nameField = contentPanel.textField

        val popup =
                NewItemPopupUtil.createNewItemPopup(EntBundle.message("action.ent.new.name"), contentPanel, nameField)

        contentPanel.setApplyAction { event ->
            val name = nameField.text
            val validator = ModelNameInputValidator(project)
            if (validator.checkInput(name) && validator.canClose(name)) {
                when (directory.findChild(name)) {
                    null -> {
                        popup.closeOk(event)

                        WriteCommandAction.runWriteCommandAction(project) {
                            GoExecutor.`in`(module)
//                                    .disablePty()
                                    .withExePath(Constants.EntCmdName)
                                    .withParameters(
                                            listOf(
                                                    "init", "--target", "./schema", name.toCamelCase()
                                            )
                                    )
                                    .withPresentableName(Constants.EntCmdName + " init --target ./schema " + name.toCamelCase()).withWorkDirectory(file.path)
                                    .executeWithOutput {
                                        when (it.status) {
                                            GoExecutor.ExecutionResult.Status.SUCCEEDED -> {
                                                Notification.info(project, "ent model generate success")
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
                    }

                    else -> {
                        contentPanel.setError(EntBundle.message("action.ent.new.model.exists", name))
                    }
                }
            }
        }

        popup.showCenteredInCurrentWindow(project)

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

    private class ModelNameInputValidator(private var project: Project) : InputValidator {
        override fun checkInput(inputString: String): Boolean {
            if (inputString.isEmpty()) {
                return false
            }

            if (!Regex("[a-zA-Z]\\w*").matches(inputString)) {
                Notification.error(project, "Model name must be matched [a-zA-Z]\\w*")
                return false
            }

            return true
        }

        override fun canClose(inputString: String): Boolean {
            return true
        }
    }
}

