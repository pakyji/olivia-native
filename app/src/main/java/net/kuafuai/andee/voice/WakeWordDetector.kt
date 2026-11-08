package net.kuafuai.andee.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * WakeWordDetector - Offline wake word detection (Andee-style MFCC + DTW).
 * Detects "Olivia" without cloud dependency.
 */
class WakeWordDetector(
    private val context: Context,
    private val onDetected: () -> Unit
) {

    companion object {
        const val TAG = "WakeWordDetector"
        const val WAKE_WORD = "olivia"
        const val SAMPLE_RATE = 16000
        const val FRAME_SIZE = 512
        const val HOP_SIZE = 160
        const val MFCC_COEFFS = 13
        const val TEMPLATE_FRAMES = 50
        const val DTW_THRESHOLD = 0.85
    }

    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private val buffer = ShortArray(FRAME_SIZE)
    private val mfccExtractor = MfccExtractor()
    private var template: Array<DoubleArray>? = null

    init {
        loadOrCreateTemplate()
    }

    fun start() {
        if (isRunning) return
        isRunning = true

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize.coerceAtLeast(FRAME_SIZE * 4)
        )

        audioRecord?.startRecording()

        scope.launch {
            detectionLoop()
        }
        Log.d(TAG, "Wake word detection started")
    }

    fun stop() {
        isRunning = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        Log.d(TAG, "Wake word detection stopped")
    }

    private fun detectionLoop() {
        while (isRunning) {
            val read = audioRecord?.read(buffer, 0, FRAME_SIZE) ?: 0
            if (read <= 0) continue

            // Convert to float array
            val floatBuffer = buffer.map { it.toDouble() / 32768.0 }.toDoubleArray()

            // Extract MFCC
            val mfcc = mfccExtractor.extract(floatBuffer)
            if (mfcc == null) continue

            // Compare with template using DTW
            val score = dtwScore(mfcc)
            if (score > DTW_THRESHOLD) {
                Log.d(TAG, "Wake word detected! Score: $score")
                onDetected()
                // Brief pause to avoid re-triggering
                Thread.sleep(2000)
            }
        }
    }

    private fun dtwScore(input: DoubleArray): Double {
        val template = this.template ?: return 0.0
        val n = input.size
        val m = template.size

        if (n == 0 || m == 0) return 0.0

        // DTW distance matrix
        val dtw = Array(n + 1) { DoubleArray(m + 1) { Double.POSITIVE_INFINITY } }
        dtw[0][0] = 0.0

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = Math.abs(input[i - 1] - template[j - 1])
                dtw[i][j] = cost + minOf(
                    dtw[i - 1][j],     // insertion
                    dtw[i][j - 1],     // deletion
                    dtw[i - 1][j - 1]  // match
                )
            }
        }

        // Normalize by path length
        val pathLength = n + m
        val distance = dtw[n][m] / pathLength

        // Convert to similarity score (0-1)
        return 1.0 / (1.0 + distance)
    }

    private fun loadOrCreateTemplate() {
        // TODO: Load from file or generate from training samples
        // For now, create a placeholder template
        template = Array(TEMPLATE_FRAMES) { DoubleArray(MFCC_COEFFS) { 0.0 } }

        // Try to load from files
        try {
            val filesDir = context.filesDir
            val templateFile = java.io.File(filesDir, "wake_template.bin")
            if (templateFile.exists()) {
                val input = java.io.DataInputStream(java.io.FileInputStream(templateFile))
                val frames = input.readInt()
                val coeffs = input.readInt()
                template = Array(frames) { DoubleArray(coeffs) { input.readDouble() } }
                input.close()
                Log.d(TAG, "Loaded wake word template: $frames frames x $coeffs coeffs")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load wake word template: ${e.message}")
        }
    }

    fun saveTemplate() {
        template?.let { t ->
            try {
                val filesDir = context.filesDir
                val templateFile = java.io.File(filesDir, "wake_template.bin")
                val output = java.io.DataOutputStream(java.io.FileOutputStream(templateFile))
                output.writeInt(t.size)
                output.writeInt(t[0].size)
                for (frame in t) {
                    for (coeff in frame) {
                        output.writeDouble(coeff)
                    }
                }
                output.close()
                Log.d(TAG, "Saved wake word template")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save template: ${e.message}")
            }
        }
    }
}

/**
 * Simple MFCC extractor for wake word detection.
 */
