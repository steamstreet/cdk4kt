job("compile") {
    container("amazoncorretto:17-alpine") {
        kotlinScript {
            it.gradlew("publish")
        }
    }
}