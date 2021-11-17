package minim

import java.io.File
import java.io.InputStream

class MinimObject {
    fun sketchPath(fileName:String) :String {
        return "./"
    }

    fun createInput(fileName: String): InputStream {
        return File(fileName).inputStream()
    }

}
