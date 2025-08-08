# template-library
This DAPM organization contains processing element templates available to everyone.

Note that for testing purposes, the compose.yaml is currently set to `` bridge`` (i.e. for local use). To use this library in a distributed setting, a docker swarm would need to be used with ``overlay`` instead. Additionally, ``src/main/java/librarytemplate/Application.java`` needs to be running when using miners from this library.

The compose.yaml must be run with docker to be able to run the application.