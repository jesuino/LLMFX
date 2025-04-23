package org.fxapps.llmfx.tools.time;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class DateTimeTool {

    @Tool("Returns the current date including the year, month and day. It is the equivalent of user asking for today's date or current year")
    public String currentDate() {
        return java.time.LocalDate.now().toString();
    }

    @Tool("Returns the current year. Use this if user asks for the current year or mention 'this year'")
    public int currentYear() {
        return java.time.LocalDate.now().getYear();
    }

    @Tool("Returns the current date and time")
    public String currentDateTime() {
        return java.time.LocalDateTime.now().toString();
    }

    @Tool("Returns the current time. Responds to question: what time is it?")
    public String currentTime() {
        return java.time.LocalTime.now().toString();
    }

    @Tool("Returns the current date and time with timezone")
    public String currentDateTimeZone() {
        return java.time.ZonedDateTime.now().toString();
    }
}
