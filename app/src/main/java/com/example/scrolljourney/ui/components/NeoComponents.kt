package com.example.scrolljourney.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object NeoDimens {
    val BorderWidth = 3.dp
    val BorderWidthNested = 2.dp
    val ShadowOffset = 4.dp
    val CornerRadius = 4.dp
    val TopBarBorderWidth = 3.dp
}

/** Top-level neobrutalist block: solid fill, hard border, flat offset shadow (no blur). */
@Composable
fun NeoPanel(
    fillColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = NeoDimens.BorderWidth,
    shadowColor: Color = borderColor,
    shadowOffset: Dp = NeoDimens.ShadowOffset,
    cornerRadius: Dp = NeoDimens.CornerRadius,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(color = shadowColor, shape = shape)
        )
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = fillColor, shape = shape)
                    .border(width = borderWidth, color = borderColor, shape = shape)
                    .padding(contentPadding),
                content = content
            )
        }
    }
}

/** Nested sub-region inside a NeoPanel: solid fill, hard border, no shadow (avoids doubling up). */
@Composable
fun NeoInsetBlock(
    fillColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = NeoDimens.BorderWidthNested,
    cornerRadius: Dp = NeoDimens.CornerRadius,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(color = fillColor, shape = shape)
                .border(width = borderWidth, color = borderColor, shape = shape)
                .padding(contentPadding),
            content = content
        )
    }
}

/** Compact pill for short inline status text (ON/OFF, checkmarks, +XP, etc). */
@Composable
fun NeoBadge(
    text: String,
    fillColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
) {
    val shape = RoundedCornerShape(NeoDimens.CornerRadius)
    Box(
        modifier = modifier
            .background(fillColor, shape)
            .border(NeoDimens.BorderWidthNested, borderColor, shape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

/** Clickable neobrutalist block: same shadow/border treatment as NeoPanel, pushes flat on press. */
@Composable
fun NeoButton(
    onClick: () -> Unit,
    fillColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(NeoDimens.CornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val offset by animateDpAsState(
        targetValue = if (pressed) 0.dp else NeoDimens.ShadowOffset,
        label = "neoButtonShadow"
    )

    Box(modifier = modifier.padding(end = NeoDimens.ShadowOffset, bottom = NeoDimens.ShadowOffset)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(NeoDimens.ShadowOffset, NeoDimens.ShadowOffset)
                .background(borderColor, shape)
        )
        Row(
            modifier = Modifier
                .offset(offset, offset)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .background(fillColor, shape)
                .border(NeoDimens.BorderWidth, borderColor, shape)
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor, content = { content() })
        }
    }
}

/** Full-bleed top bar with a solid bottom border, replacing per-screen duplicated top-bar rows. */
@Composable
fun NeoTopBar(
    title: String,
    fillColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onNavigateBack: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val borderWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { NeoDimens.TopBarBorderWidth.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(fillColor)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = borderWidthPx
                )
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onNavigateBack != null) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = contentColor)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = contentColor)
        trailing()
    }
}
