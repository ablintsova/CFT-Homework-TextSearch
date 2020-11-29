package com.example.textsearch

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var tvStory: TextView
    private lateinit var searchView: SearchView
    private lateinit var searchObservable: Observable<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTextViews()
        setSearchView()
        setSearchObservable()
    }

    private fun setSearchView() {
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(p0: String?): Boolean {
                searchObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(getObserver())
                return true
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }
        })
    }

    private fun setSearchObservable() {
        searchObservable = Observable.create { emitter ->
            emitter.onNext(searchView.query.toString())
            emitter.onComplete()
        }
        searchObservable = searchObservable.debounce(5000, TimeUnit.MILLISECONDS)
        //searchObservable = observable.debounce(5000, TimeUnit.MILLISECONDS)
    }

    private fun getObserver() = object : DisposableObserver<String>() {
        override fun onComplete() {
            Log.d("observer", "onComplete")
        }

        override fun onNext(query: String) {
            val count = countOccurrencesOf(query)
            updateResult(count)
        }

        override fun onError(e: Throwable) {
            Log.e("observer", "onError: ${e.message}")
        }
    }

    private fun setTextViews() {
        tvResult = findViewById(R.id.tvResult)
        tvStory = findViewById(R.id.tvStory)
        tvStory.text = Story.TEXT
    }


    private fun updateResult(count: Int) {
        val str = getString(R.string.result) + " " + count
        tvResult.text = str
    }

    private fun countOccurrencesOf(query: String): Int {
        if (query.isBlank()) return 0
        val text = Story.TEXT.toLowerCase(Locale.ROOT)
        var count = 0
        if (text.contains(query)) {
            val regex = query.toLowerCase(Locale.ROOT).toRegex()
            val result = regex.findAll(text)
            count = result.count()
        }
        return count
    }
}