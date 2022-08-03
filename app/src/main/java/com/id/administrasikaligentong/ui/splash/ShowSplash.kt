package com.id.administrasikaligentong.ui.splash

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class ShowSplash : ActivityResultContract<Void?, Unit>() {
    override fun createIntent(context: Context, input: Void?): Intent =
        Intent(context, SplashActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?) = Unit
}