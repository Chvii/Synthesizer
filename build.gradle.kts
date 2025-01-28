plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

javafx {
    version = "21" // Match your JDK version
    modules = listOf(
            "javafx.controls",
            "javafx.fxml",
            "javafx.graphics"
    )
}

dependencies {
    // Add other dependencies here if needed
}

application {
    mainClass.set("com.core.Visuals.GUIFrontendStuff")
}