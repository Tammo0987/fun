namespace com/github/tammo

class Main():

    effect main(args: String[]): Unit = {
        println(test())
    }

    fun add(): Int = {
        20 + 20 + 20
    }

    fun test(): Int = {
        add()
    }