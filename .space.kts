job("compile") {
    container("openjdk:11") {
        mountDir = "/root"

        kotlinScript {
            it.gradlew("publishAllPublicationsToSpaceRepository")
        }

        val buildFiles = "{{ hashFiles('gradle/wrapper/gradle-wrapper.properties', 'build.gradle.kts', 'kotlin-cdk-lib/build.gradle.kts', 'buildSrc/build.gradle.kts', 'settings.gradle.kts') }}"

        cache {
            storeKey = "gradle-wrapper-{{ hashFiles('gradle/wrapper/gradle-wrapper.properties', 'build.gradle.kts', 'env/build.gradle.kts', 'settings.gradle.kts') }}"
            storeKey = "gradle-wrapper-${buildFiles}"
            localPath = "/root/.gradle/wrapper"
        }
        cache {
            storeKey = "gradle-daemon-{{ hashFiles('gradle/wrapper/gradle-wrapper.properties', 'build.gradle.kts', 'env/build.gradle.kts', 'settings.gradle.kts') }}"
            storeKey = "gradle-cache-${buildFiles}"
            localPath = "/root/.gradle/caches"
        }
        cache {
            storeKey = "gradle-caches-{{ hashFiles('gradle/wrapper/gradle-wrapper.properties', 'build.gradle.kts', 'env/build.gradle.kts', 'settings.gradle.kts') }}"
            storeKey = "gradle-daemons-${buildFiles}"
            localPath = "/root/.gradle/daemon"
        }
    }
}