class MfccExtractor {
    private val sampleRate = WakeWordDetector.SAMPLE_RATE
    private val frameSize = WakeWordDetector.FRAME_SIZE
    private val numFilters = 26
    private val numCoeffs = WakeWordDetector.MFCC_COEFFS

    private val melFilters: Array<DoubleArray> = initializeMelFilters()
    private val dctMatrix: Array<DoubleArray> = initializeDctMatrix()

    fun extract(audioFrame: DoubleArray): DoubleArray? {
        if (audioFrame.size != frameSize) return null

        // Apply pre-emphasis
        val emphasized = DoubleArray(frameSize)
        emphasized[0] = audioFrame[0]
        for (i in 1 until frameSize) {
            emphasized[i] = audioFrame[i] - 0.97 * audioFrame[i - 1]
        }

        // Apply Hamming window
        val windowed = DoubleArray(frameSize)
        for (i in 0 until frameSize) {
            windowed[i] = emphasized[i] * (0.54 - 0.46 * Math.cos(2 * Math.PI * i / (frameSize - 1)))
        }

        // FFT (simplified - using real FFT would be better)
        val spectrum = computePowerSpectrum(windowed)

        // Apply mel filters
        val melEnergies = DoubleArray(numFilters)
        for (i in 0 until numFilters) {
            var sum = 0.0
            for (j in 0 until spectrum.size) {
                sum += spectrum[j] * melFilters[i][j]
            }
            melEnergies[i] = Math.max(sum, 1e-10).log()
        }

        // DCT to get MFCCs
        val mfcc = DoubleArray(numCoeffs)
        for (i in 0 until numCoeffs) {
            var sum = 0.0
            for (j in 0 until numFilters) {
                sum += melEnergies[j] * dctMatrix[i][j]
            }
            mfcc[i] = sum
        }

        return mfcc
    }

    private fun computePowerSpectrum(signal: DoubleArray): DoubleArray {
        // Simplified FFT - in production use a proper FFT library
        val n = signal.size
        val spectrum = DoubleArray(n / 2)
        for (k in 0 until n / 2) {
            var real = 0.0
            var imag = 0.0
            for (t in 0 until n) {
                val angle = 2 * Math.PI * k * t / n
                real += signal[t] * Math.cos(angle)
                imag -= signal[t] * Math.sin(angle)
            }
            spectrum[k] = real * real + imag * imag
        }
        return spectrum
    }

    private fun initializeMelFilters(): Array<DoubleArray> {
        val filters = Array(numFilters) { DoubleArray(frameSize / 2) { 0.0 } }
        val minFreq = 0.0
        val maxFreq = sampleRate / 2.0
        val minMel = freqToMel(minFreq)
        val maxMel = freqToMel(maxFreq)

        for (i in 0 until numFilters) {
            val centerMel = minMel + (i + 1) * (maxMel - minMel) / (numFilters + 1)
            val leftMel = minMel + i * (maxMel - minMel) / (numFilters + 1)
            val rightMel = minMel + (i + 2) * (maxMel - minMel) / (numFilters + 1)

            val centerFreq = melToFreq(centerMel)
            val leftFreq = melToFreq(leftMel)
            val rightFreq = melToFreq(rightMel)

            val centerBin = (centerFreq * frameSize / sampleRate).toInt()
            val leftBin = (leftFreq * frameSize / sampleRate).toInt()
            val rightBin = (rightFreq * frameSize / sampleRate).toInt()

            for (j in leftBin..centerBin) {
                if (centerBin > leftBin) {
                    filters[i][j] = (j - leftBin).toDouble() / (centerBin - leftBin)
                }
            }
            for (j in centerBin..rightBin) {
                if (rightBin > centerBin) {
                    filters[i][j] = (rightBin - j).toDouble() / (rightBin - centerBin)
                }
            }
        }
        return filters
    }

    private fun initializeDctMatrix(): Array<DoubleArray> {
        val dct = Array(numCoeffs) { DoubleArray(numFilters) { 0.0 } }
        for (i in 0 until numCoeffs) {
            for (j in 0 until numFilters) {
                dct[i][j] = Math.cos(Math.PI * i * (j + 0.5) / numFilters)
            }
        }
        return dct
    }

    private fun freqToMel(freq: Double): Double {
        return 2595.0 * Math.log10(1.0 + freq / 700.0)
    }

    private fun melToFreq(mel: Double): Double {
        return 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0)
    }
}