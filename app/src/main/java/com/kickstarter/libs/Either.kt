package com.kickstarter.libs

class Either<out A, out B> private constructor (val left: A?, val right: B?) {
  companion object {
    fun <A, B> left(left: A): Either<A, B> {
      return Either(left, null)
    }

    fun <A, B> right(right: B): Either<A, B> {
      return Either(null, right)
    }
  }

  fun <C> either(ifLeft: (A) -> C, ifRight: (B) -> C): C {
    if (this.left != null) {
      return ifLeft(this.left)
    }
    if (this.right != null) {
      return ifRight(this.right)
    }
    throw Exception("Exception: Invalid left or right values.")
  }

  fun isLeft(): Boolean {
    return this.left != null
  }

  fun isRight(): Boolean {
    return this.right != null
  }

  /**
   * Maps the right side of an `Either` value.
   *
   * @param transform    A transformation
   * @return             A new `Either` value.
   */
  fun <C> map(transform: (B) -> C): Either<A, C> {
    if (this.left != null) {
      return Companion.left(this.left)
    }
    if (this.right != null) {
      return Companion.right(transform(this.right))
    }
    throw Exception("Exception: Invalid left or right values.")
  }

  /**
   * Maps the left side of an `Either` value.
   *
   * @param transform    A transformation
   * @return             A new `Either` value.
   */
  fun <C> mapLeft(transform: (A) -> C): Either<C, B> {
    if (this.left != null) {
      return Companion.left(transform(this.left))
    }
    if (this.right != null) {
      return Companion.right(this.right)
    }
    throw Exception("Exception: Invalid left or right values.")
  }
}
