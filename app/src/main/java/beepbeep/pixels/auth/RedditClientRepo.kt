package beepbeep.pixels.auth

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import net.dean.jraw.RedditClient

class RedditClientRepo {
    companion object {
        fun createGuestRedditClient(): Observable<RedditClient> {
            return Observable.create<RedditClient> { emitter ->
                val userAgent = UserAgentRepo.createUserAgent()
                val credentials = CredentialsRepo.createGuestCredentials()
                val redditClient = RedditClient(userAgent).apply {
                    val oAuthData = oAuthHelper.easyAuth(credentials)
                    authenticate(oAuthData)
                }
                emitter.onNext(redditClient)
                emitter.onComplete()
            }.subscribeOn(Schedulers.io())
        }
    }
}