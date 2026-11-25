plugins {
	application
}

group = "br.ufrn.imd"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("tools.jackson.core:jackson-databind:3.1.8")
}

application {
	mainClass = "br.ufrn.imd.loja.console.FrontendApplication"
}

tasks.named<JavaExec>("run") {
	standardInput = System.`in`
}
