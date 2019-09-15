package io.kfleet.cars.service.commands

interface OwnerCommand {
    val id: String
    val type: String
}


data class CreateOwnerCommand(
        override val id: String,
        val name: String) : OwnerCommand {
    override val type: String = this::class.java.name
}
