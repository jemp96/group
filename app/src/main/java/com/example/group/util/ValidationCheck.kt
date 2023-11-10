package com.example.group.util

import android.util.Patterns

fun ValidateEmail(email: String): RegisterValidation{
    if (email.isEmpty())
        return RegisterValidation.Failed("El Email no puede estar vacio")
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Formato de email incorrecto")

    return RegisterValidation.Success
}

fun ValidatePassword(password: String): RegisterValidation{
    if (password.isEmpty())
        return RegisterValidation.Failed("La contraseña no puede estar vacia")

    if(password.length <6)
        return RegisterValidation.Failed("La contraseña solo puede tener 6 caracteres ")

    return RegisterValidation.Success
}