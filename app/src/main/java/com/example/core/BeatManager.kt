package com.example.core

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Drift-free hardware-clock BPM engine paired with an ultra-lightweight 4-stem procedural
 * PCM synth engine (Drums, Acid Bass, Neon Arp Melody, Overdrive Chords).
 *
 * Uses a precomputed 2048-entry sine LUT to cut CPU usage by ~6x while preserving punchy audio,
 * leaving maximum CPU budget for 50-60+ FPS gameplay rendering.
 */
class BeatManager(
    val bpm: Int = 128
) {
    val secondsPerBeat: Double = 60.0 / bpm.toDouble()
    private val sampleRate = 16000
    private val samplesPerBeat: Double = sampleRate * secondsPerBeat
    private val samplesPerStep: Double = samplesPerBeat / 4.0

    // Precomputed sine lookup table for ultra-fast audio synthesis
    private val lutSize = 2048
    private val lutMask = lutSize - 1
    private val sinLut = FloatArray(lutSize) { idx ->
        sin(2.0 * PI * idx / lutSize).toFloat()
    }

    @Volatile
    var currentBeatProgress: Float = 0f
        private set

    @Volatile
    var beatIndex: Int = -1
        private set

    @Volatile
    var isMuted: Boolean = false
        private set

    @Volatile
    private var targetDrumGain: Float = 1.0f

    @Volatile
    private var targetBassGain: Float = 0.0f

    @Volatile
    private var targetMelodyGain: Float = 0.0f

    @Volatile
    private var targetOverdriveGain: Float = 0.0f

    @Volatile
    var drumLevel: Float = 1.0f
        private set

    @Volatile
    var bassLevel: Float = 0.0f
        private set

    @Volatile
    var melodyLevel: Float = 0.0f
        private set

    @Volatile
    var overdriveLevel: Float = 0.0f
        private set

    @Volatile
    private var pendingShockwaveSfx = false

    @Volatile
    private var pendingOrbSfx = false

    @Volatile
    private var pendingLevelUpSfx = false

    @Volatile
    private var pendingEvolutionSfx = false

    @Volatile
    private var pendingDamageSfx = false

    private var clockStartNanos: Long = 0L
    private var pausedAccumulatedNanos: Long = 0L
    private var pauseStartNanos: Long = 0L
    private var isRunning: Boolean = false
    private var isPaused: Boolean = false
    private var lastDispatchedBeat: Int = -1

    @Volatile
    private var audioActive = false
    @Volatile
    private var audioPaused = false
    private var audioThread: Thread? = null
    private var audioTrack: AudioTrack? = null

    private var onBeatListener: ((Int) -> Unit)? = null

    fun setOnBeatListener(listener: (Int) -> Unit) {
        onBeatListener = listener
    }

    fun start() {
        stop()
        val now = System.nanoTime()
        clockStartNanos = now
        pausedAccumulatedNanos = 0L
        pauseStartNanos = 0L
        lastDispatchedBeat = -1
        beatIndex = -1
        currentBeatProgress = 0f
        isRunning = true
        isPaused = false
        audioPaused = false
        updateComboStems(0)
        startAudioSynth()
    }

    fun pause() {
        if (!isRunning || isPaused) return
        isPaused = true
        audioPaused = true
        pauseStartNanos = System.nanoTime()
    }

    fun resume() {
        if (!isRunning || !isPaused) return
        val now = System.nanoTime()
        pausedAccumulatedNanos += (now - pauseStartNanos)
        isPaused = false
        audioPaused = false
    }

    fun stop() {
        isRunning = false
        isPaused = false
        audioActive = false
        audioPaused = false
        try {
            audioThread?.interrupt()
            audioThread = null
        } catch (_: Throwable) {
        }
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Throwable) {
        } finally {
            audioTrack = null
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun updateComboStems(combo: Int) {
        targetDrumGain = 1.0f
        targetBassGain = when {
            combo >= 12 -> 1.0f
            combo >= 5 -> 0.55f + (combo - 5) * 0.06f
            else -> 0.12f
        }
        targetMelodyGain = when {
            combo >= 25 -> 1.0f
            combo >= 15 -> 0.45f + (combo - 15) * 0.055f
            else -> 0.0f
        }
        targetOverdriveGain = when {
            combo >= 40 -> 1.0f
            combo >= 30 -> 0.45f + (combo - 30) * 0.055f
            else -> 0.0f
        }
    }

    fun triggerShockwaveSfx() {
        pendingShockwaveSfx = true
    }

    fun triggerOrbSfx() {
        pendingOrbSfx = true
    }

    fun triggerLevelUpSfx() {
        pendingLevelUpSfx = true
    }

    fun triggerEvolutionSfx() {
        pendingEvolutionSfx = true
    }

    fun triggerDamageSfx() {
        pendingDamageSfx = true
    }

    fun updateClock(nowNanos: Long = System.nanoTime()) {
        if (!isRunning || isPaused) return
        val elapsedSeconds = (nowNanos - clockStartNanos - pausedAccumulatedNanos) / 1_000_000_000.0
        if (elapsedSeconds < 0.0) return

        val exactBeats = elapsedSeconds / secondsPerBeat
        val currentIdx = exactBeats.toInt()
        currentBeatProgress = (exactBeats - currentIdx).toFloat().coerceIn(0f, 1f)

        drumLevel += (targetDrumGain - drumLevel) * 0.14f
        bassLevel += (targetBassGain - bassLevel) * 0.08f
        melodyLevel += (targetMelodyGain - melodyLevel) * 0.08f
        overdriveLevel += (targetOverdriveGain - overdriveLevel) * 0.08f

        if (currentIdx > lastDispatchedBeat) {
            val startBeat = if (lastDispatchedBeat < 0) currentIdx else lastDispatchedBeat + 1
            for (b in startBeat..currentIdx) {
                beatIndex = b
                onBeatListener?.invoke(b)
            }
            lastDispatchedBeat = currentIdx
        }
    }

    private inline fun fastSin(turns: Float): Float {
        val idx = (turns * lutSize).toInt() and lutMask
        return sinLut[idx]
    }

    private fun startAudioSynth() {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return

            val bufferSamples = (minBuf / 2).coerceAtLeast(2048)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSamples * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            audioActive = true
            track.play()

            val chunkSamples = 2048
            val pcmBuffer = ShortArray(chunkSamples)

            audioThread = Thread({
                renderAudioLoop(track, pcmBuffer)
            }, "SynvexSynthThread").apply {
                priority = Thread.NORM_PRIORITY - 1
                isDaemon = true
                start()
            }
        } catch (_: Throwable) {
            audioActive = false
        }
    }

    private fun renderAudioLoop(track: AudioTrack, pcm: ShortArray) {
        val bassNotes = floatArrayOf(
            73.42f, 73.42f, 0f, 73.42f,
            87.31f, 0f, 98.00f, 73.42f,
            65.41f, 65.41f, 0f, 65.41f,
            82.41f, 0f, 98.00f, 110.00f
        )
        val arpNotes = floatArrayOf(
            293.66f, 349.23f, 440.00f, 523.25f,
            587.33f, 440.00f, 349.23f, 392.00f,
            261.63f, 329.63f, 392.00f, 523.25f,
            440.00f, 392.00f, 349.23f, 329.63f
        )
        val padChord = floatArrayOf(146.83f, 220.00f, 261.63f, 349.23f)

        var sampleCursor = 0L
        var smoothBass = 0.12f
        var smoothMelody = 0.0f
        var smoothOverdrive = 0.0f
        var noiseSeed = 133742

        var shockwaveEnv = 0f
        var shockwaveTurns = 0f
        var orbEnv = 0f
        var orbTurns = 0f
        var levelEnv = 0f
        var levelTurns = 0f
        var evoEnv = 0f
        var evoTurns = 0f
        var dmgEnv = 0f
        var dmgTurns = 0f

        val invSampleRate = 1f / sampleRate
        val invSamplesPerStep = (1.0 / samplesPerStep)
        val invSamplesPerBeat = (1.0 / samplesPerBeat)
        val secPerBeatF = secondsPerBeat.toFloat()

        while (audioActive && !Thread.currentThread().isInterrupted) {
            if (audioPaused) {
                try {
                    Thread.sleep(30)
                } catch (_: InterruptedException) {
                    break
                }
                continue
            }

            if (pendingShockwaveSfx) {
                pendingShockwaveSfx = false
                shockwaveEnv = 1f
                shockwaveTurns = 0f
            }
            if (pendingOrbSfx) {
                pendingOrbSfx = false
                orbEnv = 0.85f
                orbTurns = 0f
            }
            if (pendingLevelUpSfx) {
                pendingLevelUpSfx = false
                levelEnv = 1f
                levelTurns = 0f
            }
            if (pendingEvolutionSfx) {
                pendingEvolutionSfx = false
                evoEnv = 1f
                evoTurns = 0f
            }
            if (pendingDamageSfx) {
                pendingDamageSfx = false
                dmgEnv = 0.9f
                dmgTurns = 0f
            }

            val muted = isMuted
            val tgtBass = targetBassGain
            val tgtMel = targetMelodyGain
            val tgtOvd = targetOverdriveGain
            val drumGain = targetDrumGain

            if (muted) {
                pcm.fill(0)
                sampleCursor += pcm.size
            } else {
                for (i in pcm.indices) {
                    smoothBass += (tgtBass - smoothBass) * 0.0008f
                    smoothMelody += (tgtMel - smoothMelody) * 0.0008f
                    smoothOverdrive += (tgtOvd - smoothOverdrive) * 0.0008f

                    val stepFloat = sampleCursor * invSamplesPerStep
                    val stepLong = stepFloat.toLong()
                    val stepIndex = (stepLong and 15L).toInt()
                    val stepProgress = (stepFloat - stepLong).toFloat()

                    val beatFloat = sampleCursor * invSamplesPerBeat
                    val beatLong = beatFloat.toLong()
                    val beatInBar = (beatLong and 3L).toInt()
                    val beatProgress = (beatFloat - beatLong).toFloat()

                    noiseSeed = noiseSeed * 1664525 + 1013904223
                    val noise = (((noiseSeed shr 16) and 0x7FFF) * (1f / 16384f)) - 1f

                    // --- STEM 1: DRUMS (zero transcendental math in hot audio loop) ---
                    val kickTime = beatProgress * secPerBeatF
                    val kickNorm = (1f - kickTime * 3.8f).coerceAtLeast(0f)
                    val kickEnv = kickNorm * kickNorm * kickNorm
                    val kick = if (kickEnv > 0.004f) {
                        val kickFreq = 48f + 120f * kickEnv * kickEnv
                        fastSin(kickFreq * kickTime) * kickEnv * 0.50f
                    } else 0f

                    val snare = if ((beatInBar == 1 || beatInBar == 3) && kickTime < 0.22f) {
                        val sNorm = (1f - kickTime * 4.55f).coerceAtLeast(0f)
                        val snareEnv = sNorm * sNorm * sNorm
                        (noise * 0.65f + fastSin(195f * kickTime) * 0.35f) * snareEnv * 0.32f
                    } else 0f

                    val hatEnv = (1f - stepProgress * 3.5f).coerceAtLeast(0f)
                    val hat = if (hatEnv > 0f) {
                        noise * hatEnv * hatEnv * if ((stepIndex and 1) == 1) 0.13f else 0.06f
                    } else 0f

                    var total = (kick + snare + hat) * drumGain

                    val t = (sampleCursor and 0xFFFFFFL) * invSampleRate

                    // --- STEM 2: ACID BASS ---
                    val bassFreq = bassNotes[stepIndex]
                    if (bassFreq > 0f && smoothBass > 0.02f) {
                        val phase = (t * bassFreq) % 1f
                        val saw = 2f * phase - 1f
                        val sub = fastSin(bassFreq * 0.5f * t)
                        val env = (1f - stepProgress).let { it * it }
                        total += (saw * 0.55f + sub * 0.45f) * env * 0.28f * smoothBass
                    }

                    // --- STEM 3: MELODY ARP ---
                    if (smoothMelody > 0.02f) {
                        val arpFreq = arpNotes[stepIndex]
                        val env = (1f - stepProgress).let { it * it * it }
                        val tone = fastSin(arpFreq * t) + 0.3f * fastSin(arpFreq * 2f * t)
                        total += tone * env * 0.19f * smoothMelody
                    }

                    // --- STEM 4: OVERDRIVE CHORD ---
                    if (smoothOverdrive > 0.02f) {
                        val chordSum = (
                            fastSin(padChord[0] * t) +
                                fastSin(padChord[1] * t) +
                                fastSin(padChord[2] * t) +
                                fastSin(padChord[3] * t)
                            ) * 0.25f
                        total += chordSum * beatProgress * 0.21f * smoothOverdrive
                    }

                    // --- SFX ---
                    if (shockwaveEnv > 0.005f) {
                        shockwaveTurns += (75f + 180f * shockwaveEnv) * invSampleRate
                        total += fastSin(shockwaveTurns) * shockwaveEnv * 0.26f
                        shockwaveEnv *= 0.9991f
                    }
                    if (orbEnv > 0.005f) {
                        orbTurns += (880f + 440f * (1f - orbEnv)) * invSampleRate
                        total += fastSin(orbTurns) * orbEnv * 0.15f
                        orbEnv *= 0.9975f
                    }
                    if (levelEnv > 0.005f) {
                        levelTurns += (523.25f * (1f + (1f - levelEnv) * 0.5f)) * invSampleRate
                        total += (fastSin(levelTurns) + 0.35f * fastSin(levelTurns * 1.5f)) * levelEnv * 0.22f
                        levelEnv *= 0.9994f
                    }
                    if (evoEnv > 0.005f) {
                        evoTurns += (440f + 440f * (1f - evoEnv)) * invSampleRate
                        total += (fastSin(evoTurns) + 0.45f * fastSin(evoTurns * 2f)) * evoEnv * 0.25f
                        evoEnv *= 0.9996f
                    }
                    if (dmgEnv > 0.005f) {
                        dmgTurns += 110f * invSampleRate
                        total += (fastSin(dmgTurns) + noise * 0.3f) * dmgEnv * 0.22f
                        dmgEnv *= 0.9985f
                    }

                    pcm[i] = (total.coerceIn(-0.96f, 0.96f) * 32767f).toInt().toShort()
                    sampleCursor++
                }
            }

            try {
                track.write(pcm, 0, pcm.size)
            } catch (_: Throwable) {
                break
            }
        }
    }
}
