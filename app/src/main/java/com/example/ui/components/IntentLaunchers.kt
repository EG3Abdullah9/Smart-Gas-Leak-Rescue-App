package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun launchNativeSms(context: Context, phone: String, body: String, errorFallback: String) {
    val cleanPhone = phone.trim()
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:${Uri.encode(cleanPhone)}")
        putExtra("sms_body", body)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, errorFallback, Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsApp(context: Context, phone: String, body: String, errorFallback: String) {
    val digits = phone.filter { it.isDigit() }
    val uri = Uri.parse("https://wa.me/$digits?text=${Uri.encode(body)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, errorFallback, Toast.LENGTH_SHORT).show()
    }
}
