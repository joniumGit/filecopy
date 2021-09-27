# Filecopy

Simple Maven multimodule project for copying textfiles.

The project is split into three modules:

- api
- text
- ui

The API interfaces are placed in the __api__ module.

The service implementation resides in the __text__ module. This module also has all the tests for the implementation.

The UI is implemented in the __ui__ module with JavaFX. There is no tests for the UI at the moment.

#### Usage

Build and package:
> maven package -T8

Run:
> java -jar ui/target/filecopy-ui-0.1.0-jar-with-dependencies.jar

(or with any IDE)

The program has a single argument:

- `--buffer-size=2048` controls the memory buffer for copying.
    - default: 2048 characters