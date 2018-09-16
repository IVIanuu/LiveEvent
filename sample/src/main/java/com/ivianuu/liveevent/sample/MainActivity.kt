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
            Log.d("NavigationCall", "on event")
        }
    }
}

class MyViewModel : ViewModel() {

    val navigationCall: LiveEvent<Unit>
        get() = _navigationCall
    private val _navigationCall = MutableLiveEvent<Unit>()

    init {
        _navigationCall.offer(Unit)
        _navigationCall.offer(Unit)
        _navigationCall.offer(Unit)
        _navigationCall.offer(Unit)

        Handler().postDelayed({
            _navigationCall.offer(Unit)
            _navigationCall.offer(Unit)
        }, 6000)
    }

}