package com.id.administrasikaligentong.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.id.administrasikaligentong.content.Data
import com.id.administrasikaligentong.databinding.ActivityMainBinding
import com.id.administrasikaligentong.ui.form.FormActivity
import com.id.administrasikaligentong.ui.splash.ShowSplash
import com.id.administrasikaligentong.util.DocumentListAdapter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<MainViewModel>()
    private val showSplash = registerForActivityResult(ShowSplash()) {
        viewModel.toggleShouldShowSplash(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.shouldShowSplash
            .onEach { shouldShowSplash -> if (shouldShowSplash) showSplash.launch() }
            .launchIn(lifecycleScope)

        with(binding.rvSuratKeterangan) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = DocumentListAdapter(Data.suratKeteranganList)
                .setItemOnClickListener {
                    startActivity(Intent(this@MainActivity, FormActivity::class.java)
                        .apply { putExtra(FormActivity.EXTRA_PARCELABLE_DOCUMENT, it) }
                    )
                }
        }

        with(binding.rvSuratPengantar) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = DocumentListAdapter(Data.suratPengantarList)
                .setItemOnClickListener {
                    startActivity(Intent(this@MainActivity, FormActivity::class.java)
                        .apply { putExtra(FormActivity.EXTRA_PARCELABLE_DOCUMENT, it) }
                    )
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}