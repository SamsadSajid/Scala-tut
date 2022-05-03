/**
  * unapply method in a singleton object for pattern matching
  * */
object PatternMatching extends App {

  /**
    * If we use a singleton object with `unApply` function,
    * we should use lowercase to name the object
    * */
  object even {
    def unapply(number: Int): Boolean = number % 2 == 0
  }

  object singleDigit {
    def unapply(arg: Int): Boolean = arg > -10 && arg < 10
  }

  def matchPattern(n: Int): Unit = {
    val mathProperty = n match {
      case singleDigit() => "single digit"
      case even()        => "even number"
      case _             => "no match found, stayin' alive"
    }

    println(mathProperty)
  }

  List(69, 9, -11, 20).foreach(x => matchPattern(x))
}
