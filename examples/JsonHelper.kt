import android.util.Log
import org.json.JSONException
import org.json.JSONObject

/**
 * A helper class for working with JSON Strings. Now in Kotlin!
 * @author joelross
 */
object JsonHelper {

    private val TAG = "JsonHelper"

    /* A data class representing a Movie */
    data class Movie(
        var title: String? = null,
        var year: Int = 0,
        var imdbId: String? = null,
        var posterUrl: String? = null
    )

    /**
     * Parses a JSON-format String (from OMDB search results) into a list of Movie objects
     * @param json A JSON formatted String, such as
     * `
     * {"Search":[
     *   {"Title":"Star Wars: A New Hope","Year":"1977","imdbID":"tt0076759","Poster":"http://..."},
     *   {"Title":"Star Wars: The Empire Strikes Back","Year":"1980","imdbID":"tt0080684","Poster":"http://..."}
     * ]}
     * `
     * @return An List of Movie objects with title, year, imdbId, and posterUrl as specified in the input
     */
    fun parseMovieJSONData(json: String): MutableList<Movie>? {
        val movies = mutableListOf<Movie>() //empty list to return

        try {
            val moviesJsonArray = JSONObject(json).getJSONArray("Search") //get array object from "Search" key
            for (i in 0 until moviesJsonArray.length()) //iterate through array object
            {
                val movieJsonObject = moviesJsonArray.getJSONObject(i) //get ith object from array
                val movie = Movie() //make a "blank" movie
                movie.title = movieJsonObject.getString("Title") //get title from object and assign
                movie.year = Integer.parseInt(movieJsonObject.getString("Year")) //get year from object
                movie.imdbId = movieJsonObject.getString("imdbID") //get imdb from object
                movie.posterUrl = movieJsonObject.getString("Poster") //get poster from object

                movies.add(movie)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing json", e) //Android log the error
            return null
        }

        return movies
    }
}
