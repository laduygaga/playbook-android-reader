package com.playbook.reader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.playbook.reader.domain.model.Chapter
import com.playbook.reader.domain.model.FontStyleOption
import com.playbook.reader.domain.model.ReaderSettings
import com.playbook.reader.domain.model.ReaderThemeColors
import com.playbook.reader.domain.model.ReadingMode
import com.playbook.reader.domain.model.SupportedLanguage
import com.playbook.reader.domain.model.TranslationState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit
) {
    val book by viewModel.book.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isTranslationActive by viewModel.isTranslationActive.collectAsState()
    val translationState by viewModel.translationState.collectAsState()
    val translatedChapters by viewModel.translatedChapters.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showTocSheet by remember { mutableStateOf(false) }
    var showTranslationSheet by remember { mutableStateOf(false) }
    var tocTabSelected by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val themeColors = when (settings.themeName) {
        "Sepia" -> ReaderThemeColors.SEPIA
        "Dark" -> ReaderThemeColors.DARK
        "Amoled Night" -> ReaderThemeColors.NIGHT
        else -> ReaderThemeColors.LIGHT
    }

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = book?.currentChapterIndex ?: 0,
        initialFirstVisibleItemScrollOffset = book?.currentScrollOffset ?: 0
    )
    val pagerState = rememberPagerState(
        initialPage = book?.currentChapterIndex ?: 0,
        pageCount = { chapters.size }
    )
    var pagedScrollOffset by remember { mutableStateOf(book?.currentScrollOffset ?: 0) }

    val currentIdx = if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
        lazyListState.firstVisibleItemIndex
    } else {
        pagerState.currentPage
    }

    val saveCurrentPosition = {
        if (chapters.isNotEmpty()) {
            if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
                val idx = lazyListState.firstVisibleItemIndex
                val offset = lazyListState.firstVisibleItemScrollOffset
                val progress = (idx + 1).toFloat() / chapters.size.toFloat()
                viewModel.saveProgress(idx, offset, progress)
            } else {
                val page = pagerState.currentPage
                val progress = (page + 1).toFloat() / chapters.size.toFloat()
                viewModel.saveProgress(page, pagedScrollOffset, progress)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            saveCurrentPosition()
        }
    }

    val jumpToChapter: (Int) -> Unit = { targetIdx ->
        coroutineScope.launch {
            if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
                lazyListState.scrollToItem(targetIdx)
            } else {
                pagerState.scrollToPage(targetIdx)
            }
        }
    }

    @OptIn(FlowPreview::class)
    LaunchedEffect(lazyListState, settings.readingMode) {
        snapshotFlow {
            Triple(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
                chapters.size
            )
        }
        .debounce(300L)
        .collect { (itemIndex, scrollOffset, totalChapters) ->
            if (totalChapters > 0 && settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
                val progress = (itemIndex + 1).toFloat() / totalChapters.toFloat()
                viewModel.saveProgress(itemIndex, scrollOffset, progress)
            }
        }
    }

    @OptIn(FlowPreview::class)
    LaunchedEffect(pagerState, pagedScrollOffset, settings.readingMode) {
        snapshotFlow {
            Triple(
                pagerState.currentPage,
                pagedScrollOffset,
                chapters.size
            )
        }
        .debounce(300L)
        .collect { (page, scrollOffset, totalChapters) ->
            if (totalChapters > 0 && settings.readingMode == ReadingMode.HORIZONTAL_PAGED) {
                val progress = (page + 1).toFloat() / totalChapters.toFloat()
                viewModel.saveProgress(page, scrollOffset, progress)
            }
        }
    }

    LaunchedEffect(settings.readingMode) {
        if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
            lazyListState.scrollToItem(pagerState.currentPage, pagedScrollOffset)
        } else {
            pagerState.scrollToPage(lazyListState.firstVisibleItemIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        if (chapters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Loading book chapters...",
                    color = themeColors.text,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif
                )
            }
        } else if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = settings.textMarginDp.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }

                itemsIndexed(
                    items = chapters,
                    key = { _, chapter -> chapter.id }
                ) { index, chapter ->
                    val cacheKey = "${chapter.id}-${selectedLanguage.code}"
                    val translatedText = translatedChapters[cacheKey]
                    AppleChapterView(
                        chapter = chapter,
                        settings = settings,
                        themeColors = themeColors,
                        translatedContent = translatedText,
                        isTranslationActive = isTranslationActive,
                        selectedLanguage = selectedLanguage,
                        onToggleOriginal = { viewModel.toggleTranslationActive() }
                    )
                    if (index < chapters.size - 1) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Divider(
                            color = themeColors.secondaryText.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val chapter = chapters[page]
                val pageScrollState = rememberScrollState(
                    initial = if (page == (book?.currentChapterIndex ?: 0)) (book?.currentScrollOffset ?: 0) else 0
                )
                LaunchedEffect(pageScrollState.value, pagerState.currentPage) {
                    if (page == pagerState.currentPage) {
                        pagedScrollOffset = pageScrollState.value
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = settings.textMarginDp.dp, vertical = 72.dp)
                        .verticalScroll(pageScrollState)
                ) {
                    val cacheKey = "${chapter.id}-${selectedLanguage.code}"
                    val translatedText = translatedChapters[cacheKey]
                    AppleChapterView(
                        chapter = chapter,
                        settings = settings,
                        themeColors = themeColors,
                        translatedContent = translatedText,
                        isTranslationActive = isTranslationActive,
                        selectedLanguage = selectedLanguage,
                        onToggleOriginal = { viewModel.toggleTranslationActive() }
                    )
                }
            }
        }

        // Apple Books Top Navigation Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = themeColors.surface.copy(alpha = 0.95f),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = book?.title ?: "Book",
                            color = themeColors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            saveCurrentPosition()
                            onBackClick()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = themeColors.accent
                            )
                        }
                    },
                    actions = {
                        val currentChapterTitle = chapters.getOrNull(currentIdx)?.title ?: "Chapter ${currentIdx + 1}"
                        val isCurrentBookmarked = bookmarks.any { it.chapterIndex == currentIdx }

                        IconButton(onClick = {
                            val scrollOffset = if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
                                lazyListState.firstVisibleItemScrollOffset
                            } else 0
                            viewModel.toggleBookmark(
                                chapterIndex = currentIdx,
                                chapterTitle = currentChapterTitle,
                                scrollOffset = scrollOffset
                            )
                        }) {
                            Icon(
                                imageVector = if (isCurrentBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isCurrentBookmarked) themeColors.accent else themeColors.text
                            )
                        }

                        IconButton(onClick = { showTranslationSheet = true }) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = "Translate Current Page",
                                tint = if (isTranslationActive) themeColors.accent else themeColors.text
                            )
                        }

                        IconButton(onClick = { showTocSheet = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Table of Contents",
                                tint = themeColors.text
                            )
                        }

                        // Apple Books "Aa" Icon Button
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showSettingsSheet = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aa",
                                color = themeColors.text,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }

        // Apple Books Floating Bottom Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = themeColors.surface.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    val currentChapterTitle = chapters.getOrNull(currentIdx)?.title ?: ""
                    val progressPct = if (chapters.isNotEmpty()) {
                        ((currentIdx + 1).toFloat() / chapters.size.toFloat() * 100).toInt()
                    } else 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentChapterTitle,
                            color = themeColors.secondaryText,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$progressPct%",
                            color = themeColors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = currentIdx.toFloat(),
                        onValueChange = { targetIdx ->
                            jumpToChapter(targetIdx.toInt())
                        },
                        valueRange = 0f..(chapters.size - 1).coerceAtLeast(1).toFloat(),
                        steps = (chapters.size - 2).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.accent,
                            activeTrackColor = themeColors.accent,
                            inactiveTrackColor = themeColors.secondaryText.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }

    // Apple Books Typography Settings Sheet ("Aa")
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = themeColors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Text & Appearance",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = themeColors.text
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Font Size Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        val newSize = (settings.fontSizeSp - 2f).coerceAtLeast(12f)
                        viewModel.updateSettings(settings.copy(fontSizeSp = newSize))
                    }) {
                        Text("A", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.text)
                    }

                    Slider(
                        value = settings.fontSizeSp,
                        onValueChange = { viewModel.updateSettings(settings.copy(fontSizeSp = it)) },
                        valueRange = 12f..32f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.accent,
                            activeTrackColor = themeColors.accent
                        )
                    )

                    IconButton(onClick = {
                        val newSize = (settings.fontSizeSp + 2f).coerceAtMost(32f)
                        viewModel.updateSettings(settings.copy(fontSizeSp = newSize))
                    }) {
                        Text("A", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = themeColors.text)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Font Selection
                Text("Font Family", fontSize = 13.sp, color = themeColors.secondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FontStyleOption.values().forEach { option ->
                        val isSelected = settings.fontStyle == option
                        Surface(
                            color = if (isSelected) themeColors.accent else themeColors.background,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateSettings(settings.copy(fontStyle = option)) }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else themeColors.text
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reading Mode Selector
                Text("Page Mode", fontSize = 13.sp, color = themeColors.secondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isScroll = settings.readingMode == ReadingMode.VERTICAL_SCROLL
                    ModeChip(
                        title = "Continuous Scroll",
                        isSelected = isScroll,
                        themeColors = themeColors,
                        onClick = {
                            viewModel.updateSettings(settings.copy(readingMode = ReadingMode.VERTICAL_SCROLL))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeChip(
                        title = "Page Turn",
                        isSelected = !isScroll,
                        themeColors = themeColors,
                        onClick = {
                            viewModel.updateSettings(settings.copy(readingMode = ReadingMode.HORIZONTAL_PAGED))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Color Themes
                Text("Theme", fontSize = 13.sp, color = themeColors.secondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeCircle("White", ReaderThemeColors.LIGHT, settings.themeName == "Light", themeColors) {
                        viewModel.updateSettings(settings.copy(themeName = "Light"))
                    }
                    ThemeCircle("Sepia", ReaderThemeColors.SEPIA, settings.themeName == "Sepia", themeColors) {
                        viewModel.updateSettings(settings.copy(themeName = "Sepia"))
                    }
                    ThemeCircle("Gray", ReaderThemeColors.DARK, settings.themeName == "Dark", themeColors) {
                        viewModel.updateSettings(settings.copy(themeName = "Dark"))
                    }
                    ThemeCircle("Night", ReaderThemeColors.NIGHT, settings.themeName == "Amoled Night", themeColors) {
                        viewModel.updateSettings(settings.copy(themeName = "Amoled Night"))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showTocSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTocSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = themeColors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = if (tocTabSelected == 0) themeColors.accent else themeColors.background,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { tocTabSelected = 0 }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Contents",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tocTabSelected == 0) Color.White else themeColors.text
                            )
                        }
                    }
                    Surface(
                        color = if (tocTabSelected == 1) themeColors.accent else themeColors.background,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { tocTabSelected = 1 }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bookmarks (${bookmarks.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tocTabSelected == 1) Color.White else themeColors.text
                            )
                        }
                    }
                }

                if (tocTabSelected == 0) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(chapters) { index, chapter ->
                            val isCurrent = currentIdx == index
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) themeColors.accent.copy(alpha = 0.12f) else Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        jumpToChapter(index)
                                        showTocSheet = false
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) themeColors.accent else themeColors.secondaryText,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Text(
                                        text = chapter.title,
                                        fontSize = 15.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Serif,
                                        color = if (isCurrent) themeColors.accent else themeColors.text,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (bookmarks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No bookmarks yet.\nTap the bookmark icon while reading to save one.",
                                color = themeColors.secondaryText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(bookmarks, key = { it.id }) { bookmark ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = themeColors.background),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    coroutineScope.launch {
                                                        if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
                                                            lazyListState.scrollToItem(
                                                                bookmark.chapterIndex,
                                                                bookmark.scrollOffset
                                                            )
                                                        } else {
                                                            pagerState.scrollToPage(bookmark.chapterIndex)
                                                        }
                                                    }
                                                    showTocSheet = false
                                                }
                                        ) {
                                            Text(
                                                text = bookmark.chapterTitle,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Serif,
                                                color = themeColors.text
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Chapter ${bookmark.chapterIndex + 1}",
                                                fontSize = 12.sp,
                                                color = themeColors.secondaryText
                                            )
                                        }
                                        IconButton(onClick = { viewModel.removeBookmark(bookmark.id) }) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "Remove Bookmark",
                                                tint = themeColors.secondaryText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTranslationSheet) {
        val currentChapter = chapters.getOrNull(currentIdx)
        ModalBottomSheet(
            onDismissRequest = { showTranslationSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = themeColors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = null,
                            tint = themeColors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Translate Current Page",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = themeColors.text
                        )
                    }
                    if (isTranslationActive) {
                        Surface(
                            color = themeColors.accent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentChapter?.title ?: "Current Page",
                    fontSize = 13.sp,
                    color = themeColors.secondaryText
                )

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Select Language",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.secondaryText
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SupportedLanguage.DEFAULT_LANGUAGES) { lang ->
                        val isSelected = selectedLanguage.code == lang.code
                        Surface(
                            color = if (isSelected) themeColors.accent else themeColors.background,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                viewModel.setSelectedLanguage(lang)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lang.flagEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = lang.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else themeColors.text
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (val state = translationState) {
                    is TranslationState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = themeColors.accent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Translating into ${selectedLanguage.name}...",
                                fontSize = 14.sp,
                                color = themeColors.text
                            )
                        }
                    }
                    is TranslationState.Error -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Translation error: ${state.message}",
                                color = Color(0xFFE53935),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(
                                onClick = {
                                    currentChapter?.let { viewModel.translateCurrentChapter(it, selectedLanguage) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Retry Translation", color = Color.White)
                            }
                        }
                    }
                    else -> {
                        val currentCacheKey = "${currentChapter?.id}-${selectedLanguage.code}"
                        val hasTranslation = translatedChapters.containsKey(currentCacheKey)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (hasTranslation || isTranslationActive) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.toggleTranslationActive()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (isTranslationActive) "Show Original" else "Show Translation",
                                        color = themeColors.text
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    currentChapter?.let {
                                        viewModel.translateCurrentChapter(it, selectedLanguage)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (hasTranslation) "Re-Translate" else "Translate Page",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AppleChapterView(
    chapter: Chapter,
    settings: ReaderSettings,
    themeColors: ReaderThemeColors,
    translatedContent: String? = null,
    isTranslationActive: Boolean = false,
    selectedLanguage: SupportedLanguage? = null,
    onToggleOriginal: (() -> Unit)? = null
) {
    val isTranslated = isTranslationActive && !translatedContent.isNullOrBlank()
    val rawText = if (isTranslated) translatedContent!! else chapter.content

    val fontFamily = when (settings.fontStyle) {
        FontStyleOption.SERIF -> FontFamily.Serif
        FontStyleOption.SANS_SERIF -> FontFamily.SansSerif
        FontStyleOption.MONOSPACE -> FontFamily.Monospace
    }

    val paragraphs = remember(rawText) {
        rawText.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isTranslated && selectedLanguage != null) {
            Surface(
                color = themeColors.accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onToggleOriginal?.invoke() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedLanguage.flagEmoji} Translated to ${selectedLanguage.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.accent
                    )
                    Text(
                        text = "Show Original",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                }
            }
        }

        Text(
            text = chapter.title,
            fontSize = (settings.fontSizeSp + 6).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = themeColors.text,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (paragraphs.isEmpty()) {
            Text(
                text = rawText,
                fontSize = settings.fontSizeSp.sp,
                lineHeight = (settings.fontSizeSp * settings.lineSpacingMultiplier).sp,
                fontFamily = fontFamily,
                color = themeColors.text
            )
        } else {
            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    fontSize = settings.fontSizeSp.sp,
                    lineHeight = (settings.fontSizeSp * settings.lineSpacingMultiplier).sp,
                    fontFamily = fontFamily,
                    color = themeColors.text,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }
        }
    }
}

@Composable
fun ModeChip(
    title: String,
    isSelected: Boolean,
    themeColors: ReaderThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) themeColors.accent else themeColors.background,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = if (isSelected) Color.White else themeColors.text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ThemeCircle(
    label: String,
    colors: ReaderThemeColors,
    isSelected: Boolean,
    currentThemeColors: ReaderThemeColors,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(colors.background, shape = CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) colors.accent else currentThemeColors.secondaryText.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aa",
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = currentThemeColors.secondaryText
        )
    }
}