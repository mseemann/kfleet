package io.kfleet.domain

enum class CarStatus {
    FREE, // is waiting for a traveler
    IN_USE // is driving a traveler to its destination
}
