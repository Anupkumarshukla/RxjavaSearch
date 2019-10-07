package com.anupkumar.rxjavasearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var searchView: SearchView
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchView = findViewById(R.id.searchView)
        textViewResult = findViewById(R.id.textViewResult)
        setUpSearchObservable()
    }



    fun SearchView.getQueryTextChangeObservable(): Observable<String> {

        val subject = PublishSubject.create<String>()

        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                subject.onComplete()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                subject.onNext(newText)
                return true
            }
        })

        return subject

    }

    private fun setUpSearchObservable() {

        searchView.getQueryTextChangeObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { text ->
                if (text.isEmpty()) {
                    textViewResult.text = ""
                    return@filter false
                } else {
                    return@filter true
                }
            }
            .distinctUntilChanged()
            .switchMap { query ->
                dataFromNetwork(query)
                    .doOnError {
                        // handle error
                    }
                    .onErrorReturn { "" }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                textViewResult.text = result
            }
    }

    private fun dataFromNetwork(query: String): Observable<String> {
        return Observable.just(true)
            .delay(2, TimeUnit.SECONDS)
            .map {
                query
            }
    }

}
