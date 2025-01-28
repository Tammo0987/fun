namespace com/github/tammo

class Main():

    effect main(args: String[]): Unit = {
        println(add())
    }

    fun add(): Int = {
        20 + 20 + 20
    }