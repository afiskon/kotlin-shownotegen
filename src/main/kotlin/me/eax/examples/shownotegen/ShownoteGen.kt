package me.eax.examples.shownotegen

import org.apache.http.client.methods.*
import org.apache.http.impl.client.*
import java.util.regex.*
import java.io.*

fun defaultUserAgent() =
    """Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, """ +
    """like Gecko) Chrome/39.0.2171.95 Safari/537.36 OPR/26.0.1656.60"""

fun getTitle(url: String): String {
    val req = HttpGet(url)
    req.setHeader("User-Agent", defaultUserAgent())
    try {
        HttpClients.createDefault() use {
            it.execute(req) use { resp ->
                val entity = resp.getEntity()
                val charset = {
                    val contentType = entity.getContentType().getValue() ?: ""
                    val pattern = Pattern.compile("charset=(.*)")
                    val matcher = pattern.matcher(contentType)
                    if (matcher.find()) matcher.group(1) else "UTF-8"
                }()
                val title = {
                    val body = entity.getContent().readBytes().toString(charset)
                    val pattern = Pattern.compile("""(?is)<title>(.*?)</title>""")
                    val matcher = pattern.matcher(body)
                    matcher.find()
                    matcher.group(1)
                }()
                return title.replaceAll("""\s+""", " ").trim()
            }

        }
    } catch(e: Exception) {
        return "[NO TITLE: ${e.getMessage()}]"
    }
}

fun processFile(fileName: String) {
    val data = File(fileName).readText(Charsets.UTF_8)
    val matcher = Pattern.compile("""https?://\S+""").matcher(data)
    println("<ul>")
    while(matcher.find()) {
        val url = matcher.group()
        println("<li><a href=\"$url\">${getTitle(url)}</a></li>")
    }
    println("</ul>")
}

fun printUsage() {
    val executableName = System.getProperty("sun.java.command")
    println("Usage: $executableName input.txt")
    System.exit(1)
}

fun main(args : Array<String>) {
    when(args.size()) {
        0 -> printUsage()
        else -> processFile(args[0])
    }
}
