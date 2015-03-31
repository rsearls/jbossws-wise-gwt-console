
Project jbossws-wise-gwt-console is a temporary holding area for proof-of-concept code for
a GWT based console for project http://wise.jboss.org/.  Module wise-gwt is new
code.  Module wise-gui contains changes to this existing module.

The console was implemented using MVP design pattern.

The console UI is only minimally implemented currently.

    Implementation status
        - Input screen to accept a wsdl URL
        - Input screen handle user and password fields.
        - Endpoints screen displays a tree of endpoints. Clicking an endpoint send user
            to the endpoint's input arguments screen.  A small set of Java primitive
            types are supported and corresponding List (parameter) type.
        - TODO: add support for existing datatypes in wise-gui
        - Change cancel button to back.
        - implement Invoke button
        - implement Preview Message button
        - implement override fields to args input screen
        - implement view message button
        - TODO: add error handling
        - TODO: add styling


A couple of demo app are provided in wise-gwt/test-archives for convenience.
Deploy them to your JBOSS.

Building project

    From the project root directory execute
    mvn clean install

    Deploy  wise-gwt/target/wise-gwt-1.0-SNAPSHOT.war to your JBOSS.

    The URL is http://localhost:8080/wise-gwt-1.0-SNAPSHOT
        Provide the URL a deployed WS app. The URLS for the wise-gwt/test-archives apps
            http://localhost:8080/jaxws-jbws1798/Service?wsdl
            http://localhost:8080/jaxws-jbws2259?wsdl
            http://localhost:8080/schemasInWeirdPlaceFromSrc/SayHiImpl?wsdl
