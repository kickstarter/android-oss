package com.kickstarter.mock.factories

import com.kickstarter.models.Location
import com.kickstarter.models.Location.Companion.builder

object LocationFactory {
    @JvmStatic
    fun germany(): Location {
        return builder()
            .id(1L)
            .displayableName("Berlin, Germany")
            .name("Berlin")
            .state("Berlin")
            .country("DE")
            .expandedCountry("Germany")
            .build()
    }

    @JvmStatic
    fun mexico(): Location {
        return builder()
            .id(2L)
            .displayableName("Mexico City, Mexico")
            .name("Mexico City")
            .state("Mexico")
            .country("MX")
            .expandedCountry("Mexico")
            .build()
    }

    fun nigeria(): Location {
        return builder()
            .id(3L)
            .displayableName("Nigeria")
            .name("Nigeria")
            .state("Imo State")
            .country("NG")
            .expandedCountry("Nigeria")
            .build()
    }

    @JvmStatic
    fun sydney(): Location {
        return builder()
            .id(4L)
            .name("Sydney")
            .displayableName("Sydney, AU")
            .country("AU")
            .state("NSW")
            .projectsCount(33)
            .expandedCountry("Australia")
            .build()
    }

    @JvmStatic
    fun unitedStates(): Location {
        return builder()
            .id(5L)
            .displayableName("Brooklyn, NY")
            .name("Brooklyn")
            .state("NY")
            .country("US")
            .expandedCountry("United States")
            .build()
    }

    @JvmStatic
    fun canada(): Location {
        return builder()
            .id(6L)
            .displayableName("Canada")
            .name("Canada")
            .country("CA")
            .expandedCountry("Canada")
            .build()
    }

    fun empty(): Location {
        return builder()
            .id(-1L)
            .displayableName("")
            .name("")
            .country("")
            .expandedCountry("")
            .build()
    }
}
