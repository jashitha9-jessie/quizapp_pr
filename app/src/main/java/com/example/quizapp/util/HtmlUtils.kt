package com.example.quizapp.util

import androidx.core.text.HtmlCompat

/** Open Trivia DB returns HTML entities (&quot; &#039; &amp; ...). This decodes them. */
fun String.decodeHtml(): String =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
