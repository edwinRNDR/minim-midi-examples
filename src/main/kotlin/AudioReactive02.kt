import ddf.minim.Minim
import ddf.minim.analysis.FFT
import minim.MinimObject
import org.openrndr.application
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blur.GaussianBloom

fun main() {
    application {

        configure {
            width = 1280
            height = 720

        }

        program {
            val minim = Minim(MinimObject())
            minim.getLineIn()
            val lineIn = minim.lineIn
            lineIn.enableMonitoring()
            val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
            fft.logAverages( 22, 3 );

            program.ended.listen {
                minim.stop()

            }


            val c = compose {
                layer {
                    draw {
                        for (i in 0 until fft.specSize()) {
                            val y = fft.getBand(i)
                            drawer.circle(i * 2.0, height / 2.0 - y * 10.0, 5.0)

                        }
                    }

                }

                layer {
                    draw {
                        drawer.circle(100.0, 100.0, 40.0)

                    }
                    post(GaussianBloom()) {
                        sigma = fft.getAvg(0).toDouble()*10.0
                        width = fft.getAvg(1).toInt()
                    }

                }

            }
            extend {
                fft.forward(lineIn.mix)
                c.draw(drawer)
            }
        }
    }
}