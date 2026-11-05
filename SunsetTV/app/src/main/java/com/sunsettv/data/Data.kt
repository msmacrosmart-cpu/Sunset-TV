package com.sunsettv.data

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.CancellationException

data class Channel(val name: String, val url: String, val logo: String?, val group: String, val id: String? = null)
data class Media(val id: String, val name: String, val poster: String?, val category: String, val url: String, val series: Boolean = false)
data class Epg(val title: String, val desc: String, val start: Long, val end: Long)
data class Library(val live: List<Channel>, val movies: List<Media>, val series: List<Media>, val api: Xtream? = null)

internal fun httpGet(u: String): String {
    val c = URL(u).openConnection() as HttpURLConnection
    c.connectTimeout = 15000; c.readTimeout = 30000
    return try {
        require(c.responseCode in 200..299) { "Servidor indisponível (HTTP ${c.responseCode})" }
        c.inputStream.bufferedReader().use { it.readText() }
    } finally { c.disconnect() }
}

object M3u {
    fun parse(text: String): List<Channel> {
        val out = mutableListOf<Channel>()
        var name = ""; var logo: String? = null; var group = "Geral"
        fun attr(l: String, k: String) = Regex("$k=\"([^\"]*)\"").find(l)?.groupValues?.get(1)?.ifBlank { null }
        text.lineSequence().map { it.trim() }.forEach { l ->
            when {
                l.startsWith("#EXTINF") -> {
                    name = l.substringAfterLast(",").trim()
                    logo = attr(l, "tvg-logo"); group = attr(l, "group-title") ?: "Geral"
                }
                l.isNotEmpty() && !l.startsWith("#") -> {
                    out += Channel(name.ifBlank { l }, l, logo, group)
                    name = ""; logo = null; group = "Geral"
                }
            }
        }
        return out
    }
}

internal fun encode(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
class Xtream(val base: String, username: String, password: String) {
    val user = encode(username)
    val pass = encode(password)
    internal val api = "$base/player_api.php?username=$user&password=$pass"
    private fun arr(a: String) = JSONArray(httpGet("$api&action=$a"))
    private fun cats(a: String): Map<String, String> {
        val c = arr(a)
        return (0 until c.length()).associate { c.getJSONObject(it).getString("category_id") to c.getJSONObject(it).getString("category_name") }
    }
    fun authOk(): Boolean {
        val info = JSONObject(httpGet(api)).optJSONObject("user_info") ?: return false
        return info.optInt("auth", 0) == 1 && info.optString("status", "Active").equals("Active", true)
    }

    fun live(): List<Channel> {
        val c = cats("get_live_categories"); val s = arr("get_live_streams")
        return (0 until s.length()).map {
            val o = s.getJSONObject(it)
            Channel(o.getString("name"), "$base/live/$user/$pass/${o.get("stream_id")}.m3u8",
                o.optString("stream_icon").ifBlank { null }, c[o.optString("category_id")] ?: "Geral", o.get("stream_id").toString())
        }
    }
    fun movies(): List<Media> {
        val c = cats("get_vod_categories"); val s = arr("get_vod_streams")
        return (0 until s.length()).map {
            val o = s.getJSONObject(it); val id = o.get("stream_id").toString()
            Media(id, o.getString("name"), o.optString("stream_icon").ifBlank { null }, c[o.optString("category_id")] ?: "Geral",
                "$base/movie/$user/$pass/$id.${o.optString("container_extension", "mp4")}")
        }
    }
    fun series(): List<Media> {
        val c = cats("get_series_categories"); val s = arr("get_series")
        return (0 until s.length()).map {
            val o = s.getJSONObject(it)
            Media(o.get("series_id").toString(), o.getString("name"), o.optString("cover").ifBlank { null },
                c[o.optString("category_id")] ?: "Geral", "", true)
        }
    }
    suspend fun epg(streamId: String): List<Epg> = withContext(Dispatchers.IO) {
        fun b64(s: String) = runCatching { String(Base64.decode(s, Base64.DEFAULT)) }.getOrDefault(s)
        val l = JSONObject(httpGet("$api&action=get_short_epg&stream_id=$streamId&limit=8")).optJSONArray("epg_listings") ?: JSONArray()
        (0 until l.length()).map {
            val o = l.getJSONObject(it)
            Epg(b64(o.optString("title")), b64(o.optString("description")),
                o.optString("start_timestamp").toLongOrNull().let { it ?: 0L } * 1000, o.optString("stop_timestamp").toLongOrNull().let { it ?: 0L } * 1000)
        }
    }
    suspend fun firstEpisode(seriesId: String): String? = withContext(Dispatchers.IO) {
        val eps = JSONObject(httpGet("$api&action=get_series_info&series_id=$seriesId")).optJSONObject("episodes") ?: return@withContext null
        val season = eps.optJSONArray(eps.keys().asSequence().minOrNull() ?: return@withContext null) ?: return@withContext null
        val e = season.optJSONObject(0) ?: return@withContext null
        "$base/series/$user/$pass/${e.get("id")}.${e.optString("container_extension", "mp4")}"
    }
}

object Repo {
    suspend fun m3u(url: String): Library = withContext(Dispatchers.IO) {
        val all = M3u.parse(httpGet(url.trim()))
        val movies = all.filter { "/movie/" in it.url }.map { Media(it.url, it.name, it.logo, it.group, it.url) }
        Library(all.filter { "/movie/" !in it.url && "/series/" !in it.url }, movies, emptyList())
    }

    suspend fun xtream(server: String, user: String, pass: String): Library = coroutineScope {
        val base = server.trim().trimEnd('/').let { if ("://" in it) it else "http://$it" }
        val parsed = java.net.URI(base)
        require(parsed.scheme in listOf("http", "https") && !parsed.host.isNullOrBlank() && parsed.rawQuery == null && parsed.rawUserInfo == null) { "URL inválida. Use http://servidor:porta" }
        val x = Xtream(base, user, pass)
        require(withContext(Dispatchers.IO) { x.authOk() }) { "Credenciais inválidas" }
        val l = async(Dispatchers.IO) { x.live() }
        val m = async(Dispatchers.IO) { x.movies() }
        val s = async(Dispatchers.IO) { x.series() }
        Library(l.await(), m.await(), s.await(), x)
    }
}
