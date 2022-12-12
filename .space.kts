job("compile") {
    container("amazoncorretto:17-alpine") {
        kotlinScript {
            it.gradlew("publish", "-PBUILD_NUMBER=${it.executionNumber()}")
        }
    }
}