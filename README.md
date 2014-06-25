JSON Schema UI
==============

Define data content in JSON schema with validations/rules/dependencies and generate UI
--------------------------------------------------------------------------------------

Application built with the following technologies:

- Scala 2.11.1

- SBT 0.13.5

- Akka 2.3.0

- Play 2.3.0

- Reactive Mongo 0.11.0

- Specs2 2.3.8

- AngularJS 1.3.0-beta.13

and including

- HTML, CSS, CoffeeScript, Twitter Bootstrap

Introduction
------------

TODO

Setup
-----

To get you going with this application, just follow along

Scala, SBT and Play must be installed - the following assumes you are using "Homebrew", if not, tough luck:
> brew install scala

> brew install sbt

> brew install typesafe-activator

That last "brew" command in the past would have been "brew install play" but as of Play 2.3.0, the typesafe activator is utilised.
(In the following commands "activator" is again used instead of the usual "play").

In a directory where you wish to clone this project from git:
> $ git clone https://github.com/davidainslie/json-schema-ui.git

Go into the application's new project directory and complete the following:
> $ cd json-schema-ui

> $ activator

> [json-schema-ui] $ update-classifiers

> [json-schema-ui] $ idea sbt-classifiers with-sources=yes

> [json-schema-ui] $ test

Hopefully all "tests" will pass and you can now open up IntelliJ and start doing some damage.
(Sorry for not including Eclipse, but just replace the above "idea" entry appropriately).

Specs
-----
First take a look at the specs.
They can be run individually in your IDE or from SBT command line console using the "test" command.
There are both unit and integration specs, where the integration specs often use an embedded Mongodb.

To run the "integration specs" you will need PhantomJS:

> $ brew install phantomjs

Note that Ghost driver is being used where the dependency can be found in build.sbt

Application
-----------

TODO