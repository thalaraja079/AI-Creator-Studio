package com.example.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class GenerationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val toolId: String,
    val toolTitle: String,
    val emojiIcon: String,
    val prompt: String,
    val output: String,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("toolId", toolId)
        obj.put("toolTitle", toolTitle)
        obj.put("emojiIcon", emojiIcon)
        obj.put("prompt", prompt)
        obj.put("output", output)
        obj.put("timestampMillis", timestampMillis)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): GenerationHistoryItem {
            return GenerationHistoryItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                toolId = obj.optString("toolId", "QUICK_GEN"),
                toolTitle = obj.optString("toolTitle", "Quick Studio Generator"),
                emojiIcon = obj.optString("emojiIcon", "✨"),
                prompt = obj.optString("prompt", ""),
                output = obj.optString("output", ""),
                timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis())
            )
        }
    }
}
