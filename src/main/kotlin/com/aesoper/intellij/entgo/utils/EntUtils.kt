package com.aesoper.intellij.entgo.utils

import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStream
import java.io.InputStreamReader
import java.io.LineNumberReader
import java.nio.charset.StandardCharsets


object EntUtils {
   fun isEntSchemaDirectory(file: VirtualFile?): Boolean {
       if (file == null  || !file.isDirectory) {
           return false
       }

       if (file.findChild("schema") == null) {
           return false
       }

       val generateFile = file.findChild("generate.go") ?: return false

       val content = read(generateFile.inputStream)

       return content != null && content.contains("//go:generate") && content.contains("entgo.io/ent/cmd/ent generate")
   }

    private fun read(inputStream: InputStream?): String? {
        return try {
            val reader = inputStream?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
            val line = LineNumberReader(reader)
            val buffer = StringBuilder()
            var str: String?
            while (line.readLine().also { str = it } != null) {
                buffer.append(str).append("\r\n")
            }
            buffer.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }
}