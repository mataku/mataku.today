import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise
import kotlin.js.json

private external interface Env {
    val ASSETS: AssetsFetcher
}

private external interface AssetsFetcher {
    fun fetch(input: Request): Promise<Response>
}

private sealed class Route {
    object RobotsTxt : Route()
    object SitemapXml : Route()
    object Index : Route()
    data class Asset(val key: String) : Route()
    data class Article(val datePath: String) : Route()
    data class ArticleAsset(val datePath: String, val filename: String) : Route()
    data class Page(val num: String) : Route()
    object PrivacyPolicy : Route()
    object NotFound : Route()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun fetch(request: Request, env: dynamic): Promise<Response> {
    val url = js("new URL(request.url)")
    val pathname = (url.pathname as String).removePrefix("/")
    val origin = url.origin as String
    val route = resolveRoute(pathname)
    return handleRoute(route, env, origin)
}

private fun notFoundHandler(env: Env, origin: String): Promise<Response> {
    val headers = buildHeaders(
        contentType = "text/html; charset=utf-8",
        cacheControl = "public, max-age=300"
    )
    return env.ASSETS.fetch(Request("$origin/404.html")).then { response: Response ->
        if (!response.ok) {
            Response("Not Found", ResponseInit(status = 404, headers = headers))
        } else {
            Response(response.body, ResponseInit(status = 404, headers = headers))
        }
    }
}

private fun handleRoute(route: Route, env: dynamic, origin: String): Promise<Response> {
    val typedEnv = env.unsafeCast<Env>()
    return when (route) {
        is Route.RobotsTxt -> robotsTxtHandler()
        is Route.SitemapXml -> sitemapXmlHandler()
        is Route.NotFound -> notFoundHandler(typedEnv, origin)
        is Route.Index -> indexHandler(typedEnv, origin)
        is Route.Asset -> assetHandler(route.key, typedEnv, origin)
        is Route.Article -> articleHandler(route.datePath, typedEnv, origin)
        is Route.ArticleAsset -> articleAssetHandler(route.datePath, route.filename, typedEnv, origin)
        is Route.Page -> pageHandler(route.num, typedEnv, origin)
        is Route.PrivacyPolicy -> privacyPolicyHandler(typedEnv, origin)
    }
}

private fun indexHandler(env: Env, origin: String): Promise<Response> {
    return fetchFromAssets("index.html", env, origin)
}

private fun assetHandler(key: String, env: Env, origin: String): Promise<Response> {
    return fetchFromAssets(key, env, origin)
}

private fun articleHandler(datePath: String, env: Env, origin: String): Promise<Response> {
    return fetchFromAssets("$datePath/index.html", env, origin)
}

private fun articleAssetHandler(datePath: String, filename: String, env: Env, origin: String): Promise<Response> {
    return fetchFromAssets("$datePath/$filename", env, origin)
}

private fun pageHandler(num: String, env: Env, origin: String): Promise<Response> {
    return fetchFromAssets("page/$num/index.html", env, origin)
}

private fun privacyPolicyHandler(env: Env, origin: String): Promise<Response> {
    return fetchFromAssets("privacy_policy.html", env, origin)
}

private fun fetchFromAssets(key: String, env: Env, origin: String): Promise<Response> {
    val contentType = contentTypeFor(key)
    if (contentType == null) {
        return notFoundHandler(env, origin)
    }
    return env.ASSETS.fetch(Request("$origin/$key")).then { response: Response ->
        if (!response.ok) {
            notFoundHandler(env, origin)
        } else {
            val headers = buildHeaders(
                contentType = contentType,
                cacheControl = cacheControlFor(key)
            )
            Promise.resolve(Response(response.body, ResponseInit(headers = headers)))
        }
    }.asDynamic().unsafeCast<Promise<Response>>()
}

private val datePattern = Regex("^(\\d{4}/\\d{2}/\\d{2})/?(?:index\\.html)?$")
private val articleAssetPattern = Regex("^(\\d{4}/\\d{2}/\\d{2})/([^/]+\\.[a-zA-Z0-9]+)$")
private val pagePattern = Regex("^page/(\\d+)/?(?:index\\.html)?$")

private fun resolveRoute(pathname: String): Route {
    if (pathname.contains("..") || pathname.contains("//")) return Route.NotFound

    val decoded = try {
        js("decodeURIComponent(pathname)") as String
    } catch (e: Throwable) {
        return Route.NotFound
    }
    if (decoded.contains("..") || decoded.contains("//")) return Route.NotFound

    datePattern.matchEntire(pathname)?.let { match ->
        return Route.Article(match.groupValues[1])
    }

    articleAssetPattern.matchEntire(pathname)?.let { match ->
        return Route.ArticleAsset(match.groupValues[1], match.groupValues[2])
    }

    pagePattern.matchEntire(pathname)?.let { match ->
        return Route.Page(match.groupValues[1])
    }

    return when {
        pathname == "robots.txt" -> Route.RobotsTxt
        pathname == "sitemap.xml" -> Route.SitemapXml
        pathname.isEmpty() -> Route.Index
        pathname == "feed.xml" -> Route.Asset(pathname)
        pathname.startsWith("assets/") -> Route.Asset(pathname)
        pathname.startsWith("images/") -> Route.Asset(pathname)
        pathname == "privacy_policy" || pathname == "privacy_policy.html" -> Route.PrivacyPolicy
        else -> Route.NotFound
    }
}

private fun contentTypeFor(filename: String): String? {
    return when {
        filename.endsWith(".html") -> "text/html; charset=utf-8"
        filename.endsWith(".css") -> "text/css; charset=utf-8"
        filename.endsWith(".js") -> "application/javascript; charset=utf-8"
        filename.endsWith(".json") -> "application/json; charset=utf-8"
        filename.endsWith(".gif") -> "image/gif"
        filename.endsWith(".png") -> "image/png"
        filename.endsWith(".jpg") || filename.endsWith(".jpeg") -> "image/jpeg"
        filename.endsWith(".ico") -> "image/x-icon"
        filename.endsWith(".xml") -> "application/rss+xml; charset=utf-8"
        else -> null
    }
}

private fun robotsTxtHandler(): Promise<Response> {
    val body = """
        User-agent: *
        Allow: /
        Sitemap: https://mataku.today/sitemap.xml
    """.trimIndent()
    val headers = buildHeaders(
        contentType = "text/plain; charset=utf-8",
        cacheControl = "public, max-age=86400"
    )
    return Promise.resolve(Response(body, ResponseInit(headers = headers)))
}

private fun sitemapXmlHandler(): Promise<Response> {
    val body = """
        <?xml version="1.0" encoding="UTF-8"?>
        <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
          <url>
            <loc>https://mataku.today/</loc>
          </url>
          <url>
            <loc>https://mataku.today/feed.xml</loc>
          </url>
        </urlset>
    """.trimIndent()
    val headers = buildHeaders(
        contentType = "application/xml; charset=utf-8",
        cacheControl = "public, max-age=86400"
    )
    return Promise.resolve(Response(body, ResponseInit(headers = headers)))
}

private fun buildHeaders(contentType: String, cacheControl: String) = json(
    "content-type" to contentType,
    "cache-control" to cacheControl,
    "X-Content-Type-Options" to "nosniff",
    "X-Frame-Options" to "DENY",
    "Referrer-Policy" to "strict-origin-when-cross-origin"
)

private fun cacheControlFor(filename: String): String {
    return when {
        filename.endsWith(".html") -> "public, max-age=86400"
        filename.endsWith(".css") || filename.endsWith(".js") -> "public, max-age=31536000, immutable"
        filename.endsWith(".gif") || filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".ico") -> "public, max-age=31536000, immutable"
        filename.endsWith(".json") -> "public, max-age=86400"
        filename.endsWith(".xml") -> "public, max-age=3600"
        else -> "public, max-age=3600"
    }
}
