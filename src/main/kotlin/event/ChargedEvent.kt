package event

import kotlinx.coroutines.yield
import org.openrndr.Program
import org.openrndr.launch
import org.openrndr.math.mix

class ChargedEvent(program: Program, val dischargeRate: Double) {
    var charge = 0.0
    var smoothCharge = 0.0

    init {
        program.launch {
            while (true) {
                update()
                yield()
            }
        }
    }

    fun update() {
        charge *= dischargeRate
        smoothCharge = mix(smoothCharge, charge, 0.5)
    }

    fun trigger() {
        charge = 1.0
    }

}