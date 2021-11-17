import ddf.minim.Minim
import ddf.minim.analysis.FFT
import minim.MinimObject
import org.openrndr.application

fun main() {
    application {

        configure {
            width = 1280
            height = 720

        }

        program {
            val minim = Minim(MinimObject())
            minim.getLineIn(1)
            val lineIn = minim.lineIn
            lineIn.enableMonitoring()
            println(lineIn.bufferSize())
            val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())

            extend {
                fft.forward(lineIn.mix)
                for (i in 0 until fft.specSize()) {
                    val y = fft.getBand(i)
                    drawer.circle(i*2.0, height/2.0 - y*10.0, 5.0)

                }
            }

        }


    }
}