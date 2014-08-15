go-rapiddeploy
==============

Plugins for Thoughtworks Go - MidVision RapidDeploy integration

Note: you will need to add the go-plugin-api.jar dependency from the lib directory into your local maven repository in order to build the sources.
Place the jar file into your maven repository on your filesystem to the location below:
[user_home]/.m2/repository/com/thoughtworks/go/go-plugin-api/14.1.0/go-plugin-api-14.1.0.jar

This maven dependency will resolve then:

<dependency>
            <groupId>com.thoughtworks.go</groupId>
            <artifactId>go-plugin-api</artifactId>
            <version>14.1.0</version>
            <scope>provided</scope>
</dependency>

