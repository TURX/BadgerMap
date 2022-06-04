JAVA ?= /usr/bin
JFX = --module-path javafx-sdk-11.0.2/lib --add-modules javafx.controls,javafx.fxml
# for example, execute: make JAVA="~/Library/Java/JavaVirtualMachines/temurin-11.0.14.1/Contents/Home/bin" target
run: App.class MainWindow.fxml javafx-sdk-11.0.2
	$(JAVA)/java $(JFX) -cp .:gson-2.9.0.jar App
runTests: noteForTA runDataWranglerTests runFrontendDeveloperTests
runFrontendDeveloperTests: FrontendDeveloperTests.class MainWindow.fxml javafx-sdk-11.0.2 gson-2.9.0.jar junit5.jar JavaFXTester.jar
	$(JAVA)/java $(JFX) --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED -jar junit5.jar -cp .:gson-2.9.0.jar:JavaFXTester.jar --scan-classpath --disable-banner -n FrontendDeveloperTests
clean:
	rm -rf *.class BadgerMap.jar test2E.json test2V.json edgesCopy.json verticesCopy.json vertices.json edges.json TestGraph/
runSampleTests: SampleTests.class javafx-sdk-11.0.2 junit5.jar JavaFXTester.jar
	$(JAVA)/java $(JFX) --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED -jar junit5.jar -cp .:JavaFXTester.jar --scan-classpath --disable-banner -n SampleTests
runJar: BadgerMap.jar javafx-sdk-11.0.2 gson-2.9.0.jar
	$(JAVA)/java $(JFX) -cp BadgerMap.jar:gson-2.9.0.jar App
App.class: App.java javafx-sdk-11.0.2 gson-2.9.0.jar
	$(JAVA)/javac $(JFX) -cp .:gson-2.9.0.jar App.java
FrontendDeveloperTests.class: App.class FrontendDeveloperTests.java javafx-sdk-11.0.2 gson-2.9.0.jar junit5.jar JavaFXTester.jar
	$(JAVA)/javac $(JFX) -cp .:gson-2.9.0.jar:junit5.jar:JavaFXTester.jar FrontendDeveloperTests.java
BadgerMap.jar: App.class MainWindow.fxml
	$(JAVA)/jar cf BadgerMap.jar *.class MainWindow.fxml
SampleTests.class: SampleMain.class SampleTests.java javafx-sdk-11.0.2 junit5.jar JavaFXTester.jar
	$(JAVA)/javac $(JFX) -cp .:junit5.jar:JavaFXTester.jar SampleTests.java
SampleMain.class: SampleMain.java javafx-sdk-11.0.2
	$(JAVA)/javac $(JFX) -cp . SampleMain.java
