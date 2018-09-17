package com.ivianuu.liveevent.sample

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ivianuu.liveevent.LiveEvent
import com.ivianuu.liveevent.MutableLiveEvent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(MyViewModel::class.java)

        viewModel.navigationCall.consume(this) {
            Log.d("NavigationCall", "on event $it")
        }
    }
}

class MyViewModel : ViewModel() {

    val navigationCall: LiveEvent<Int>
        get() = _navigationCall
    private val _navigationCall = MutableLiveEvent<Int>(2)

    init {
        _navigationCall.offer(1)
        _navigationCall.offer(2)
        _navigationCall.offer(3)
        _navigationCall.offer(4)

        Handler().postDelayed({
            _navigationCall.offer(5)
            _navigationCall.offer(6)
        }, 2000)
    }

}