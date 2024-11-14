package com.github.linrol.tool.lang

import com.github.linrol.tool.model.GitCmd
import com.github.linrol.tool.state.ToolSettingsState
import com.github.linrol.tool.utils.OkHttpClientUtils
import com.github.linrol.tool.utils.getValue
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*
import java.util.concurrent.TimeUnit

class LangTranslater(val project: Project) {

    private val cache = mutableMapOf<String, String>()

    private var printUse = false

    private var batch = false

    private val canProxy = canProxy()

    companion object {
        private val logger = logger<FrontLangAction>()
        private val gson = Gson()
    }

    fun translate(text: String): String {
        // 过滤除纯中文以外的内容
        val api = ToolSettingsState.instance.translaterApi
        return when {
            api == "baidu" -> translateUseBaidu(text)
            api == "youdao" -> translateUseYoudao(text)
            api == "google" && canProxy -> translateUseGoogle(text)
            api == "chatgpt" -> translateUseChatgpt(text)
            api == "77hub" -> translateUse77hub(text, "vocabulary", 0)
            else -> translateUseBaidu(text)
        }
    }

    fun translateFixed(text: String): String {
        return translateUse77hub(text, "vocabulary", 0)
    }

    fun printUse(): LangTranslater {
        printUse = true
        return this
    }

    fun batch(): LangTranslater {
        batch = true
        return this
    }

    private fun translateUseGoogle(text: String): String {
        val key = "AIzaSyBuRCQkN72SAkmQ0CT3fK4mJIEg_ZCqUd8"
        val params = "q=${text}&source=zh&target=en&format=text&key=${key}"
        val url = "https://translation.googleapis.com/language/translate/v2?${params}"

        return runCatching {
            OkHttpClientUtils().get(url) {
                val translatedText = JsonParser.parseString(it.string()).getValue("data.translations[0].translatedText")
                return@get if (translatedText != null) {
                    if (printUse) GitCmd.log(project, "使用谷歌翻译【${text}】:【${translatedText.asString}】", !batch)
                    translatedText.asString.apply {
                        cache[text] = it.toString()
                    }
                } else {
                    GitCmd.log(project, "使用谷歌翻译【${text}】:出现错误【${it.string()}】", !batch)
                    logger.error(it.string())
                    translateUseBaidu(text)
                }
            }
        }.getOrElse { translateUseBaidu(text) }
    }

