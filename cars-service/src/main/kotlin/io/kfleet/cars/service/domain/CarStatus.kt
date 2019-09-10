package io.kfleet.cars.service.domain

enum class CarStatus {
    FREE, // is waiting for a traveler
    IN_USE, // is driving a traveler to its destination
    OUT_OF_POOL // the car is not in the pool (the owner did not offer it for travelers or it is damaged, or... )
}
