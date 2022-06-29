package com.aesoper.intellij.entgo

import com.aesoper.intellij.entgo.utils.EntUtils
import com.intellij.icons.AllIcons
import com.intellij.ide.IconProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import javax.swing.Icon

class EntIconProvider : IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if (element is PsiDirectory) {
            val folder: VirtualFile = element.virtualFile
            if (EntUtils.isEntSchemaDirectory(folder) || EntUtils.isEntSchemaDirectory(folder.parent)) {
                return AllIcons.Modules.GeneratedFolder
            }
        }

        return null
    }
}