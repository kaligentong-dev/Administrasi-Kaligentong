package com.id.administrasikaligentong.ui.result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.id.administrasikaligentong.entity.DocumentEntity
import com.id.administrasikaligentong.repository.PdfRepository
import com.id.administrasikaligentong.util.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File

class ResultViewModel(
    private val doc: DocumentEntity,
    private val repo: PdfRepository
) : ViewModel() {

    private val _cachedPdf: MutableStateFlow<State<File>> = MutableStateFlow(State.Loading)
    val cachedPdf = _cachedPdf.asStateFlow()

    private val _usablePdf: MutableStateFlow<State<File>> = MutableStateFlow(State.Loading)
    val usablePdf = _usablePdf.asStateFlow()

    init {
        viewModelScope.launch {
            repo.createTemporaryPdf(doc.pdfName, doc.formFields)
                .catch { _cachedPdf.emit(State.Failure(it)) }
                .collect { _cachedPdf.emit(State.Success(it)) }
        }
    }

    fun getUsablePdf() = viewModelScope.launch {
        _usablePdf.emit(State.Loading)
        repo.createUsablePdf(doc.pdfName)
            .catch { _usablePdf.emit(State.Failure(it)) }
            .collect { _usablePdf.emit(State.Success(it)) }
    }

    fun getImageOfPdf() = viewModelScope.launch {
        _usablePdf.emit(State.Loading)
        repo.createImageOfPdf(doc.pdfName.replace("pdf", "png"))
            .catch { _usablePdf.emit(State.Failure(it)) }
            .collect { _usablePdf.emit(State.Success(it)) }
    }

    fun getImagePdf() = viewModelScope.launch {
        _usablePdf.emit(State.Loading)
        repo.createImagePdf(doc.pdfName)
            .catch { _usablePdf.emit(State.Failure(it)) }
            .collect { _usablePdf.emit(State.Success(it)) }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: success")
        repo.close()
    }

    companion object {
        private val TAG = ResultViewModel::class.java.simpleName
    }
}