    private fun translateUseBaidu(text: String): String {
        Thread.sleep(13)
        val appId = "20240513002050659"
        val appKey = "Y6ZoTVT8oDBsF_MzBcIE"
        val salt = System.currentTimeMillis().toString()
        val sign = md5("$appId$text$salt$appKey")
        val params = "q=${text}&from=zh&to=en&appid=${appId}&salt=${salt}&sign=${sign}"
        val url = "https://api.fanyi.baidu.com/api/trans/vip/translate?${params}"

        return runCatching {
            return OkHttpClientUtils().get(url) {
                val dst = JsonParser.parseString(it.string()).getValue("trans_result[0].dst")
                return@get if (dst != null) {
                    if (printUse) GitCmd.log(project, "使用百度翻译【${text}】:【${dst.asString}】", !batch)
                    dst.asString.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    }.apply {
                        cache[text] = it.toString()
                    }.replace("% s", "%s")
                } else {
                    logger.error(it.string())
                    GitCmd.log(project, "使用百度翻译【${text}】:出现错误【${it.string()}】", !batch)
                    text
                }
            }
        }.getOrElse { text }
    }

    private fun translateUseChatgpt(text: String): String {
        val url = "https://api.chatanywhere.tech/v1/chat/completions"
        val key = ToolSettingsState.instance.chatgptKey
        val headers: Headers = Headers.Builder().add("Content-Type", "application/json").add("Authorization", "Bearer $key").build()
        // 构建请求体
        val params = "{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\",\"content\": \"翻译:${text}\"}]}"
        val request = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params)
        return runCatching {
            return OkHttpClientUtils().post(url, headers, request) {
                val content = JsonParser.parseString(it.string()).getValue("choices[0].message.content")
                return@post if (content != null) {
                    if (printUse) GitCmd.log(project, "使用chatgpt翻译【${text}】:【${content.asString}】", !batch)
                    content.asString.replace("\"", "").apply {
                        cache[text] = it.toString()
                    }
                } else {
                    logger.error(it.string())
                    GitCmd.log(project, "使用chatgpt翻译【${text}】:出现错误【${it.string()}】", !batch)
                    text
                }
            }
        }.getOrElse {
            it.message?.also { error ->
                GitCmd.log(project, error, !batch)
            }
            text
        }
    }

    private fun translateUse77hub(text: String, source: String, retry: Int): String {
        if (retry > 3) return ""
        val url = "http://52.83.252.105:3000/api/v1/chat/completions"
        val key = if (source == "vocabulary") {
            "fastgpt-JQfCEvQpN0j8jTkXB3ulh3pGtp67ulHEnVKaEmGd8gZZW0lnLC0JYja"
        } else {
            "fastgpt-scYdy1EwipUsSkAQZTpqr50UnDzfxC5BQdFNKcAsNzzCgEetoYjU"
        }
        val headers: Headers = Headers.Builder().add("Content-Type", "application/json").add("Authorization", "Bearer $key").build()
        val params = mapOf("stream" to false, "detail" to false, "chatId" to "", "variables" to mapOf("textType" to "data", "translateFormat" to "JSON"), "messages" to listOf(mapOf("content" to text, "role" to "user")))
        val requestBody = gson.toJson(params)
        val request = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody)
        return runCatching {
            return OkHttpClientUtils().connectTimeout(20, TimeUnit.SECONDS).post(url, headers, request) {
                val response = it.string().ifBlank { """{"choices":[]}""" }
                JsonParser.parseString(response).asJsonObject.get("choices").asJsonArray.filter { f ->
                    val role = f.getValue("message.role") ?: return@filter false
                    role.asString == "assistant"
                }.mapNotNull { m ->
                    val content = m.getValue("message.content") ?: return@mapNotNull null
                    if (source == "vocabulary") {
                        JsonParser.parseString(content.asString).asJsonArray.filter { f -> f.asJsonObject.get("q").asString == text }.firstNotNullOfOrNull { v -> v.getValue("a")?.asString }
                    } else {
                        JsonParser.parseString(content.asString.replace("```", "").replace("json", "")).getValue("english")?.asString
                    }?.apply {
                        if (printUse) GitCmd.log(project, "使用企企翻译助手翻译【${text}】:【${this}】", !batch)
                    }
                }.firstOrNull() ?: if (source == "vocabulary") {
                    translateUse77hub(text, "translate", retry = (retry + 1))
                } else {
                    if (printUse) GitCmd.log(project, "使用企企翻译助手翻译【${text}】:出现错误【${it.string()}】", !batch)
                    ""
                }
            }
        }.getOrElse {
            it.message?.also { error ->
                GitCmd.log(project, "使用企企翻译助手翻译【${text}】:出现错误【${error}】", !batch)
            }
            Thread.sleep(5000)  // 暂停5s继续翻译
            translateUse77hub(text, source, retry = (retry + 1))
        }
    }

    private fun translateUseYoudao(text: String): String {
        Thread.sleep(133)
        val appId = "4e9db46185880163"
        val privateKey = "tGpgvOiwE8PL7517GQSFacmUTJIPhKnD"
        val salt = UUID.randomUUID().toString()
        val sign = generateSign(text, salt, appId, privateKey)
        val url = "https://openapi.youdao.com/api"
        val formBody = FormBody.Builder()
            .add("q", text)
            .add("from", "auto")
            .add("to", "auto")
            .add("appKey", appId)
            .add("salt", salt)
            .add("sign", sign)
            .build()
        return runCatching {
            OkHttpClientUtils().post(url, formBody) {
                val ret = JsonParser.parseString(it.string()).getValue("translation[0]")
                return@post if (ret != null) {
                    if (printUse) GitCmd.log(project, "使用有道翻译【${text}】:【${ret.asString}】", !batch)
                    ret.asString.apply {
                        cache[text] = it.toString()
                    }
                } else {
                    logger.error(it.string())
                    GitCmd.log(project, "使用有道翻译【${text}】:出现错误【${it.string()}】", !batch)
                    translateUseYoudao(text)
                }
            }
        }.getOrElse { text }
    }

    private fun canProxy(): Boolean {
        return runCatching {
            val test = "https://translate.google.com/"
            OkHttpClientUtils().connectTimeout(1, TimeUnit.SECONDS).get(test) {
                true
            }
        }.getOrElse { false }
    }

    private fun generateSign(input: String, salt: String, appKey: String, appSecret: String): String {
        val data = appKey + input + salt + appSecret
        return md5(data)
    }

    private fun md5(input: String): String {
        val md = java.security.MessageDigest.getInstance("MD5")
        val byteArray = input.toByteArray()
        val mdBytes = md.digest(byteArray)
        return mdBytes.joinToString("") { "%02x".format(it) }
    }
}