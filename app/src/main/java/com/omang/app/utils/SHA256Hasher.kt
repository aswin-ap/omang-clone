package com.omang.app.utils

import java.security.MessageDigest

class SHA256Hasher {
    companion object {
        fun sha256(input: String): String {
            val bytes = input.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return bytesToHex(digest)
        }

        private fun bytesToHex(bytes: ByteArray): String {
            val hexArray = "0123456789abcdef".toCharArray()
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
    }
}
