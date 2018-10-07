# Rock Paper Scissors using Axon and Elm

[![Build Status](https://travis-ci.org/johanhaleby/rps-axon-elm.svg)](https://travis-ci.org/johanhaleby/rps-axon-elm)

Just an example app using event sourcing with Axon and a frontend written in Elm.

To build and run the server:
	
	$ mvn package && target/rock-paper-scissors

This will start a webserver on port 8080.

To run tests do:

	$ mvn clean test
	
When developing the frontend application first start the server using:

	$ mvn exec:java -Dexec.args="-l external -d src/main/resources/compiled-static,src/main/resources/static"

Then start the Elm compiler:

	$ watch elm make src/main/elm/Frontend.elm --output src/main/resources/compiled-static/index.html
	
Now you can navigate to [http://localhost:8080](http://localhost:8080). Just reload to page to see changes in made to the Elm source code.
