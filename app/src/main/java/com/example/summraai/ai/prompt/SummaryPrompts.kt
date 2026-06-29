package com.example.summraai.ai.prompt

object SummaryPrompts {

    fun textSummaryPrompt(text: String, style: String): String = """
Summarize the following text in a $style style:
$text
""".trimIndent()

    fun websiteSummaryPrompt(url: String, content: String, style: String): String = """
Summarize the following webpage content from $url in a $style style:
$content
""".trimIndent()

    fun youtubeSummaryPrompt(videoTitle: String, transcript: String, style: String): String = """
Summarize the following YouTube video "$videoTitle" in a $style style:
$transcript
""".trimIndent()

    fun pdfSummaryPrompt(text: String, style: String): String = """
Summarize the following document text in a $style style:
$text
""".trimIndent()
}
