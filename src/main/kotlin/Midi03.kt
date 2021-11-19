import event.SmoothedControl
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.color.ChromaticAberration
import org.openrndr.extra.fx.distort.HorizontalWave
import org.openrndr.extra.midi.MidiDeviceDescription

fun main() {
    application {
        program {

            val midiDevices = MidiDeviceDescription.list().filter { it.receive && it.transmit }

            midiDevices.forEachIndexed { index, it ->
                println("$index > ${it.name}, ${it.vendor} r:${it.receive} t:${it.transmit}")
            }
            // check console output for the right index
            val midiController = midiDevices[0].open()

            // make sure we close everything properly when the program is stopped
            ended.listen {
                midiController.destroy()
            }

            // create the controls we need
            val radiusControl = SmoothedControl(this)
            val redControl = SmoothedControl(this, 0.9)
            val blueControl = SmoothedControl(this, 0.9)
            val greenControl = SmoothedControl(this, 0.9)


            val verticalControl = SmoothedControl(this, 0.2)
            val amplitudeControl = SmoothedControl(this, 0.9)
            val phaseControl = SmoothedControl(this, 0.9)
            val segmentControl = SmoothedControl(this, 0.9)


            // link up controls
            midiController.controlChanged.listen {

//                println(it)
                if (it.control == 7) {
                    radiusControl.targetValue = it.value / 127.0
                }
            }

            // link up notes
            midiController.noteOn.listen {

                println(it)

                val v = it.velocity / 127.0


                if (it.note == 60) {
                    redControl.targetValue = v
                }
                if (it.note == 61) {
                    blueControl.targetValue = v
                }
                if (it.note == 62) {
                    greenControl.targetValue = v
                }

                if (it.note == 48) {
                    amplitudeControl.targetValue = v
                }
                if (it.note == 49) {
                    phaseControl.targetValue = v
                }
                if (it.note == 50) {
                    segmentControl.targetValue = v
                }

                if (it.note == 51) {
                    verticalControl.targetValue = v
                }
            }

            val composition = compose {
                layer {
                    draw {
                        drawer.fill = ColorRGBa.WHITE
                            .mix(ColorRGBa.RED, redControl.value)
                            .mix(ColorRGBa.BLUE, blueControl.value)
                            .mix(ColorRGBa.GREEN, greenControl.value)

                        drawer.circle(width / 2.0, height / 2.0, radiusControl.value * 400.0)
                    }
                    post(ChromaticAberration()) {
                        this.aberrationFactor = amplitudeControl.value * 40.0
                    }

                    post(HorizontalWave()) {
                        this.amplitude = amplitudeControl.value
                        this.segments = (segmentControl.value * 16.0).toInt()
                        this.phase += phaseControl.value
                    }
                }

                layer {
                    draw {
                        drawer.rectangle(300.0, 300.0 - verticalControl.value * 300.0, 100.0, 100.0)

                    }

                }

            }

            extend {
                composition.draw(drawer)
            }

        }
    }
}