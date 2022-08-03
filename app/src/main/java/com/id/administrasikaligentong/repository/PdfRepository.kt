package com.id.administrasikaligentong.repository

import android.app.Application
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.id.administrasikaligentong.entity.DocumentFormEntity
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

interface PdfRepository {

    suspend fun createTemporaryPdf(fileName: String, fields: List<DocumentFormEntity>): Flow<File>

    suspend fun createUsablePdf(fileName: String): Flow<File>
    
    suspend fun createImagePdf(fileName: String): Flow<File>

    suspend fun createImageOfPdf(fileName: String): Flow<File>

    fun close()
}

class ImpPdfRepository(application: Application) : PdfRepository {

    private var context = application.applicationContext
    private lateinit var doc: PDDocument
    private var cache: File? = null

    init { PDFBoxResourceLoader.init(context) }

    override suspend fun createTemporaryPdf(fileName: String, fields: List<DocumentFormEntity>): Flow<File> = flow {
        withContext(Dispatchers.IO) {
            doc = PDDocument.load(context.assets.open("pdf/$fileName"))
//            doc.pages.map { it.resources }
//                .forEach { res -> res.fontNames
//                    .map { Pair(it, res.getFont(it)) }
//                    .forEach { (cosName, font) ->
//                        res.add(PDType1Font.TIMES_ROMAN)
//                        Log.d(TAG, "font: $font")
//                    }
//                }
            val form = doc.documentCatalog.acroForm
            for (field in fields) {
                val formField = form.getField(field.fieldName)
                if (formField == null) {
                    Log.w(TAG, "No such field: $field")
                    continue
                }
                formField.setValue(field.fieldValue)
            }
            form.flatten()

            cache = File.createTempFile("temp", ".pdf", context.cacheDir)
            doc.save(cache)
        }
        cache?.let { emit(it) }
    }

    override suspend fun createUsablePdf(fileName: String): Flow<File> = flow {
        var file: File?
        withContext(Dispatchers.IO) {
            file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            doc.save(file)
        }
        file?.let { emit(it) }
    }

    override suspend fun createImagePdf(fileName: String): Flow<File> = flow {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        withContext(Dispatchers.IO) {
            val imageDoc = PDDocument()
            val page = PDPage()
            imageDoc.addPage(page)

            val renderer = PDFRenderer(doc)
            val bitmap = renderer.renderImage(0, 1F, ImageType.RGB)

            with(PDPageContentStream(imageDoc, page)) {
                drawImage(JPEGFactory.createFromImage(imageDoc, bitmap), 0F, 0F)
                close()
            }

            imageDoc.save(file)
        }
        emit(file)
    }

    override suspend fun createImageOfPdf(fileName: String): Flow<File> = flow {
        var image: File?
        withContext(Dispatchers.IO) {
            image = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            val renderer = PDFRenderer(doc)
            val bitmap = renderer.renderImage(0, 1F, ImageType.RGB)
            val stream = BufferedOutputStream(FileOutputStream(image))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        }
        image?.let { emit(it) }
    }

    override fun close() {
        cache?.delete()
        doc.close()
    }

    companion object {
        private val TAG = ImpPdfRepository::class.java.simpleName
    }
}