import ddf.minim.Minim
import ddf.minim.analysis.FFT
import ddf.minim.analysis.HammingWindow
import ddf.minim.analysis.LanczosWindow
import ddf.minim.analysis.WindowFunction
import minim.MinimObject
import org.openrndr.application
import org.openrndr.math.map

fun main() {
    application {

        configure {
            width = 1280
            height = 720

        }

        program {
            val minim = Minim(MinimObject())
            val lineIn = minim.getLineIn(Minim.MONO, 2048, 48000f)
            val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
            fft.window(LanczosWindow())
            ended.listen {
                minim.stop()
            }

            extend {
                fft.forward(lineIn.mix)
                for (i in 0 until 200) {
                    val bandDB = 20.0 * Math.log(2.0*fft.getBand(i)/fft.timeSize())

                    //drawer.circle(i*5.0, height/2.0 - bandDB, 5.0)
                    drawer.rectangle(i*5.0, height/2.0, 5.0, bandDB.map(0.0, -150.0, 0.0, -height/8.0))

                }
            }
        }
    }
}