package com.aesoper.intellij.entgo.utils

import com.aesoper.intellij.entgo.notification.Notification
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import java.io.File
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.pathString


object ExecUtils {

    fun lookPath(project: Project, cmdName: String): String? {
        var os = System.getProperty("os.name")
        os = os.lowercase(Locale.getDefault())

        return when {
            os.startsWith("mac") || os.startsWith("linux") -> {
                LookPathUnix(project).lookPath(cmdName)
            }
            os.startsWith("windows") -> {
                LookPathWindows(project).lookPath(cmdName)
            }
            else -> {
                null
            }
        }

    }


    @Suppress("unused")
    private fun run(command: List<String>, workDirectory: String?): ProcessOutput {
        val commandLine = GeneralCommandLine(command)
        if (workDirectory != null && StringUtil.isNotEmpty(workDirectory)) {
            commandLine.workDirectory = File(workDirectory)
        }

        return ExecUtil.execAndGetOutput(commandLine)
    }

    fun runCmd(pe: ProcessEntity) {
        if (!pe.beforeRun()) {
            return
        }
        val result = run(pe.command, pe.workDirectory)
        pe.afterRun(result)
    }

}

private interface LookPathInterface {
    fun lookPath(file: String): String?
}

private class LookPathUnix(private var project: Project) : LookPathInterface {

    override fun lookPath(file: String): String? {
        if (StringUtil.containsAnyChar(file, "/")) {
            val f = findExecutable(file)
            if (!StringUtil.isEmpty(f)) {
                return f
            }
        }

        val path = System.getenv("PATH").replace("\"", "")
        for (dir in path.split(";")) {
            var tmpDir = dir
            if (StringUtil.isEmpty(dir)) {
                tmpDir = "."
            }

            val path2 = findExecutable(Path(tmpDir, file).pathString)
            if (!StringUtil.isEmpty(path2)) {
                return path2
            }
        }

        Notification.error(project, "Not Found $file")
        return null
    }

    private fun findExecutable(file: String): String? {
        val f = File(file)
        return if (!f.isDirectory && f.canExecute()) {
            file
        } else {
            null
        }
    }


}

private class LookPathWindows(private var project: Project) : LookPathInterface {

    override fun lookPath(file: String): String? {
        val exts = mutableListOf<String>()

        val entResult = System.getenv("PATHEXT")
        if (!StringUtil.isEmpty(entResult)) {
            for (e in entResult.split(";")) {
                if (StringUtil.isEmpty(e)) {
                    continue
                }
                var ext = e
                if (e[0].toString() != ".") {
                    ext = ".$e"
                }

                exts.add(ext)
            }
        } else {
            exts.add(".com")
            exts.add(".exe")
            exts.add(".bat")
            exts.add(".cmd")
        }

        if (StringUtil.containsAnyChar(file, ":\\/")) {
            return findExecutable(file, exts)
        }

        // todo: no tests
        val f = findExecutable(StringUtil.join(Path(".", file).pathString), exts)
        if (!StringUtil.isEmpty(f)) {
            return f
        }

        val path = System.getenv("PATH").replace("\"", "")
        for (v in path.split(";")) {

            val f1 = findExecutable(Path(v, file).pathString, exts)
            if (!StringUtil.isEmpty(f1)) {
                return f1
            }
        }

        Notification.error(project, "Not Found $file")
        return null
    }

    private fun findExecutable(file: String, exts: MutableList<String>): String? {
        if (exts.isEmpty()) {
            return if (chkStat(file)) {
                file
            } else {
                null
            }
        }
        if (hasExt(file)) {
            if (chkStat(file)) {
                return file
            }
        }

        for (ext in exts) {
            val f = file + ext
            if (chkStat(f)) {
                return f
            }
        }

        return null
    }

    private fun hasExt(file: String): Boolean {
        val i = file.lastIndexOf(".")
        if (i < 0) {
            return false
        }

        return StringUtil.lastIndexOfAny(file, ":\\/") < i
    }


    private fun chkStat(file: String): Boolean {
        val f = File(file)
        return !f.isDirectory && f.canExecute()
    }
}

interface RunHandler {
    fun beforeRun(): Boolean
    fun afterRun(output: ProcessOutput)
}

abstract class ProcessEntity(
    var project: Project,
    private val cmdName: String,
    params: List<String>,
    var workDirectory: String?
) :
    RunHandler {
    val command = mutableListOf<String>()

    init {
        command.add(cmdName)
        for (p in params) {
            command.add(p)
        }
    }

    override fun beforeRun(): Boolean {
        if (!StringUtil.isNotEmpty(ExecUtils.lookPath(project, this.cmdName))) {
            Notification.error(project, "$cmdName not found in PATH")
            return false
        }
        return true
    }

    override fun afterRun(output: ProcessOutput) {

    }
}
