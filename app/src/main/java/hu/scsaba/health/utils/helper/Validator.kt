package hu.scsaba.health.utils.helper

import android.util.Patterns

fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
fun CharSequence?.isValidPassword() = !isNullOrEmpty() && this.length >= 6
fun CharSequence?.isValidUsername() = !isNullOrEmpty() && this.length >= 3

fun CharSequence?.isValidWorkoutName() = !isNullOrEmpty() && this.length >= 3 && this.length <= 20
