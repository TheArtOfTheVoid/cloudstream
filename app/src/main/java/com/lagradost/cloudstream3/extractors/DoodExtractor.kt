package com.lagradost.cloudstream3.extractors

import com.lagradost.cloudstream3.network.get
import com.lagradost.cloudstream3.network.text
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import java.lang.Thread.sleep

class DoodToExtractor : DoodLaExtractor() {
    override val mainUrl: String
        get() = "https://dood.to"
}

class DoodSoExtractor : DoodLaExtractor() {
    override val mainUrl: String
        get() = "https://dood.so"
}

open class DoodLaExtractor : ExtractorApi() {
    override val name: String
        get() = "DoodStream"
    override val mainUrl: String
        get() = "https://dood.la"
    override val requiresReferer: Boolean
        get() = false

    override fun getExtractorUrl(id: String): String {
        return "$mainUrl/d/$id"
    }

    override fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val id = url.removePrefix("$mainUrl/e/").removePrefix("$mainUrl/d/")
        val trueUrl = getExtractorUrl(id)
        val response = get(trueUrl).text
        Regex("href=\".*/download/(.*?)\"").find(response)?.groupValues?.get(1)?.let { link ->
            if (link.isEmpty()) return null
            sleep(5000) // might need this to not trigger anti bot
            val downloadLink = "$mainUrl/download/$link"
            val downloadResponse = get(downloadLink).text
            Regex("onclick=\"window\\.open\\((['\"])(.*?)(['\"])").find(downloadResponse)?.groupValues?.get(2)
                ?.let { trueLink ->
                    return listOf(
                        ExtractorLink(
                            trueLink,
                            this.name,
                            trueLink,
                            mainUrl,
                            Qualities.Unknown.value,
                            false
                        )
                    ) // links are valid in 8h
                }
        }

        return null
    }
}