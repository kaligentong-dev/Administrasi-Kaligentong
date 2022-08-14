package com.id.administrasikaligentong.ui.result

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var actionToPdf: Action

    private val viewModel by viewModels<ResultViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ResultViewModel(document, ImpPdfRepository(application)) as T
        }
    }

    private val openDirectory = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {

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
                    when(actionToPdf) {
                        Action.PRINT -> startActivity(Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "application/pdf"
                            setPackage(PACKAGE_NAME_EPSON_PRINT)
                            putExtra(Intent.EXTRA_STREAM, uri)
                        })
                        Action.SHARE -> startActivity(Intent.createChooser(Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(PACKAGE_NAME_EPSON_PRINT))
                            putExtra(Intent.EXTRA_STREAM, uri)
                        }, getString(R.string.title_share)))
                        Action.SAVE -> {
                            Snackbar.make(binding.root, "WIP", Snackbar.LENGTH_SHORT).show()
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                if (!isAppInstalled(PACKAGE_NAME_EPSON_PRINT)) {
                    MaterialAlertDialogBuilder(this@ResultActivity)
                        .setMessage(R.string.prompt_iprint_missing)
                        .setPositiveButton(R.string.text_yes, ) { _, _ ->
                            startActivity(Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(MARKET_URL_EPSON_PRINT))
                            )
                        }
                        .setNegativeButton(R.string.text_no, null)
                        .show()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { invalidateOptionsMenu() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_result, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_print_pdf).setIcon(
            if (isAppInstalled(PACKAGE_NAME_EPSON_PRINT)) {
                R.drawable.ic_print
            } else {
                R.drawable.ic_print_disabled
            }
        )
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_save_pdf -> {
                usePdf(Action.SAVE)
                true
            }
            R.id.action_print_pdf -> {
                if (!isAppInstalled(PACKAGE_NAME_EPSON_PRINT)) {
                    Snackbar.make(binding.root, R.string.message_iprint_missing, Snackbar.LENGTH_LONG)
                        .setAction(R.string.text_install) {
                            startActivity(Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(MARKET_URL_EPSON_PRINT))
                            )
                        }
                        .show()
                    return true
                }
                usePdf(Action.PRINT)
                true
            }
            R.id.action_share_pdf -> {
                usePdf(Action.SHARE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun usePdf(action: Action) {
        actionToPdf = action
        if (viewModel.cachedPdf.value != State.Loading) viewModel.getUsablePdf()
    }

    private fun isAppInstalled(packageName: String) = try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private enum class Action {
        PRINT, SHARE, SAVE
    }

    companion object {
        private val TAG = ResultActivity::class.java.simpleName
        private const val MARKET_URL_EPSON_PRINT = "https://play.google.com/store/apps/details?id=epson.print"
        private const val PACKAGE_NAME_EPSON_PRINT = "epson.print"
        const val EXTRA_PARCELABLE_DOCUMENT = "extra_parcelable_document"
    }
}