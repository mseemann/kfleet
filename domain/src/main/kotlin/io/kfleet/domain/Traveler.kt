package io.kfleet.domain

import kotlin.random.Random


data class Traveler(val id: String, val name: String, val state: TravelerStatus) {

    companion object {
        fun create(id: Int) = Traveler(
                id = "$id",
                name = "${randomName()} ${randomName()}",
                state = TravelerStatus.IS_LIVING
        )

        private val Names = arrayOf(
                "Elli", "Miah", "Ralph", "Classen", "Solange", "Hoppe",
                "Yan", "Rozier", "Philomena", "Deforest", "Eldon", "Krol",
                "Constance", "Claybrook", "Georgetta", "Viola", "Ranae", "Wolfgram",
                "Arlean", "Reno", "Farrah", "Justiniano", "Genia", "Currence",
                "Edward", "Mcphee", "Retha", "Ressler", "Kanesha", "Arteaga",
                "Lesa", "Pantoja", "Imogene", "Kight", "Mellisa", "Pilla",
                "Gracia", "Caesar", "Malka", "Badgley"
        )

        private fun randomName() = Names[Random.nextInt(0, Names.size - 1)]
    }
}
