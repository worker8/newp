package beepbeep.pixels.auth

import beepbeep.pixels.BuildConfig
import net.dean.jraw.http.UserAgent

class UserAgentRepo {
    companion object {
        fun createUserAgent(): UserAgent {
            return UserAgent.of(
                    "android",
                    BuildConfig.APPLICATION_ID,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.REDDIT_DEVELOPER_NAME)
        }
    }
}