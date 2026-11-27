package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SwarmViolet
import com.example.ui.theme.TankCrimson

/**
 * Data-driven enemy archetypes (Android Kotlin equivalent of Unity Enemy ScriptableObjects).
 */
enum class EnemyArchetype(
    val displayName: String,
    val baseHp: Float,
    val baseSpeed: Float,
    val contactDamage: Float,
    val radius: Float,
    val xpValue: Int,
    val color: Color,
    val orbTier: Int
) {
    SWARM(
        displayName = "Sürü Botu",
        baseHp = 34f,
        baseSpeed = 136f,
        contactDamage = 16f,
        radius = 15f,
        xpValue = 12,
        color = SwarmViolet,
        orbTier = 0
    ),
    RUNNER(
        displayName = "Hızlı Koşucu",
        baseHp = 30f,
        baseSpeed = 224f,
        contactDamage = 22f,
        radius = 13f,
        xpValue = 18,
        color = HotMagenta,
        orbTier = 1
    ),
    TANK(
        displayName = "Ağır Bas Tankı",
        baseHp = 165f,
        baseSpeed = 86f,
        contactDamage = 30f,
        radius = 25f,
        xpValue = 42,
        color = TankCrimson,
        orbTier = 1
    ),
    ELITE_BOSS(
        displayName = "Ritim Muhafızı",
        baseHp = 680f,
        baseSpeed = 118f,
        contactDamage = 38f,
        radius = 36f,
        xpValue = 160,
        color = SolarGold,
        orbTier = 2
    ),
    CORRUPTED_CORE(
        displayName = "Bozuk Ses Çekirdeği",
        baseHp = 160f,
        baseSpeed = 0f,
        contactDamage = 22f,
        radius = 24f,
        xpValue = 48,
        color = TankCrimson,
        orbTier = 2
    )
}

/**
 * Escalating wave table supporting extended multi-mission stages.
 */
data class WaveDefinition(
    val waveNumber: Int,
    val startSeconds: Float,
    val spawnIntervalSeconds: Float,
    val batchSize: Int,
    val hpMultiplier: Float,
    val swarmWeight: Float,
    val runnerWeight: Float,
    val tankWeight: Float
)

object WaveSchedule {
    const val VICTORY_DURATION_SECONDS = 480f // 8-minute cap for extended multi-mission stages

    val waves: List<WaveDefinition> = listOf(
        WaveDefinition(
            waveNumber = 1,
            startSeconds = 0f,
            spawnIntervalSeconds = 0.80f,
            batchSize = 3,
            hpMultiplier = 1.12f,
            swarmWeight = 0.76f,
            runnerWeight = 0.24f,
            tankWeight = 0.0f
        ),
        WaveDefinition(
            waveNumber = 2,
            startSeconds = 35f,
            spawnIntervalSeconds = 0.66f,
            batchSize = 3,
            hpMultiplier = 1.45f,
            swarmWeight = 0.50f,
            runnerWeight = 0.38f,
            tankWeight = 0.12f
        ),
        WaveDefinition(
            waveNumber = 3,
            startSeconds = 85f,
            spawnIntervalSeconds = 0.56f,
            batchSize = 4,
            hpMultiplier = 1.85f,
            swarmWeight = 0.38f,
            runnerWeight = 0.40f,
            tankWeight = 0.22f
        ),
        WaveDefinition(
            waveNumber = 4,
            startSeconds = 150f,
            spawnIntervalSeconds = 0.46f,
            batchSize = 5,
            hpMultiplier = 2.40f,
            swarmWeight = 0.32f,
            runnerWeight = 0.40f,
            tankWeight = 0.28f
        ),
        WaveDefinition(
            waveNumber = 5,
            startSeconds = 225f,
            spawnIntervalSeconds = 0.38f,
            batchSize = 6,
            hpMultiplier = 3.20f,
            swarmWeight = 0.26f,
            runnerWeight = 0.40f,
            tankWeight = 0.34f
        )
    )

    val eliteSpawnTimestamps = floatArrayOf(95f, 175f, 255f, 340f)

    fun currentWave(elapsedSeconds: Float): WaveDefinition {
        var active = waves.first()
        for (wave in waves) {
            if (elapsedSeconds >= wave.startSeconds) {
                active = wave
            }
        }
        return active
    }
}
