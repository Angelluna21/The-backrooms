package com.example.backrooms.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.backrooms.model.*
import kotlin.math.*

object RaycastRenderer {

    data class SpriteToRender(
        val x: Float,
        val y: Float,
        val dist: Float,
        val transformY: Float,
        val spriteScreenX: Float,
        val spriteHeight: Float,
        val spriteWidth: Float,
        val drawItem: ItemInstance? = null,
        val drawEntity: EntityInstance? = null,
        val drawMarker: WallMarker? = null
    )

    fun renderScene(
        drawScope: DrawScope,
        map: GeneratedMap,
        posX: Float,
        posY: Float,
        dirX: Float,
        dirY: Float,
        planeX: Float,
        planeY: Float,
        flashlightOn: Boolean,
        flashlightBattery: Float,
        flickerIntensity: Float,
        vhsDistortion: Float,
        wallMarkers: List<WallMarker>
    ) {
        val w = drawScope.size.width
        val h = drawScope.size.height

        val numRays = 140
        val colWidth = w / numRays
        val zBuffer = FloatArray(numRays)

        // Base lighting calculation
        val flashBoost = if (flashlightOn && flashlightBattery > 0.05f) 0.55f else 0.0f
        val ambient = (map.levelDef.ambientLight * flickerIntensity).coerceIn(0.12f, 1.0f)

        // Draw Ceiling & Floor with vertical gradient
        val halfH = h / 2f
        val ceilColor = map.levelDef.ceilingColor
        val floorColor = map.levelDef.floorColor

        drawScope.drawRect(
            color = ceilColor.copy(alpha = 0.85f * ambient),
            topLeft = Offset(0f, 0f),
            size = Size(w, halfH)
        )
        drawScope.drawRect(
            color = floorColor.copy(alpha = 0.90f * ambient),
            topLeft = Offset(0f, halfH),
            size = Size(w, halfH)
        )

        // Raycasting Loop
        for (i in 0 until numRays) {
            val cameraX = 2f * i / numRays - 1f
            val rayDirX = dirX + planeX * cameraX
            val rayDirY = dirY + planeY * cameraX

            var mapX = posX.toInt()
            var mapY = posY.toInt()

            val deltaDistX = if (abs(rayDirX) < 1e-6f) 1e30f else abs(1f / rayDirX)
            val deltaDistY = if (abs(rayDirY) < 1e-6f) 1e30f else abs(1f / rayDirY)

            var stepX: Int
            var sideDistX: Float
            if (rayDirX < 0) {
                stepX = -1
                sideDistX = (posX - mapX) * deltaDistX
            } else {
                stepX = 1
                sideDistX = (mapX + 1.0f - posX) * deltaDistX
            }

            var stepY: Int
            var sideDistY: Float
            if (rayDirY < 0) {
                stepY = -1
                sideDistY = (posY - mapY) * deltaDistY
            } else {
                stepY = 1
                sideDistY = (mapY + 1.0f - posY) * deltaDistY
            }

            var hit = false
            var side = 0 // 0 for X side, 1 for Y side
            var hitCell = 1
            var steps = 0

            while (!hit && steps < 40) {
                steps++
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX
                    mapX += stepX
                    side = 0
                } else {
                    sideDistY += deltaDistY
                    mapY += stepY
                    side = 1
                }

                if (mapX in 0 until map.width && mapY in 0 until map.height) {
                    val cell = map.grid[mapY][mapX]
                    if (cell == 1 || cell == 2 || cell == 6) { // Wall, Exit, or Pillar
                        hit = true
                        hitCell = cell
                    }
                } else {
                    hit = true
                    hitCell = 1
                }
            }

            val perpWallDist = if (side == 0) {
                (mapX - posX + (1 - stepX) / 2f) / rayDirX
            } else {
                (mapY - posY + (1 - stepY) / 2f) / rayDirY
            }.coerceAtLeast(0.1f)

            zBuffer[i] = perpWallDist

            // Calculate wall slice height
            val lineHeight = (h / perpWallDist).coerceIn(1f, h * 3f)
            val drawStart = (halfH - lineHeight / 2f).coerceAtLeast(0f)
            val drawEnd = (halfH + lineHeight / 2f).coerceAtMost(h)

            // Dynamic Lighting / Darkness Falloff
            val centerBias = 1.0f - abs(cameraX) * 0.35f
            val maxSight = if (flashlightOn && flashlightBattery > 0.05f) 12.0f else 6.0f
            val distShading = (1.0f - (perpWallDist / maxSight)).coerceIn(0.0f, 1.0f)
            val finalLight = ((distShading * (ambient + flashBoost * centerBias))).coerceIn(0.04f, 1.0f)

            // Wall color calculation with pattern/seams
            val wallColor = when (hitCell) {
                2 -> {
                    // EXIT wall / No-clip glitch wall
                    val pulse = (sin(System.currentTimeMillis() / 200.0) * 0.3f + 0.7f).toFloat()
                    if (map.levelDef.id == LevelId.LEVEL_0) {
                        // Glitching dimensional tear
                        Color(0xFFBA68C8).copy(alpha = pulse)
                    } else if (map.levelDef.id == LevelId.LEVEL_RUN) {
                        // Blue emergency exit doors
                        Color(0xFF29B6F6)
                    } else {
                        // Industrial metal exit doors
                        Color(0xFF66BB6A)
                    }
                }
                6 -> {
                    // Pillar
                    Color(0xFF3E3D35)
                }
                else -> {
                    val base = if (side == 1) map.levelDef.wallColorDark else map.levelDef.wallColorPrimary
                    // Subtle vertical wallpaper seam pattern
                    val wallX = if (side == 0) posY + perpWallDist * rayDirY else posX + perpWallDist * rayDirX
                    val texX = wallX - floor(wallX)
                    val seam = if (texX < 0.05f || texX > 0.95f) 0.85f else 1.0f
                    base.copy(
                        red = (base.red * seam).coerceIn(0f, 1f),
                        green = (base.green * seam).coerceIn(0f, 1f),
                        blue = (base.blue * seam).coerceIn(0f, 1f)
                    )
                }
            }

            val litWallColor = Color(
                red = wallColor.red * finalLight,
                green = wallColor.green * finalLight,
                blue = wallColor.blue * finalLight,
                alpha = 1.0f
            )

            drawScope.drawRect(
                color = litWallColor,
                topLeft = Offset(i * colWidth, drawStart),
                size = Size(colWidth + 1f, drawEnd - drawStart)
            )
        }

