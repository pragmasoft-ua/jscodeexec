package api.demo.graalvmdemo.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeParser {

    public static LocalDateTime parseString(String scheduledTime) {
        LocalDateTime dateTime = null;
        if (scheduledTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            dateTime = LocalDateTime.parse(scheduledTime, formatter);
        }

        return dateTime;
    }

}
