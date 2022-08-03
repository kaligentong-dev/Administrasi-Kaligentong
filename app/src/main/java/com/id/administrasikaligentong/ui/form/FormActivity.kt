package com.id.administrasikaligentong.ui.form

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.id.administrasikaligentong.R
import com.id.administrasikaligentong.databinding.ActivityFormBinding
import com.id.administrasikaligentong.entity.DocumentEntity
import com.id.administrasikaligentong.entity.DocumentFormEntity
import com.id.administrasikaligentong.ui.result.ResultActivity
import com.id.administrasikaligentong.util.DocumentFormAdapter
import com.id.administrasikaligentong.util.InputType

class FormActivity : AppCompatActivity() {

    private var _binding: ActivityFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FormViewModel>()
    private lateinit var recyclerAdapter: DocumentFormAdapter
    private lateinit var document: DocumentEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        document = intent.getParcelableExtra(EXTRA_PARCELABLE_DOCUMENT) ?: DocumentEntity.empty()
        if (document.pdfName == "") {
            finish()
            return
        }

        with(binding.toolbarForm) {
            title = document.name
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }

        recyclerAdapter = DocumentFormAdapter(document.formFields)

        with(binding.rvForm) {
            layoutManager = LinearLayoutManager(this@FormActivity)
            adapter = recyclerAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_form, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.action_see_pdf -> {
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra(ResultActivity.EXTRA_PARCELABLE_DOCUMENT, document)
            }
            startActivity(intent)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val EXTRA_PARCELABLE_DOCUMENT = "extra_parcelable_document"
    }
}