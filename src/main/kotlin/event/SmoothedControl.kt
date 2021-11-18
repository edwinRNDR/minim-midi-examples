package event

import kotlinx.coroutines.yield
import org.openrndr.Program
import org.openrndr.launch
import org.openrndr.math.mix

class SmoothedControl(program: Program, val smoothFactor: Double = 0.5) {
    var targetValue = 0.0
    var value = 0.0

    init {
        program.launch {
            while(true) {
                update()
                yield()
            }
        }
    }

    fun update() {
        value = mix(value, targetValue, 1.0 - smoothFactor)
    }

}