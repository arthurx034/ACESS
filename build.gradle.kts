// build.gradle (Project)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.2") // AGP compatível
        classpath("com.google.gms:google-services:4.4.2") // Firebase
    }
}

plugins {
    // Nenhum plugin Android aqui, apenas no módulo :app
}
