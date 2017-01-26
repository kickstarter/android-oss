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

    throw Exception("Exception: neither left nor right values found")
  }

  fun isLeft(): Boolean {
    return this.left != null
  }

  fun isRight(): Boolean {
    return this.right != null
  }
}
