package at.aau.serg.sdlapp.network.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import at.aau.serg.sdlapp.network.StompConnectionManager

class ConnectionViewModel(application: Application): ViewModel() {
    val myStomp = MutableLiveData<StompConnectionManager?>()

    fun initializeStomp(callback: (String) -> Unit){
        if (myStomp.value == null){
            myStomp.value = StompConnectionManager(callback)
        }
    }
}