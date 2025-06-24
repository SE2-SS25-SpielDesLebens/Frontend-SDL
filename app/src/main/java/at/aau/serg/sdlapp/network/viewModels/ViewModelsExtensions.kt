package at.aau.serg.sdlapp.network.viewModels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun ComponentActivity.getSharedViewModel(): ConnectionViewModel {
    return ViewModelProvider(
        (application as MyViewModelStore).sharedViewModelStore,
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ConnectionViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
            }
        }
    )[ConnectionViewModel::class.java]
}
