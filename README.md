LLM FX
--
A LLM Desktop Client free for everyone! 


## About

![image](https://github.com/user-attachments/assets/269b827d-67ae-424a-9e37-023825000d8e)


LLM FX is a very simple desktop application to interact with LLM Servers. It is desktop only, no web or mobile.

I wanted a local graphical LLM client which had support to MCP and a few tools ready for use, but I wanted to know what was used. I had to create an application for it and LLM FX was born.


All the features of LLM FX were conceived by basically me trying to make it usable for daily tasks: assistance during work with local LLMs (respect your company policies regarding IA use) and also have fun on my spare time by talking to LLM and asking it to do anything I have in mind.


## Requirements

LLM FX is a single JAR file which requires the following:

* Java 23
* Any LLM Server. Can be local or a remote server, you just need to configure the Base URL and the API token for remote servers

## Installation

There's no installation, no docker, no shell script, just download the JAR and run `java -jar llmfx-{version}.jar`. I think double clicking on it should work as well, let know if it works!

## LLM FX Features

* Any OpenAI compatible LLM server: use it locally with ollama, ramalama or remotely with any OpenAI compatible server;
* Chat: Chat in streaming mode and export the chat anytime
* History: Keep a history of your chat for future references;
* Tools: Local tools to do anything you want! YOu have the freedom to select which tool you want the LLM to have access or let the LLM select the tools based on your prompt (TBD)
* MCP: Add MCP to your local LLM with a simple configuration;
* Graphic Tools: Let the LLM generate dashoards, create web pages and drawings for you and quickly visualize on a side pane


## Roadmap

* Plug custom Tools
* Add an option to allow the LLM to automatically select tools.
* MCP Store
* 3d render
* Image generation
* Image input

## Contributing

Yes, please contribute to this project. You can write issues or make minor fixes, or even help me to organize this horrible code.

LLM FX is Java, JavaFX, Quarkus and Langchain4j. To contribute you will need maven and Java JDK 23, then clone this repo and import it into VSCode. 

Then you can run the code simply using `mvn clean compile quarkus:dev`. YOu will find the built JAR in `target/llm-fx-{version}-runner.jar`

I recommend using the Red Hat Java Language Pack and Quarkus extension. If you want to change the application layout you can use SceneBuilder and open the file Chat.fxml, which is the only screen at the moment.

## Acknowledgments

LLM FX was only possible because Quarkus has a great JavaFX extension. Also, thank you LangChaing4j for the great Java API for talking to OpenAPI servers.




