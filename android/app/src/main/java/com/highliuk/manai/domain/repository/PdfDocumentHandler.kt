package com.highliuk.manai.domain.repository

import android.graphics.Bitmap
import com.highliuk.manai.domain.model.PdfMetadata

/**
 * Abstraction for PDF document operations.
 * Implementation uses ContentResolver + PdfRenderer under the hood.
 */
interface PdfDocumentHandler {
    /**
     * Takes persistable URI permission and extracts metadata from the PDF.
     * @param uriString content:// URI string of the PDF document
     * @return metadata containing title (from filename) and page count
     */
    suspend fun importDocument(uriString: String): PdfMetadata

    /**
     * Renders a single page of the PDF as a Bitmap.
     * @param uriString content:// URI string of the PDF document
     * @param pageIndex zero-based page index
     * @param width desired bitmap width in pixels (height scales proportionally)
     * @return rendered bitmap, or null if the page index is out of range
     */
    suspend fun renderPage(uriString: String, pageIndex: Int, width: Int): Bitmap?
}
