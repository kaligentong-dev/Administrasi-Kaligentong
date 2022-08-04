package com.id.administrasikaligentong.ui.result

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import com.id.administrasikaligentong.R
import com.id.administrasikaligentong.databinding.ActivityResultBinding
import com.id.administrasikaligentong.entity.DocumentEntity
import com.id.administrasikaligentong.repository.ImpPdfRepository
import com.id.administrasikaligentong.ui.form.FormActivity
import com.id.administrasikaligentong.util.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultActivity : AppCompatActivity() {
    private var _binding: ActivityResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var document: DocumentEntity

    private val viewModel by viewModels<ResultViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ResultViewModel(document, ImpPdfRepository(application)) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        document = intent.getParcelableExtra(FormActivity.EXTRA_PARCELABLE_DOCUMENT) ?: DocumentEntity.empty()
        if (document.pdfName == "") {
            finish()
            return
        }

        with(binding.toolbar) {
            title = document.name
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cachedPdf.collect { when(val state = it) {
                    is State.Success -> {
                        withContext(Dispatchers.IO) {
                            withContext(Dispatchers.Main) {
                                binding.progressBar.hide()
                                binding.viewPdf.fromFile(state.data).show()
                            }
                        }
                    }
                    is State.Failure -> {
                        binding.progressBar.hide()
                        Snackbar.make(binding.root, "Terjadi kesalahan pada pembuat surat", Snackbar.LENGTH_SHORT).show()
                        state.throwable.printStackTrace()
                    }
                    State.Loading -> binding.progressBar.show()
                }}
            }
        }

        lifecycleScope.launch {
            viewModel.usablePdf.collect { when(val state = it) {
                is State.Success -> {
                    binding.progressBar.hide()
                    val uri = FileProvider.getUriForFile(
                        this@ResultActivity,
                        "com.id.administrasikaligentong.fileprovider",
                        state.data
                    )
                    ShareCompat.IntentBuilder(this@ResultActivity)
                        .setType("application/pdf")
                        .setStream(uri)
                        .startChooser()
                }
                is State.Failure -> {
                    binding.progressBar.hide()
                    Snackbar.make(binding.root, "Terjadi kesalahan pada pembuat surat", Snackbar.LENGTH_SHORT).show()
                    state.throwable.printStackTrace()
                }
                State.Loading -> binding.progressBar.show()
            }}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_result, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.action_save_pdf -> {
            if (viewModel.cachedPdf.value != State.Loading) viewModel.getUsablePdf()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun ProgressBar.show() = apply {
        visibility = View.VISIBLE
    }

    private fun ProgressBar.hide() = apply {
        visibility = View.GONE
    }

    companion object {
        private val TAG = ResultActivity::class.java.simpleName
        const val EXTRA_PARCELABLE_DOCUMENT = "extra_parcelable_document"
    }
}