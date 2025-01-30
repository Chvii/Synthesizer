plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0" // Use the latest version if available
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

application {
    mainClass.set("com.core.Visuals.SynthApplication")
    applicationDefaultJvmArgs = listOf(
            "--module-path", "/Users/christoffer/javafx-sdk-17/lib",
            "--add-modules", "javafx.controls,javafx.fxml"
    )
}