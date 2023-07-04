package com.upc.mobilitappv2.multimodal.FFT

import kotlin.math.cos
import kotlin.math.sin


/**
 * Utility class for computing Fast Fourier Transform (FFT) and related operations.
 *
 * @author Adrian Catalin
 */
object FFT {

    /**
     * Computes the FFT of an array of complex numbers assuming its length is a power of 2.
     *
     * @param x The input array of complex numbers.
     * @return The FFT of the input array.
     * @throws IllegalArgumentException if the length of the input array is not a power of 2.
     */
    fun fft(x: Array<Complex?>): Array<Complex?> {
        val n = x.size

        // base case
        if (n == 1) return arrayOf(x[0])

        // radix 2 Cooley-Tukey FFT
        require(n % 2 == 0) { "n is not a power of 2" }

        // compute FFT of even terms
        val even = arrayOfNulls<Complex>(n / 2)
        for (k in 0 until n / 2) {
            even[k] = x[2 * k]
        }
        val evenFFT: Array<Complex?> = com.upc.mobilitappv2.multimodal.FFT.FFT.fft(even)

        // compute FFT of odd terms
        for (k in 0 until n / 2) {
            even[k] = x[2 * k + 1]
        }
        val oddFFT: Array<Complex?> = com.upc.mobilitappv2.multimodal.FFT.FFT.fft(even)

        // combine
        val y = arrayOfNulls<Complex>(n)
        for (k in 0 until n / 2) {
            val kth = -2 * k * Math.PI / n
            val wk = Complex(cos(kth), sin(kth))
            y[k] = evenFFT[k]?.plus(wk.times(oddFFT[k]!!))
            y[k + n / 2] = evenFFT[k]?.minus(wk.times(oddFFT[k]!!))
        }
        return y
    }

}