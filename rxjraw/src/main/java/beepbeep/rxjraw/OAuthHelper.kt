package beepbeep.rxjraw

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import net.dean.jraw.RedditClient
import net.dean.jraw.http.NetworkException
import net.dean.jraw.http.oauth.Credentials
import net.dean.jraw.http.oauth.OAuthData
import net.dean.jraw.http.oauth.OAuthHelper

class RxOAuthHelper(redditClient: RedditClient) {
    private val oAuthHelper = OAuthHelper(redditClient)

    fun easyAuth(creds: Credentials): Single<OAuthData> {
        return Single.create<OAuthData> { emitter ->
            try {
                emitter.onSuccess(oAuthHelper.easyAuth(creds))
            } catch (exception: NetworkException) {
                emitter.onError(exception)
            }
        }.subscribeOn(Schedulers.io())
    }
}