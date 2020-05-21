package cforbes1.uw.edu.sunspotter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.announcement_layout.*
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.android.synthetic.main.weather_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isSunny: Boolean = false
    private var timeOfSun: String = ""
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        announcement.visibility = View.INVISIBLE
        searchButtonClicked()
        if (savedInstanceState != null) {
            val query = savedInstanceState.getString("query")
            if (query != null) {
                searchQuery = query
                getWeatherData(searchQuery)
                announcement.visibility = View.VISIBLE
            }
        }

    }

    private fun searchButtonClicked() {
        search_button.setOnClickListener {
            val searchBar = findViewById<View>(R.id.search_bar) as EditText
            searchQuery = searchBar.text.toString()
            if (searchQuery != "") {
                getWeatherData(searchQuery)
                announcement.visibility = View.VISIBLE
            } else {
                Toast.makeText(applicationContext, "Please enter in a Zip Code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeatherData(query: String) {
        isSunny = false
        val queue = WeatherData.weatherDataRequestQueue(applicationContext)

        // create url with queries and api
        var url = Uri.Builder().scheme("https").authority("api.openweathermap.org")
            .appendPath("data")
            .appendPath("2.5")
            .appendPath("forecast")
            .appendQueryParameter("zip", query)
            .appendQueryParameter("units", "imperial")
            .appendQueryParameter("appid", getString(R.string.OPEN_WEATHER_MAP_API_KEY))
            .toString()

        // create a request to get a json
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener{ response ->
                val forecastList = parseForecastJsonData(response.toString())
                if (forecastList != null) {
                    val forecastAdapter = ForecastAdapter(applicationContext, forecastList)
                    weather_list.adapter = forecastAdapter
                    if (isSunny) {
                        sun_info.text = getString(R.string.sun_true)
                        time.text = timeOfSun
                        image.setImageResource(R.drawable.ic_check_circle_black_24dp)
                        image.setColorFilter(Color.GREEN)
                    } else {
                        sun_info.text = getString(R.string.sun_false)
                        time.text = getString(R.string.no_sun_time)
                        image.setImageResource(R.drawable.ic_highlight_off_black_24dp)
                        image.setColorFilter(Color.RED)
                    }
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, error.networkResponse.statusCode.toString() + ": Invalid Zip Code", Toast.LENGTH_SHORT).show()
            })
        queue?.add(jsonObjectRequest)
    }

    private fun parseForecastJsonData(json: String): List<ForecastData>? {
        val forecastList = mutableListOf<ForecastData>()

        try {
            val forecastJsonArray = JSONObject(json).getJSONArray("list")
            for (i in 0 until forecastJsonArray.length()) {
                val forecastJsonObject = forecastJsonArray.getJSONObject(i)
                val forecast = ForecastData()
                val jsonWeather = forecastJsonObject.getJSONArray("weather").getJSONObject(0)

                // Getting the icon drawable int reference
                val icon = "icon" + jsonWeather.getString("icon")
                val iconId: Int = resources.getIdentifier(icon, "drawable", this.packageName)

                val weather = jsonWeather.getString("main")
                val time = convertUNIXDate(forecastJsonObject.getString("dt"))


                forecast.weather = weather
                forecast.time = time
                forecast.temperature = "(" + forecastJsonObject.getJSONObject("main").getString("temp") + "°F)"
                forecast.icon = ContextCompat.getDrawable(applicationContext, iconId) as Drawable

                findSun(weather, time)

                forecastList.add(forecast)
            }
        } catch (e: JSONException) {
            Log.e("parseForecastJsonData", "Error parsing json", e)
            return null
        }
        return forecastList
    }

    private fun findSun(weather: String, time: String) {
        if (!isSunny) {
            if (weather == "Clear") {
                isSunny = true
                timeOfSun = "At $time"
            }
        }
    }

    private fun convertUNIXDate(time: String): String {
        val sdf = SimpleDateFormat("EEE, h:mm a")
        val date = Date(time.toLong() * 1000)
        return sdf.format(date)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("query", searchQuery)
    }


}

data class ForecastData(
    var weather: String? = null,
    var time: String? = null,
    var temperature: String? = null,
    var icon: Drawable? = null
)

class ForecastAdapter(context: Context, objects: List<ForecastData>) : ArrayAdapter<ForecastData>(context, R.layout.list_item, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        var forecast = getItem(position)
        var holder = ViewHolder()

        // Checks if the View has been created
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            holder.icon = view.findViewById(R.id.icon)
            holder.temperature = view.findViewById(R.id.temperature)
            holder.time = view.findViewById(R.id.time)
            holder.weather = view.findViewById(R.id.weather)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        holder.weather?.text = forecast?.weather
        holder.time?.text = forecast?.time
        holder.temperature?.text = forecast?.temperature
        holder.icon?.setImageDrawable(forecast?.icon)

        return view as View
    }
}

private class ViewHolder {
    var weather: TextView? = null
    var time: TextView? = null
    var temperature: TextView? = null
    var icon: ImageView? = null
}

class WeatherData {
    companion object {
        private var queue: RequestQueue? = null

        fun weatherDataRequestQueue(context: Context) : RequestQueue? {
            queue = Volley.newRequestQueue(context)
            return queue
        }
    }


}
