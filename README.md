# T5 Commons
## Introduction
This is a library containing code that is shared between different projects within the Medical Device Integration Lab. It includes classes for database communication, format conversion and general utilities.

## Build
The library is written in Java version 8 and uses Gradle for automatic building and dependency management.
Assuming that Java EE 8 development kit is installed and exist on the PATH environment variable, the project can be built with the following command from the project root folder:
  
    ./gradlew build uploadArchives
    
This outputs a .jar file to the folder `t5-repo` located in the same parent folder as the project root. Other projects that use this library will by default look for this library there, assuming that their respective project root folders are placed in the same parent folder.
