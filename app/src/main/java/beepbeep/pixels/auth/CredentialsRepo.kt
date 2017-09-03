package beepbeep.pixels.auth

import beepbeep.pixels.BuildConfig
import net.dean.jraw.http.oauth.Credentials
import java.util.*

class CredentialsRepo {
    companion object {
        fun createGuestCredentials(): Credentials {
            return Credentials.userlessApp(BuildConfig.REDDIT_CLIENT_ID, UUID.randomUUID())
        }
    }
}