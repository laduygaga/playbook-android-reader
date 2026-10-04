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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
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
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showTocSheet by remember { mutableStateOf(false) }

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

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            Triple(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
                chapters.size
            )
        }.collect { (itemIndex, scrollOffset, totalChapters) ->
            if (totalChapters > 0) {
                val progress = (itemIndex + 1).toFloat() / totalChapters.toFloat()
                viewModel.saveProgress(itemIndex, scrollOffset, progress)
            }
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
                    fontSize = 16.sp
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
                    ChapterView(
                        chapter = chapter,
                        settings = settings,
                        themeColors = themeColors
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
            val pagerState = rememberPagerState(
                initialPage = book?.currentChapterIndex ?: 0,
                pageCount = { chapters.size }
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val chapter = chapters[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = settings.textMarginDp.dp, vertical = 72.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ChapterView(
                        chapter = chapter,
                        settings = settings,
                        themeColors = themeColors
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = themeColors.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = book?.title ?: "Reader",
                            color = themeColors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = themeColors.text
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val newMode = if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) {
                                ReadingMode.HORIZONTAL_PAGED
                            } else {
                                ReadingMode.VERTICAL_SCROLL
                            }
                            viewModel.updateSettings(settings.copy(readingMode = newMode))
                        }) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Toggle Reading Mode",
                                tint = if (settings.readingMode == ReadingMode.VERTICAL_SCROLL) themeColors.accent else themeColors.secondaryText
                            )
                        }

                        IconButton(onClick = { showTocSheet = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "Table of Contents",
                                tint = themeColors.text
                            )
                        }

                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Typography Settings",
                                tint = themeColors.text
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }

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
                    val currentIdx = lazyListState.firstVisibleItemIndex
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
                            coroutineScope.launch {
                                lazyListState.scrollToItem(targetIdx.toInt())
                            }
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
                    text = "Reading Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.text
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Reading Mode", fontSize = 14.sp, color = themeColors.secondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isScroll = settings.readingMode == ReadingMode.VERTICAL_SCROLL
                    ModeChip(
                        title = "Vertical Scroll",
                        isSelected = isScroll,
                        themeColors = themeColors,
                        onClick = {
                            viewModel.updateSettings(settings.copy(readingMode = ReadingMode.VERTICAL_SCROLL))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ModeChip(
                        title = "Paged Mode",
                        isSelected = !isScroll,
                        themeColors = themeColors,
                        onClick = {
                            viewModel.updateSettings(settings.copy(readingMode = ReadingMode.HORIZONTAL_PAGED))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Font Size", fontSize = 14.sp, color = themeColors.secondaryText)
                    Text("${settings.fontSizeSp.toInt()} sp", fontSize = 14.sp, color = themeColors.accent)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val newSize = (settings.fontSizeSp - 2f).coerceAtLeast(12f)
                        viewModel.updateSettings(settings.copy(fontSizeSp = newSize))
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Font Size", tint = themeColors.text)
                    }
                    Slider(
                        value = settings.fontSizeSp,
                        onValueChange = { viewModel.updateSettings(settings.copy(fontSizeSp = it)) },
                        valueRange = 12f..32f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = themeColors.accent, activeTrackColor = themeColors.accent)
                    )
                    IconButton(onClick = {
                        val newSize = (settings.fontSizeSp + 2f).coerceAtMost(32f)
                        viewModel.updateSettings(settings.copy(fontSizeSp = newSize))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Font Size", tint = themeColors.text)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Color Theme", fontSize = 14.sp, color = themeColors.secondaryText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeCircle("Light", ReaderThemeColors.LIGHT, settings.themeName == "Light", themeColors) {
                        viewModel.updateSettings(settings.copy(themeName = "Light"))
                    }
                    ThemeCircle("Sepia", ReaderThemeColors.SEPIA, settings.themeName == "Sepia", themeColors) {
                        viewModel.updateSettings(settings.copy(themeName = "Sepia"))
                    }
                    ThemeCircle("Dark", ReaderThemeColors.DARK, settings.themeName == "Dark", themeColors) {
                        viewModel.updateSettings(settings.copy(themeName = "Dark"))
                    }
                    ThemeCircle("Amoled", ReaderThemeColors.NIGHT, settings.themeName == "Amoled Night", themeColors) {
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
                Text(
                    text = "Table of Contents",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.text
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(chapters) { index, chapter ->
                        val isCurrent = lazyListState.firstVisibleItemIndex == index
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) themeColors.accent.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        lazyListState.scrollToItem(index)
                                    }
                                    showTocSheet = false
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 14.sp,
                                    color = if (isCurrent) themeColors.accent else themeColors.secondaryText,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = chapter.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) themeColors.accent else themeColors.text,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun ChapterView(
    chapter: Chapter,
    settings: ReaderSettings,
    themeColors: ReaderThemeColors
) {
    val fontFamily = when (settings.fontStyle) {
        FontStyleOption.SERIF -> FontFamily.Serif
        FontStyleOption.SANS_SERIF -> FontFamily.SansSerif
        FontStyleOption.MONOSPACE -> FontFamily.Monospace
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = chapter.title,
            fontSize = (settings.fontSizeSp + 6).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            color = themeColors.text,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = chapter.content,
            fontSize = settings.fontSizeSp.sp,
            lineHeight = (settings.fontSizeSp * settings.lineSpacingMultiplier).sp,
            fontFamily = fontFamily,
            color = themeColors.text
        )
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
        shape = RoundedCornerShape(20.dp),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, themeColors.secondaryText.copy(alpha = 0.3f)) else null,
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
                fontWeight = FontWeight.Bold
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
