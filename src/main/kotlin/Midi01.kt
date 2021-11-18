import event.SmoothedControl
import org.openrndr.application
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

            val radiusControl = SmoothedControl(this)

            midiController.controlChanged.listen {
                //println(it)
                if (it.control == 7) {
                    radiusControl.targetValue = it.value / 127.0
                }
            }

            extend {
                drawer.circle(width / 2.0, height / 2.0, radiusControl.value * width / 2.0)
            }

        }
    }
}