import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import ddf.minim.analysis.FFT
import event.ChargedEvent
import minim.MinimObject
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blur.FrameBlur
import org.openrndr.extra.fx.distort.Perturb
import kotlin.math.sqrt

fun main() {
    application {

        configure {
            width = 1280
            height = 720
        }

        program {
            var showSpectrum = false
            var showTriggers = true
            keyboard.character.listen {
                if (it.character == 's') {
                    showSpectrum = !showSpectrum
                }
                if (it.character == 't') {
                    showTriggers = !showTriggers
                }
            }
            // setup sound (through minim), fft (spectral analysis) and beat detection
            val minim = Minim(MinimObject())

            val lineIn = minim.loadFile("data/paddock-011.mp3").apply {
                this.play()
                this.loop()
            }
            val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
            fft.logAverages( 22, 3 );

            val beatDetect = BeatDetect()
            beatDetect.detectMode(BeatDetect.FREQ_ENERGY)

            // create 'charged' events for kick, hihat and snare
            val kickEvent = ChargedEvent(this, 0.9)
            val hihatEvent = ChargedEvent(this, 0.5)
            val snareEvent = ChargedEvent(this, 0.95)

            // make sure minim is stopped when the program exits
            program.ended.listen {
                minim.stop()
            }

            // use the composition framework to draw some simple post-processed visuals
            val c = compose {
                layer {
                    draw {
                        // draw a circle using the kick event's charge
                        drawer.circle(width / 2.0, height / 2.0, 20.0 + kickEvent.charge *100.0)

                    }
                    post(Perturb()) {
                        gain = snareEvent.charge * 0.2
                        phase += hihatEvent.smoothCharge
                    }
                    post(FrameBlur()) {
                        this.blend = 0.1
                    }
                }
            }

            extend {
                fft.forward(lineIn.mix)
                beatDetect.detect(lineIn.mix)

                // minim beatDetect.isKick doesn't work well with the botched mastering on my track
                if (beatDetect.isRange(1, 2, 2)) {
                    kickEvent.trigger()
                }
                if (beatDetect.isSnare) {
                    snareEvent.trigger()
                }
                if (beatDetect.isHat) {
                    hihatEvent.trigger()
                }
                c.draw(drawer)

                // draw the spectrum in case it is enabled by pressing 's'
                if (showSpectrum) {
                    for (i in 0 until fft.specSize()) {
                        val y = fft.getBand(i)
                        drawer.circle(i * 2.0, height / 2.0 - y * 10.0, 5.0)

                    }
                }

                if (showTriggers) {
                    drawer.fill = ColorRGBa.PINK.shade(0.5)
                    drawer.circle(30.0, 30.0, 15.0*sqrt(kickEvent.smoothCharge))
                    drawer.fill = ColorRGBa.PINK
                    drawer.circle(30.0, 30.0, 15.0* sqrt(kickEvent.charge))
                    drawer.text("kick", 15.0,  60.0)

                    drawer.fill = ColorRGBa.YELLOW.shade(0.5)
                    drawer.circle(80.0, 30.0, 15.0*sqrt(snareEvent.smoothCharge))
                    drawer.fill = ColorRGBa.YELLOW
                    drawer.circle(80.0, 30.0, 15.0* sqrt(snareEvent.charge))
                    drawer.text("snare", 65.0,  60.0)


                    drawer.fill = ColorRGBa.RED.shade(0.5)
                    drawer.circle(140.0, 30.0, 15.0*sqrt(hihatEvent.smoothCharge))
                    drawer.fill = ColorRGBa.RED
                    drawer.circle(140.0, 30.0, 15.0* sqrt(hihatEvent.charge))
                    drawer.text("hihat", 125.0,  60.0)
                }
            }
        }
    }
}