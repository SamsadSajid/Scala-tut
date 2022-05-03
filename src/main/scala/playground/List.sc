val aList = List(1,2,3)

val anotherList = aList :: List(4) // List[List[Int], Int]

anotherList.head // List[Int]

val oneDList = aList ::: List(4) // List[Int]

// to concatenate two list, use :::
oneDList.head // 1
oneDList.tail // [2, 3, 4], 0 cost operation
oneDList.tails // iterator
oneDList.tails.foreach(x => println(x))

// prepend an element in a list, O(1)
val prependedList = 0 :: aList

// another way to concatenate list
val flatList = 1 :: 2 :: List()

// another way to concatenate list
// ++, returns a new collection containing the elements
// from the left operand followed by the elements from
// the right operand.
val joinedList = List(1,2,3) ++ List(4,5,6)

// the following will not work because ++ works for same type
//val newJoinedList = 0 ++ joinedList

val newJoinedList = List(0) ++ joinedList

// :: or ::: works only for prepending element in a list
// the following will not work
// val appendedList = aList ::: 4

// to append an element in the list, need to use ++
// O(n)
val appendedList = aList ++ List(4)


// convert all elements of a list by joining with a comma,
// kinda like `" ".join(list)` in python
val intElemList = List("I", "am", "a", "human")
val stringBuilder = new StringBuilder()
intElemList.addString(stringBuilder, " ")

val myArray = Array[String]("", "", "")
intElemList.copyToArray(myArray)
myArray