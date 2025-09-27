rootProject.name = "CBTIS239"

include("backend", "frontend")

project(":backend").projectDir = file("backend")
project(":frontend").projectDir = file("frontend")