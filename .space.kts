job("compile") {
    container("amazoncorretto:17-alpine") {
        resources {
            cpu = 500.mcpu
            memory = 2000.mb
        }

        kotlinScript {
            it.gradlew("publish", "-PBUILD_NUMBER=${it.executionNumber()}")
        }
    }
}