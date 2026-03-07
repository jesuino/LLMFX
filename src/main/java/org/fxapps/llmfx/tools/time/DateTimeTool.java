package org.fxapps.llmfx.tools.time;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class DateTimeTool {

    @Tool("Returns the current date including the year, month and day. It is the equivalent of user asking for today's date or current year")
    public String currentDate() {
        return java.time.LocalDate.now().toString();
    }


    @Tool("Returns the current time. Use it to know the current time")
    public String currentTime() {
        return java.time.LocalTime.now().toString();
    }
 
}
