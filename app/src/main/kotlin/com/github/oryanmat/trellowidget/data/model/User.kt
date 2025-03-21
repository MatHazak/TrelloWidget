package com.github.oryanmat.trellowidget.data.model

data class User(
        val fullName: String = "",
        val username: String = "") {
    override fun toString() = "$fullName (@$username)"
}