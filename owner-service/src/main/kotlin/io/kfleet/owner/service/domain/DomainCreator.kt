package io.kfleet.owner.service.domain


import kotlin.random.Random

fun car(buildCar: Car.Builder.() -> Unit): Car = Car.newBuilder().apply { buildCar() }.build()

object CarFactory {

    fun createRandom(id: Int) = Car(
            "$id",
            CarModel.values()[Random.nextInt(CarModel.values().size)]
    )
}
