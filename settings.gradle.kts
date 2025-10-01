rootProject.name = "CBTIS239"

include("backend", "front")

project(":backend").projectDir = file("backend")
project(":front").projectDir = file("front")