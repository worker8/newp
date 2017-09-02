package beepbeep.rxjraw

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import net.dean.jraw.RedditClient
import net.dean.jraw.http.HttpAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.http.oauth.OAuthData

class RxRedditClient {
    val redditClient: RedditClient

    constructor(userAgent: UserAgent) {
        redditClient = RedditClient(userAgent)
    }

    constructor(userAgent: UserAgent, adapter: HttpAdapter<Any>) {
        redditClient = RedditClient(userAgent, adapter)
    }

    fun authenticate(authData: OAuthData): Single<Unit> {
        return Single.create<Unit> { emitter ->
            try {
                redditClient.authenticate(authData)
            } catch (exception: Exception) {
                emitter.onError(exception)
            }
        }.subscribeOn(Schedulers.io())
    }
}