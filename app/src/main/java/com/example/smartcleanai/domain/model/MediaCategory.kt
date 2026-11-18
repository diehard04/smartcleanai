package com.example.smartcleanai.domain.model

enum class MediaCategory(val title: String, val description: String) {
    DUPLICATE("Similar & Duplicates", "Identical files and burst shot clusters"),
    BLURRY("Blurry & Dark Photos", "Out-of-focus, shaky, or accidental dark shots"),
    WHATSAPP_JUNK("WhatsApp Junk & Memes", "Forwarded memes, greeting flyers with heavy text"),
    SCREENSHOT("Old Screenshots", "Screenshots older than 30 days"),
    LARGE_VIDEO("Large Videos", "Video files larger than 50 MB"),
    CLEAN("Clean", "High quality media kept safe")
}
