# SpaceInvaders

A space invaders game made for MHP.

## Controls
* Move left: <- (left arrow)
* Move right: -> (right arrow)
* Shoot: OK (ok or enter)

## Prerequisites
* JDK 6
* JAVA_HOME setup

## Clone
The following steps will ensure your project is cloned properly.

1. `git clone https://github.com/bananenpuree1997/SpaceInvaders.git`
2. `cd SpaceInvaders`

## Building
__Note:__ If you do not have [Gradle] installed then use `./gradlew` for Git Bash and `gradlew.bat` for Windows systems in place of any 'gradle' command.

In order to build SpaceInvaders you simply need to run the `gradle build` command. You can find the compiled JAR file in `./build/libs` labeled 
similarly to `spaceinvaders-x.x.x-SNAPSHOT.jar`.

## Running
__Note:__ If you do not have [Gradle] installed then use `./gradlew` for Git Bash and `gradlew.bat` for Windows systems in place of any 'gradle' command.

Running the following command will open XleTView and the SpaceInvaders game will start:
<br>`gradle runXlet`
<br>or
<br>`gradlew.bat runXlet` (for Windows)
<br>or
execute `runXlet.bat` (for Windows)
<br>or
<br>`./gradlew runXlet` (for Git Bash)

[Gradle]: https://gradle.org/