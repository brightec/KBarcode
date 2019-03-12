package uk.co.brightec.kbarcode.app.viewfinder

/**
 * A generic class that describes some data with a status
 * Inspired by:
 * [Architecture Guide](https://developer.android.com/topic/libraries/architecture/guide.html#addendum)
 * [Github](https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample)
 */
@Suppress("unused")
sealed class Resource<T> {

    data class Loading<T>(val data: T?) : Resource<T>()

    data class Error<T>(val error: Exception, val data: T?) : Resource<T>()

    data class Success<T>(val data: T) : Resource<T>()
}
