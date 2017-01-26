package com.kickstarter.libs

sealed class Either<out A, out B> {
  class Left<out L, out R>(val left: L) : Either<L, R>()
  class Right<out L, out R>(val right: R) : Either<L, R>()

  fun <C> either(ifLeft: (A) -> C, ifRight: (B) -> C): C = when(this) {
    is Left -> ifLeft(this.left)
    is Right -> ifRight(this.right)
  }

  fun isLeft(): Boolean {
    return this is Left
  }

  fun isRight(): Boolean {
    return this is Right
  }

  /**
   * Extracts the `left` value from an either.
   *
   * @return    A value of type `A` if this is a left either, `null` otherwise.
   */
  fun left(): A? = when(this) {
    is Left -> this.left
    is Right -> null
  }

  /**
   * Extracts the `right` value from an either.
   *
   * @return    A value of type `B` if this is a left either, `null` otherwise.
   */
  fun right(): B? = when(this) {
    is Left -> null
    is Right -> this.right
  }

  /**
   * Maps the right side of an `Either` value.
   *
   * @param transform    A transformation
   * @return             A new `Either` value.
   */
  fun <C> map(transform: (B) -> C): Either<A, C> = when(this) {
    is Left -> Left(this.left)
    is Right -> Right(transform(this.right))
  }

  /**
   * Maps the left side of an `Either` value.
   *
   * @param transform    A transformation
   * @return             A new `Either` value.
   */
  fun <C> mapLeft(transform: (A) -> C): Either<C, B> = when(this) {
    is Left -> Left(transform(this.left))
    is Right -> Right(this.right)
  }
}
