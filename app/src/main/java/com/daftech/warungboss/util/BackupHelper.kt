package com.daftech.warungboss.util

import android.content.Context
import com.google.gson.Gson
import java.io.File

object BackupHelper {
    fun exportJson(context: Context, json: String): File {
        val dir = File(context.getExternalFilesDir(null), "backup")
        if (!dir.exists()) dir.mkdirs()
        val f = File(dir, "warungboss-backup-" + System.currentTimeMillis() + ".json")
        f.writeText(json)
        return f
    }
    fun readJson(file: File): String = file.readText()
    fun gson(): Gson = Gson()
}
