package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiTool
import com.example.data.AiToolId
import com.example.ui.CreatorUiState
import com.example.ui.CreatorViewModel
import com.example.ui.components.CreditBadge

@Composable
fun ToolsTab(
    state: CreatorUiState,
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    if (state.isToolDetailOpen) {
        // Dedicated Tool Generator Screen
        ToolDetailGeneratorScreen(
            state = state,
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        // Vertical One-by-One Tools List Screen
        ToolsVerticalListScreen(
            state = state,
            viewModel = viewModel,
            modifier = modifier
        )
    }
}

/**
 * 1. Vertical One-by-One Directory of all 8 AI Tools
 */
@Composable
private fun ToolsVerticalListScreen(
    state: CreatorUiState,
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tools_list_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Creator AI Tools",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "8 Specialized AI generation engines",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                CreditBadge(
                    remaining = state.remainingQuota,
                    total = state.dailyFreeQuota
                )
            }
        }

        // Vertical One-by-One Tool Cards
        items(AiTool.ALL, key = { it.id.name }) { tool ->
            VerticalToolCard(
                tool = tool,
                onClick = {
                    viewModel.selectTool(tool.id, openDetail = true)
                }
            )
        }
    }
}

@Composable
private fun VerticalToolCard(
    tool: AiTool,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = tool.primaryColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .testTag("tool_card_${tool.id.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = tool.primaryColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        0.5.dp,
                        tool.primaryColor.copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = tool.emojiIcon, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Trailing Chevron
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Open ${tool.title}",
                    tint = tool.primaryColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * 2. Dedicated Tool Generator Screen with Top Back Button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToolDetailGeneratorScreen(
    state: CreatorUiState,
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTool = AiTool.findById(state.selectedToolId)
    var lastCopiedTag by remember { mutableStateOf<String?>(null) }

    // Intercept hardware/gesture back to return to the vertical list
    BackHandler {
        viewModel.closeToolDetail()
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val safeText = if (text.isNotBlank()) text else state.toolOutput
        clipboard.setPrimaryClip(ClipData.newPlainText(label, safeText))
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
        lastCopiedTag = label
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar with "← Back to All Tools" Button & Credit Badge
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_detail_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.closeToolDetail() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("back_to_tools_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "← Back to All Tools",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                CreditBadge(
                    remaining = state.remainingQuota,
                    total = state.dailyFreeQuota
                )
            }
        }

        // Active Tool Studio Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        currentTool.primaryColor.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("active_tool_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Tool Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = currentTool.primaryColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentTool.emojiIcon, fontSize = 26.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = currentTool.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = currentTool.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Special Feature: Language Selector for AI Translator & Polisher (English default)
                    if (currentTool.id == AiToolId.AI_TRANSLATOR_POLISHER) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("language_selector_container")
                        ) {
                            Text(
                                text = "Select Target Language:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3B82F6)
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val languages = listOf("English", "Tamil", "Hindi", "Spanish", "French", "Arabic", "German", "Japanese")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                languages.forEach { lang ->
                                    val isSelected = state.toolExtraArg == lang
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateToolExtraArg(lang) },
                                        label = { Text(lang, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF3B82F6),
                                            selectedLabelColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("lang_chip_$lang")
                                    )
                                }
                            }
                        }
                    }

                    // Sample Prompt Chips
                    Text(
                        text = "Quick Presets / Ideas:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentTool.samplePrompts.forEach { sample ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        0.5.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.updateToolInput(sample) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("preset_chip")
                            ) {
                                Text(
                                    text = "💡 $sample",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input Field
                    OutlinedTextField(
                        value = state.toolInput,
                        onValueChange = { viewModel.updateToolInput(it) },
                        placeholder = {
                            Text(
                                currentTool.placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("tool_prompt_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentTool.primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Generate Button & Credit Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cost: 1 Credit (${state.remainingQuota} left)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Button(
                            onClick = { viewModel.generateFromTool() },
                            enabled = !state.isGenerating,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTool.primaryColor
                            ),
                            modifier = Modifier.testTag("tool_generate_button")
                        ) {
                            if (state.isGenerating && state.generatingTarget == "tool") {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Output", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Generated Output Card
        item {
            AnimatedVisibility(
                visible = state.toolOutput.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            currentTool.primaryColor.copy(alpha = 0.3f),
                            RoundedCornerShape(20.dp)
                        )
                        .testTag("tool_output_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Generated Package",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = currentTool.primaryColor
                                )
                            )

                            // Share Action
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, state.toolOutput)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Output"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("share_tool_output_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // SPECIAL UI COPY BUTTONS FOR TOOLS
                        when (currentTool.id) {
                            AiToolId.YOUTUBE_SEO_TAGS -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("youtube_seo_copy_bar"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "One-Tap Copy Actions:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val titles = extractSection(state.toolOutput, "[TITLES]", listOf("[DESCRIPTION]", "[TAGS]"))
                                        CopyChip(
                                            label = "Copy Titles",
                                            isActive = lastCopiedTag == "Titles",
                                            onClick = { copyToClipboard("Titles", titles) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        val desc = extractSection(state.toolOutput, "[DESCRIPTION]", listOf("[TAGS]", "[HASHTAGS]"))
                                        CopyChip(
                                            label = "Copy Description",
                                            isActive = lastCopiedTag == "Description",
                                            onClick = { copyToClipboard("Description", desc) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val tags = extractSection(state.toolOutput, "[TAGS]", listOf("### [OTHER]"))
                                        CopyChip(
                                            label = "Copy Tags & Hashtags",
                                            isActive = lastCopiedTag == "Tags",
                                            onClick = { copyToClipboard("Tags", tags) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        CopyChip(
                                            label = "Copy All",
                                            isActive = lastCopiedTag == "All",
                                            onClick = { copyToClipboard("All", state.toolOutput) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            AiToolId.REELS_SHORTS_HOOKS -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("reels_hooks_copy_bar"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "One-Tap Copy Actions:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val hooks = extractSection(state.toolOutput, "[HOOKS]", listOf("[SCRIPT]", "[CAPTIONS]"))
                                        CopyChip(
                                            label = "Copy Hooks",
                                            isActive = lastCopiedTag == "Hooks",
                                            onClick = { copyToClipboard("Hooks", hooks) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        val script = extractSection(state.toolOutput, "[SCRIPT]", listOf("[CAPTIONS & HASHTAGS]"))
                                        CopyChip(
                                            label = "Copy Script",
                                            isActive = lastCopiedTag == "Script",
                                            onClick = { copyToClipboard("Script", script) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val captions = extractSection(state.toolOutput, "[CAPTIONS", listOf("### [OTHER]"))
                                        CopyChip(
                                            label = "Copy Captions & Hashtags",
                                            isActive = lastCopiedTag == "Captions",
                                            onClick = { copyToClipboard("Captions", captions) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        CopyChip(
                                            label = "Copy All",
                                            isActive = lastCopiedTag == "All",
                                            onClick = { copyToClipboard("All", state.toolOutput) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            else -> {
                                OutlinedButton(
                                    onClick = { copyToClipboard("Output", state.toolOutput) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("copy_all_button")
                                ) {
                                    Icon(
                                        imageVector = if (lastCopiedTag == "Output") Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy All",
                                        tint = if (lastCopiedTag == "Output") Color(0xFF10B981) else currentTool.primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lastCopiedTag == "Output") "Copied to Clipboard!" else "Copy Complete Output",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Content Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(16.dp)
                                .testTag("tool_output_text_box")
                        ) {
                            Text(
                                text = state.toolOutput,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        modifier = modifier.testTag("copy_chip_${label.replace(" ", "_").lowercase()}")
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun extractSection(fullText: String, startHeader: String, followingHeaders: List<String>): String {
    val startIdx = fullText.indexOf(startHeader, ignoreCase = true)
    if (startIdx == -1) return fullText
    val contentStart = startIdx + startHeader.length

    var nearestEnd = fullText.length
    for (hdr in followingHeaders) {
        val nextIdx = fullText.indexOf(hdr, startIndex = contentStart, ignoreCase = true)
        if (nextIdx != -1 && nextIdx < nearestEnd) {
            nearestEnd = nextIdx
        }
    }

    val extracted = fullText.substring(contentStart, nearestEnd).trim()
        .removePrefix(":")
        .removePrefix("###")
        .removePrefix("]")
        .trim()
    return if (extracted.isNotBlank()) extracted else fullText
}
