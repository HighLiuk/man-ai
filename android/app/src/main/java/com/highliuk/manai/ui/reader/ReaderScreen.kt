@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.highliuk.manai.ui.reader

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.highliuk.manai.R
import com.highliuk.manai.domain.model.ReadingDirection
import com.highliuk.manai.domain.model.ReadingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            val title = (uiState as? ReaderUiState.Ready)?.title ?: ""
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            val ready = uiState as? ReaderUiState.Ready
            if (ready != null) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.page_indicator,
                                ready.currentPage + 1,
                                ready.pageCount,
                            ),
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        IconButton(onClick = onSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        when (val state = uiState) {
            is ReaderUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is ReaderUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.reader_error),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            is ReaderUiState.Ready -> {
                when (state.settings.readingMode) {
                    ReadingMode.SINGLE_PAGE -> SinglePageContent(
                        state = state,
                        onPageChanged = { viewModel.goToPage(it) },
                        modifier = Modifier.padding(padding),
                    )
                    ReadingMode.DOUBLE_PAGE -> DoublePageContent(
                        state = state,
                        onPageChanged = { viewModel.goToPage(it) },
                        modifier = Modifier.padding(padding),
                    )
                    ReadingMode.LONG_STRIP -> LongStripContent(
                        filePath = state.filePath,
                        state = state,
                        onVisiblePageChanged = { viewModel.onVisiblePageChanged(it) },
                        modifier = Modifier.padding(padding),
                    )
                }
            }
        }
    }
}

@Composable
private fun SinglePageContent(
    state: ReaderUiState.Ready,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRtl = state.settings.readingDirection == ReadingDirection.RTL
    val tapEnabled = state.settings.tapNavigationEnabled
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { state.pageCount },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page -> onPageChanged(page) }
    }

    HorizontalPager(
        state = pagerState,
        reverseLayout = isRtl,
        modifier = modifier.fillMaxSize(),
    ) { pageIndex ->
        PdfPageImage(
            uriString = state.filePath,
            pageIndex = pageIndex,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .tapToNavigate(
                    enabled = tapEnabled,
                    isRtl = isRtl,
                    onNext = {
                        val target = (pagerState.currentPage + 1).coerceAtMost(state.pageCount - 1)
                        scope.launch { pagerState.scrollToPage(target) }
                    },
                    onPrevious = {
                        val target = (pagerState.currentPage - 1).coerceAtLeast(0)
                        scope.launch { pagerState.scrollToPage(target) }
                    },
                ),
        )
    }
}

@Composable
private fun DoublePageContent(
    state: ReaderUiState.Ready,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRtl = state.settings.readingDirection == ReadingDirection.RTL
    val tapEnabled = state.settings.tapNavigationEnabled
    val coverAlone = state.settings.coverAlone
    val scope = rememberCoroutineScope()

    val totalSpreads = spreadCount(state.pageCount, coverAlone)
    val initialSpread = pageToSpread(state.currentPage, coverAlone)

    val pagerState = rememberPagerState(
        initialPage = initialSpread,
        pageCount = { totalSpreads },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { spreadIndex ->
                val (firstPage, _) = spreadToPages(spreadIndex, state.pageCount, coverAlone)
                onPageChanged(firstPage)
            }
    }

    val tapModifier = Modifier.tapToNavigate(
        enabled = tapEnabled,
        isRtl = isRtl,
        onNext = {
            val target = (pagerState.currentPage + 1).coerceAtMost(totalSpreads - 1)
            scope.launch { pagerState.scrollToPage(target) }
        },
        onPrevious = {
            val target = (pagerState.currentPage - 1).coerceAtLeast(0)
            scope.launch { pagerState.scrollToPage(target) }
        },
    )

    HorizontalPager(
        state = pagerState,
        reverseLayout = isRtl,
        modifier = modifier.fillMaxSize(),
    ) { spreadIndex ->
        val (firstPage, secondPage) = spreadToPages(spreadIndex, state.pageCount, coverAlone)

        if (secondPage != null) {
            Row(
                modifier = Modifier.fillMaxSize().then(tapModifier),
                horizontalArrangement = Arrangement.Center,
            ) {
                PdfPageImage(
                    uriString = state.filePath,
                    pageIndex = firstPage,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                PdfPageImage(
                    uriString = state.filePath,
                    pageIndex = secondPage,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        } else {
            PdfPageImage(
                uriString = state.filePath,
                pageIndex = firstPage,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().then(tapModifier),
            )
        }
    }
}

@Composable
private fun LongStripContent(
    filePath: String,
    state: ReaderUiState.Ready,
    onVisiblePageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = state.currentPage)

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { onVisiblePageChanged(it) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        items(state.pageCount) { pageIndex ->
            PdfPageImage(
                uriString = filePath,
                pageIndex = pageIndex,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PdfPageImage(
    uriString: String,
    pageIndex: Int,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var bitmap by remember(uriString, pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uriString, pageIndex) {
        bitmap = withContext(Dispatchers.IO) {
            try {
                val uri = android.net.Uri.parse(uriString)
                val fd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
                fd.use { descriptor ->
                    val renderer = android.graphics.pdf.PdfRenderer(descriptor)
                    renderer.use { pdf ->
                        if (pageIndex >= pdf.pageCount) return@withContext null
                        val page = pdf.openPage(pageIndex)
                        page.use {
                            val width = 1080
                            val scale = width.toFloat() / it.width
                            val height = (it.height * scale).toInt()
                            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            it.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bmp
                        }
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Box(
            modifier = modifier
                .then(
                    if (contentScale == ContentScale.FillWidth) {
                        Modifier.aspectRatio(0.707f)
                    } else {
                        Modifier
                    }
                )
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun Modifier.tapToNavigate(
    enabled: Boolean,
    isRtl: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
): Modifier {
    if (!enabled) return this
    val currentOnNext by rememberUpdatedState(onNext)
    val currentOnPrevious by rememberUpdatedState(onPrevious)
    return pointerInput(isRtl) {
        detectTapGestures { offset ->
            val third = size.width / 3f
            when {
                offset.x < third -> if (isRtl) currentOnNext() else currentOnPrevious()
                offset.x > 2 * third -> if (isRtl) currentOnPrevious() else currentOnNext()
            }
        }
    }
}
