package com.sunsettv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Episode(val num: Int, val title: String, val thumb: String?, val url: String)
data class Season(val number: Int, val episodes: List<Episode>)
data class Detail(
    val backdrop: String?, val poster: String?, val rating: String, val genre: String,
    val year: String, val plot: String, val seasons: List<Season> = emptyList()
)

private fun JSONObject.str(vararg k: String): String =
    k.firstNotNullOfOrNull { optString(it).takeIf { s -> s.isNotBlank() && s != "null" } } ?: ""

private fun JSONObject.img(k: String): String? =
    (optJSONArray(k)?.optString(0) ?: optString(k)).takeIf { it.isNotBlank() && it != "null" }

/** Busca sinopse, nota, gênero, ano, backdrop e (para séries) temporadas/episódios via Xtream. */
suspend fun Xtream.detail(m: Media): Detail = withContext(Dispatchers.IO) {
    if (!m.series) {
        val i = JSONObject(httpGet("$api&action=get_vod_info&vod_id=${m.id}")).optJSONObject("info") ?: JSONObject()
        Detail(i.img("backdrop_path"), i.str("movie_image", "cover_big").ifBlank { null }, i.str("rating"), i.str("genre"),
            i.str("releasedate", "releaseDate", "release_date").take(4), i.str("plot", "description"))
    } else {
        val root = JSONObject(httpGet("$api&action=get_series_info&series_id=${m.id}"))
        val i = root.optJSONObject("info") ?: JSONObject()
        val eps = root.optJSONObject("episodes")
        val seasons = eps?.keys()?.asSequence()?.mapNotNull { k ->
            val n = k.toIntOrNull() ?: return@mapNotNull null
            val a = eps.getJSONArray(k)
            Season(n, (0 until a.length()).map {
                val e = a.getJSONObject(it)
                Episode(
                    e.optInt("episode_num", it + 1), e.optString("title").ifBlank { "Episódio ${it + 1}" },
                    e.optJSONObject("info")?.optString("movie_image")?.ifBlank { null },
                    "$base/series/$user/$pass/${e.get("id")}.${e.optString("container_extension", "mp4")}"
                )
            })
        }?.sortedBy { it.number }?.toList() ?: emptyList()
        Detail(i.img("backdrop_path"), i.str("cover").ifBlank { null }, i.str("rating"), i.str("genre"),
            i.str("releaseDate", "release_date", "releasedate").take(4), i.str("plot"), seasons)
    }
}
