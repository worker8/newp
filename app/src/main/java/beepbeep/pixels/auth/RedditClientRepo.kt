import android.util.Log
import beepbeep.pixels.BuildConfig
import io.reactivex.Observable
import net.dean.jraw.RedditClient
import net.dean.jraw.http.UserAgent
import net.dean.jraw.http.oauth.Credentials
import net.dean.jraw.http.oauth.OAuthData
import java.util.*

class RedditClientRepo {
    companion object {
        fun createGuestRedditClient(): Observable<RedditClient> {
            return Observable.create<RedditClient> { emitter ->
                val userAgent = UserAgent.of(
                        "android",
                        BuildConfig.APPLICATION_ID,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.REDDIT_DEVELOPER_NAME)
                val redditClient = RedditClient(userAgent)
                val credentials = Credentials.userlessApp(BuildConfig.REDDIT_CLIENT_ID, UUID.randomUUID())
                val authData: OAuthData = redditClient.oAuthHelper.easyAuth(credentials)
                Log.d("ddw","[authData]: ${authData}, expirationDate: ${authData.expirationDate}")
                redditClient.authenticate(authData)
                emitter.onNext(redditClient)
                emitter.onComplete()
            }
        }
    }
}