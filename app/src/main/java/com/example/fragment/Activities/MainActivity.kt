package com.example.fragment.Activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.widget.Toast
import com.example.fragment.Fragments.PokemonDetailsFragment
import com.example.fragment.Fragments.PokemonsListFragment
import com.example.fragment.Models.Pokemon
import com.example.fragment.Networks.NetworkUtils
import com.example.fragment.R
import com.example.fragment.Utils.AppConstants
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity(), PokemonsListFragment.ListenerTools{

    private lateinit var mainFragment : PokemonsListFragment
    private lateinit var mainContentFragment: PokemonDetailsFragment

    private var pokemonList = ArrayList<Pokemon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pokemonList = savedInstanceState?.getParcelableArrayList(AppConstants.dataset_saveinstance_key) ?: ArrayList()

        initMainFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(AppConstants.dataset_saveinstance_key, pokemonList)
        super.onSaveInstanceState(outState)
    }

    fun initMainFragment(){
        mainFragment = PokemonsListFragment.newInstance(pokemonList)

        val resource = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            R.id.main_fragment
        else {
            mainContentFragment = PokemonDetailsFragment.newInstance(Pokemon(0,
                                                                                R.string.n_a_value.toString(),
                                                                                R.string.n_a_value.toString(),
                                                                                R.string.n_a_value.toString(),
                                                                                R.string.n_a_value.toString(),
                                                                                R.string.n_a_value.toString(),
                                                                                R.string.n_a_value.toString(),
                                                                                R.string.n_a_value.toString()))
            changeFragment(R.id.land_main_cont_fragment, mainContentFragment)

            R.id.land_main_fragment
        }

        changeFragment(resource, mainFragment)

        FetchPokemonTask().execute("")
    }

    fun addPokemonToList(pokemon: Pokemon) {
        pokemonList.add(pokemon)
        mainFragment.updatePokemonList(pokemonList)
        Log.d("Number", pokemonList.size.toString())
    }

    override fun searchPokemon(pokemonName: String) {
        QueryPokemonTask().execute(pokemonName)
    }

    override fun managePortraitItemClick(item: Pokemon) {
        //val pokemonBundle = Bundle()
        //pokemonBundle.putParcelable("MOVIE", pokemon)
        //startActivity(Intent(this, MovieViewerActivity::class.java).putExtras(movieBundle))

        Toast.makeText(this@MainActivity, "Click en Portrait", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, PokemonViewer::class.java).putExtra("URL", item.url))

    }

    private fun changeFragment(id: Int, frag: Fragment){ supportFragmentManager.beginTransaction().replace(id, frag).commit() }

    override fun manageLandscapeItemClick(item: Pokemon) {
        Toast.makeText(this@MainActivity, "Click en Landscape", Toast.LENGTH_LONG).show()

        FetchPokemonDetailsTask().execute(item.url)
    }

    private inner class FetchPokemonTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg query: String): String {

            if (query.isNullOrEmpty()) return ""

            val ID = query[0]
            val pokeAPI = NetworkUtils().buildUrl("pokemon",ID)

            return try {
                NetworkUtils().getResponseFromHttpUrl(pokeAPI)
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }

        }

        override fun onPostExecute(pokemonInfo: String) {
            val pokemons = if (!pokemonInfo.isEmpty()) {
                val root = JSONObject(pokemonInfo)
                val results = root.getJSONArray("results")
                MutableList(20) { i ->
                    val result = JSONObject(results[i].toString())
                    Pokemon(i,
                        result.getString("name").capitalize(),
                        result.getString("url"),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString())
                }
            } else {
                MutableList(20) {i->
                    Pokemon(i,
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString())
                }
            }

            for (pokemon in pokemons) {
                addPokemonToList(pokemon)
            }
        }
    }

    private inner class QueryPokemonTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg query: String): String {

            if (query.isNullOrEmpty()) return ""

            val pokemonName = query[0]
            val pokeAPI = NetworkUtils().buildUrl("type",pokemonName)

            return try {
                NetworkUtils().getResponseFromHttpUrl(pokeAPI)
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }

        }

        override fun onPostExecute(pokemonInfo: String) {
            val pokemons = if (!pokemonInfo.isEmpty()) {
                val root = JSONObject(pokemonInfo)
                val results = root.getJSONArray("pokemon")
                MutableList(20) { i ->
                    val resulty = JSONObject(results[i].toString())
                    val result = JSONObject(resulty.getString("pokemon"))

                    Pokemon(i,
                        result.getString("name").capitalize(),
                        result.getString("url"),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString())
                }
            } else {
                MutableList(20) {i ->
                    Pokemon(i,
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString(),
                        R.string.n_a_value.toString())
                }
            }
            for (pokemon in pokemons) {
                addPokemonToList(pokemon)
            }
        }
    }

    private inner class FetchPokemonDetailsTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg query: String): String {

            if (query.isNullOrEmpty()) return ""

            val url = query[0]
            val pokeAPI = Uri.parse(url).buildUpon().build()
            val finalurl = try {
                URL(pokeAPI.toString())
            } catch (e: MalformedURLException){
                URL("")
            }

            return try {
                NetworkUtils().getResponseFromHttpUrl(finalurl)
            } catch (e: IOException) {
                e.printStackTrace()
                ""
            }

        }

        override fun onPostExecute(pokemonInfo: String) {
            val pokemon = if (!pokemonInfo.isEmpty()) {
                val root = JSONObject(pokemonInfo)
                val sprites = root.getString("sprites")
                val types = root.getJSONArray("types")
                val fsttype = JSONObject(types[0].toString()).getString("type")
                val sndtype = try { JSONObject(types[1].toString()).getString("type") }catch (e: JSONException){ "" }

                Pokemon(root.getInt("id"),
                    root.getString("name").capitalize(),
                    root.getString("location_area_encounters"),
                    JSONObject(fsttype).getString("name").capitalize(),
                    if(sndtype.isEmpty()) " " else JSONObject(sndtype).getString("name").capitalize(),
                    root.getString("weight"), root.getString("height"),
                    JSONObject(sprites).getString("front_default"))

            } else {
                Pokemon(0, R.string.n_a_value.toString(), R.string.n_a_value.toString(), R.string.n_a_value.toString(),R.string.n_a_value.toString(), R.string.n_a_value.toString(), R.string.n_a_value.toString(), R.string.n_a_value.toString())
            }

            mainContentFragment = PokemonDetailsFragment.newInstance(pokemon)
            changeFragment(R.id.land_main_cont_fragment, mainContentFragment)
        }
    }
}
