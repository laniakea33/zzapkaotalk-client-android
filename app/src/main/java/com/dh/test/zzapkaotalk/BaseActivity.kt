package com.dh.test.zzapkaotalk

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity: AppCompatActivity() {

    abstract val viewModel: BaseViewModel

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
}