        // Render Sprites (Items, Entities, Wall Markers)
        renderSprites(
            drawScope = drawScope,
            map = map,
            posX = posX,
            posY = posY,
            dirX = dirX,
            dirY = dirY,
            planeX = planeX,
            planeY = planeY,
            zBuffer = zBuffer,
            numRays = numRays,
            w = w,
            h = h,
            flashlightOn = flashlightOn,
            flashlightBattery = flashlightBattery,
            flickerIntensity = flickerIntensity,
            wallMarkers = wallMarkers
        )

        // Overlay VHS Scanlines and Distortion
        renderVhsOverlay(drawScope, w, h, vhsDistortion)
    }

    private fun renderSprites(
        drawScope: DrawScope,
        map: GeneratedMap,
        posX: Float,
        posY: Float,
        dirX: Float,
        dirY: Float,
        planeX: Float,
        planeY: Float,
        zBuffer: FloatArray,
        numRays: Int,
        w: Float,
        h: Float,
        flashlightOn: Boolean,
        flashlightBattery: Float,
        flickerIntensity: Float,
        wallMarkers: List<WallMarker>
    ) {
        val sprites = mutableListOf<SpriteToRender>()

        // Add Active Items
        for (item in map.items) {
            if (!item.isCollected) {
                val spriteX = item.x - posX
                val spriteY = item.y - posY
                val invDet = 1.0f / (planeX * dirY - dirX * planeY)
                val transformX = invDet * (dirY * spriteX - dirX * spriteY)
                val transformY = invDet * (-planeY * spriteX + planeX * spriteY)

                if (transformY > 0.2f) {
                    val spriteScreenX = (w / 2f) * (1f + transformX / transformY)
                    val spriteHeight = abs(h / transformY) * 0.45f
                    val spriteWidth = spriteHeight * 0.6f
                    sprites.add(
                        SpriteToRender(
                            x = item.x,
                            y = item.y,
                            dist = transformY,
                            transformY = transformY,
                            spriteScreenX = spriteScreenX,
                            spriteHeight = spriteHeight,
                            spriteWidth = spriteWidth,
                            drawItem = item
                        )
                    )
                }
            }
        }

        // Add Entities
        for (entity in map.entities) {
            val spriteX = entity.x - posX
            val spriteY = entity.y - posY
            val invDet = 1.0f / (planeX * dirY - dirX * planeY)
            val transformX = invDet * (dirY * spriteX - dirX * spriteY)
            val transformY = invDet * (-planeY * spriteX + planeX * spriteY)

            if (transformY > 0.2f) {
                val spriteScreenX = (w / 2f) * (1f + transformX / transformY)
                val spriteHeight = abs(h / transformY) * 0.95f
                val spriteWidth = spriteHeight * 0.45f
                sprites.add(
                    SpriteToRender(
                        x = entity.x,
                        y = entity.y,
                        dist = transformY,
                        transformY = transformY,
                        spriteScreenX = spriteScreenX,
                        spriteHeight = spriteHeight,
                        spriteWidth = spriteWidth,
                        drawEntity = entity
                    )
                )
            }
        }

        // Add Wall Markers
        for (marker in wallMarkers) {
            val spriteX = marker.x - posX
            val spriteY = marker.y - posY
            val invDet = 1.0f / (planeX * dirY - dirX * planeY)
            val transformX = invDet * (dirY * spriteX - dirX * spriteY)
            val transformY = invDet * (-planeY * spriteX + planeX * spriteY)

            if (transformY > 0.2f) {
                val spriteScreenX = (w / 2f) * (1f + transformX / transformY)
                val spriteHeight = abs(h / transformY) * 0.35f
                val spriteWidth = spriteHeight * 0.5f
                sprites.add(
                    SpriteToRender(
                        x = marker.x,
                        y = marker.y,
                        dist = transformY,
                        transformY = transformY,
                        spriteScreenX = spriteScreenX,
                        spriteHeight = spriteHeight,
                        spriteWidth = spriteWidth,
                        drawMarker = marker
                    )
                )
            }
        }

        // Sort sprites from furthest to closest (Painter's algorithm)
        sprites.sortByDescending { it.dist }

        val colWidth = w / numRays

        for (sprite in sprites) {
            val spriteCol = (sprite.spriteScreenX / colWidth).toInt()
            // Check if center ray is visible behind zBuffer
            if (spriteCol in 0 until numRays && sprite.dist < zBuffer[spriteCol] + 0.3f) {
                val drawY = (h / 2f) - (sprite.spriteHeight / 2f) + (if (sprite.drawItem != null) sprite.spriteHeight * 0.4f else 0f)

                if (sprite.drawEntity != null) {
                    renderEntitySprite(drawScope, sprite, drawY, flashlightOn, flashlightBattery, flickerIntensity)
                } else if (sprite.drawItem != null) {
                    renderItemSprite(drawScope, sprite, drawY)
                } else if (sprite.drawMarker != null) {
                    renderMarkerSprite(drawScope, sprite, drawY)
                }
            }
        }
    }

    private fun renderEntitySprite(
        drawScope: DrawScope,
        sprite: SpriteToRender,
        topY: Float,
        flashlightOn: Boolean,
        flashlightBattery: Float,
        flickerIntensity: Float
    ) {
        val entity = sprite.drawEntity ?: return
        val centerX = sprite.spriteScreenX
        val halfW = sprite.spriteWidth / 2f
        val sh = sprite.spriteHeight

        // Distance fog
        val maxDist = if (flashlightOn && flashlightBattery > 0.05f) 11.0f else 6.5f
        val lightAlpha = (1.0f - sprite.dist / maxDist).coerceIn(0.1f, 1.0f) * flickerIntensity

        when (entity.type) {
            EntityType.BACTERIA -> {
                // Tall tangled wire humanoid with red eyes (Kane Pixels style)
                drawScope.drawRect(
                    color = Color.Black.copy(alpha = lightAlpha),
                    topLeft = Offset(centerX - halfW * 0.4f, topY),
                    size = Size(halfW * 0.8f, sh)
                )
                // Wires / tentacles branching
                drawScope.drawLine(
                    color = Color(0xFF221F1A).copy(alpha = lightAlpha),
                    start = Offset(centerX, topY + sh * 0.3f),
                    end = Offset(centerX - halfW * 1.1f, topY + sh * 0.7f),
                    strokeWidth = (sh * 0.035f).coerceAtLeast(2f)
                )
                drawScope.drawLine(
                    color = Color(0xFF221F1A).copy(alpha = lightAlpha),
                    start = Offset(centerX, topY + sh * 0.4f),
                    end = Offset(centerX + halfW * 1.1f, topY + sh * 0.8f),
                    strokeWidth = (sh * 0.035f).coerceAtLeast(2f)
                )
                // Ominous Glowing Red Eye
                val eyeY = topY + sh * 0.15f
                val eyeSize = (sh * 0.045f).coerceAtLeast(3f)
                drawScope.drawCircle(
                    color = Color(0xFFFF2222),
                    radius = eyeSize,
                    center = Offset(centerX, eyeY)
                )
            }
            EntityType.SMILER -> {
                // Dark shadowy void with bright glowing smile and white eyes
                drawScope.drawCircle(
                    color = Color.Black.copy(alpha = 0.95f),
                    radius = halfW * 0.9f,
                    center = Offset(centerX, topY + sh * 0.4f)
                )
                val eyeY = topY + sh * 0.32f
                val eyeRad = (sh * 0.04f).coerceAtLeast(3f)
                // Left & Right glowing white eyes
                drawScope.drawCircle(color = Color.White, radius = eyeRad, center = Offset(centerX - halfW * 0.35f, eyeY))
                drawScope.drawCircle(color = Color.White, radius = eyeRad, center = Offset(centerX + halfW * 0.35f, eyeY))
                // Huge sharp glowing toothy smile arc
                val smileY = topY + sh * 0.45f
                drawScope.drawArc(
                    color = Color.White,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - halfW * 0.5f, smileY),
                    size = Size(halfW, sh * 0.15f)
                )
            }
            EntityType.HOUND -> {
                // Low, feral quadruped beast with amber eyes
                drawScope.drawOval(
                    color = Color(0xFF261A15).copy(alpha = lightAlpha),
                    topLeft = Offset(centerX - halfW * 0.8f, topY + sh * 0.35f),
                    size = Size(halfW * 1.6f, sh * 0.55f)
                )
                // Amber eyes
                val eyeY = topY + sh * 0.45f
                drawScope.drawCircle(color = Color(0xFFFFAB00), radius = 3.5f, center = Offset(centerX - halfW * 0.25f, eyeY))
                drawScope.drawCircle(color = Color(0xFFFFAB00), radius = 3.5f, center = Offset(centerX + halfW * 0.25f, eyeY))
            }
            EntityType.HORDE -> {
                // Massive towering crimson nightmare silhouette
                drawScope.drawRect(
                    color = Color(0xFF8E0000).copy(alpha = 0.95f),
                    topLeft = Offset(centerX - halfW, topY),
                    size = Size(halfW * 2f, sh)
                )
                // Glowing vicious red eyes
                val eyeY = topY + sh * 0.2f
                drawScope.drawCircle(color = Color(0xFFFF1744), radius = 6f, center = Offset(centerX - halfW * 0.4f, eyeY))
                drawScope.drawCircle(color = Color(0xFFFF1744), radius = 6f, center = Offset(centerX + halfW * 0.4f, eyeY))
            }
        }
    }

    private fun renderItemSprite(drawScope: DrawScope, sprite: SpriteToRender, topY: Float) {
        val item = sprite.drawItem ?: return
        val centerX = sprite.spriteScreenX
        val halfW = sprite.spriteWidth / 2f
        val sh = sprite.spriteHeight

        when (item.type) {
            ItemType.ALMOND_WATER -> {
                // Almond water glowing aqua bottle
                drawScope.drawRect(
                    color = Color(0xFF4DD0E1),
                    topLeft = Offset(centerX - halfW * 0.3f, topY),
                    size = Size(halfW * 0.6f, sh * 0.8f)
                )
                // Bottle cap
                drawScope.drawRect(
                    color = Color.White,
                    topLeft = Offset(centerX - halfW * 0.2f, topY - sh * 0.15f),
                    size = Size(halfW * 0.4f, sh * 0.15f)
                )
            }
            ItemType.BATTERY -> {
                // Golden lithium battery cylinder
                drawScope.drawRect(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(centerX - halfW * 0.35f, topY),
                    size = Size(halfW * 0.7f, sh * 0.7f)
                )
                // Plus terminal
                drawScope.drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(centerX - halfW * 0.15f, topY - sh * 0.12f),
                    size = Size(halfW * 0.3f, sh * 0.12f)
                )
            }
            ItemType.CHALK -> {
                // White stick of chalk
                drawScope.drawRect(
                    color = Color(0xFFF5F5F5),
                    topLeft = Offset(centerX - halfW * 0.2f, topY),
                    size = Size(halfW * 0.4f, sh * 0.6f)
                )
            }
            ItemType.SURVIVOR_TAPE -> {
                // Orange/black VHS cassette
                drawScope.drawRect(
                    color = Color(0xFF212121),
                    topLeft = Offset(centerX - halfW * 0.6f, topY),
                    size = Size(halfW * 1.2f, sh * 0.5f)
                )
                drawScope.drawRect(
                    color = Color(0xFFFF7043),
                    topLeft = Offset(centerX - halfW * 0.4f, topY + sh * 0.1f),
                    size = Size(halfW * 0.8f, sh * 0.25f)
                )
            }
        }
    }

    private fun renderMarkerSprite(drawScope: DrawScope, sprite: SpriteToRender, topY: Float) {
        val centerX = sprite.spriteScreenX
        // Draw white chalk arrow mark on floor / wall
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = (sprite.spriteWidth * 0.3f).coerceAtLeast(3f),
            center = Offset(centerX, topY + sprite.spriteHeight * 0.5f)
        )
    }

    private fun renderVhsOverlay(drawScope: DrawScope, w: Float, h: Float, distortion: Float) {
        // Subtle horizontal scanlines
        var y = 0f
        val lineSpacing = 6f
        while (y < h) {
            drawScope.drawLine(
                color = Color.Black.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += lineSpacing
        }

        // Noise & Static tracking glitches when near entity
        if (distortion > 0.05f) {
            val glitchCount = (distortion * 18).toInt()
            repeat(glitchCount) {
                val gy = (0..h.toInt()).random().toFloat()
                val gh = (2..12).random().toFloat()
                val galpha = (distortion * 0.4f).coerceIn(0.05f, 0.45f)
                drawScope.drawRect(
                    color = if (it % 2 == 0) Color.White.copy(alpha = galpha) else Color.Red.copy(alpha = galpha * 0.7f),
                    topLeft = Offset(0f, gy),
                    size = Size(w, gh)
                )
            }
        }
    }
}
