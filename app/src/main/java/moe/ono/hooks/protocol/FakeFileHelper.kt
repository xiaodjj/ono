package moe.ono.hooks.protocol

import FakeFile.root
import FakeFile.subproto2
import FakeFile.subproto7
import moe.ono.util.Logger
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import kotlin.math.pow

fun create(filename: String, size: Long): String {
    try {
        val rootMsg = root.newBuilder()
            .setF1(6)
            .build()


        val randomString = UUID.randomUUID().toString()
        val md5Hash = md5(randomString).uppercase(Locale.getDefault())

        val subProto2Msg = subproto2.newBuilder()
            .setF7("{\"info\": \"powered by ono\"}")
            .setF8(md5Hash)
            .setF4(filename)
            .setF3(size)
            .setF1(102)
            .setF2(UUID.randomUUID().toString())
            .build()

        val subProto7Msg = subproto7.newBuilder()
            .setF2(subProto2Msg)
            .build()

        val updatedRootMsg = rootMsg.toBuilder()
            .setF7(subProto7Msg)
            .build()

        val serializedData = updatedRootMsg.toByteArray()

        val lengthBytes = ByteArray(2)
        lengthBytes[0] = (serializedData.size shr 8 and 0xFF).toByte()
        lengthBytes[1] = (serializedData.size and 0xFF).toByte()

        val finalByteArray = ByteArray(1 + 2 + serializedData.size)
        finalByteArray[0] = 1
        System.arraycopy(lengthBytes, 0, finalByteArray, 1, 2)
        System.arraycopy(serializedData, 0, finalByteArray, 3, serializedData.size)

        val hexOutput = bytesToHex(finalByteArray).uppercase(Locale.getDefault())

        val result = """
                {
                  "5": {
                    "1": 24,
                    "2": "$hexOutput"
                  }
                }
            """.trimIndent()

        Logger.i("FakeFile/result: $result")
        return result

    } catch (e: Exception) {
        Logger.e("FakeFile/err: $e")
        return ""
    }
}

fun parseFileSize(input: String): Long {
    val regex = Regex("""(\d+(\.\d+)?)([KMGTPEB]?)""", RegexOption.IGNORE_CASE)
    val matchResult = regex.find(input.trim())

    if (matchResult != null) {
        val value = matchResult.groupValues[1].toDouble()
        val unit = matchResult.groupValues[3].uppercase(Locale.getDefault())

        if (value == 0.0) return 0

        val multiplier = when (unit.uppercase(Locale.getDefault())) {
            "K", "KB", "k", "kb" -> 1024.0.pow(1).toLong()
            "M", "MB", "m", "mb" -> 1024.0.pow(2).toLong()
            "G", "GB", "gb", "g" -> 1024.0.pow(3).toLong()
            "T", "TB", "tb", "t" -> 1024.0.pow(4).toLong()
            "P", "PB", "pb", "p" -> 1024.0.pow(5).toLong()
            "E", "EB", "e", "eb" -> 1024.0.pow(6).toLong()
            else -> 1L
        }


        val result = (value * multiplier).toLong()
        return if (unit == "E" && value >= 8) Long.MAX_VALUE else result
    }

    throw IllegalArgumentException("Invalid file size format: $input")
}

private fun md5(input: String): String {
    val randomSalt = UUID.randomUUID().toString()
    val saltedInput = input + randomSalt
    val md = MessageDigest.getInstance("MD5")
    val hashBytes = md.digest(saltedInput.toByteArray())
    return hashBytes.joinToString("") { String.format("%02X", it) }
}

private fun bytesToHex(bytes: ByteArray): String {
    return bytes.joinToString("") { String.format("%02X", it) }
}