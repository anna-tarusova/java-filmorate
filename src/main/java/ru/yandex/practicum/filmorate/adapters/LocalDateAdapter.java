package ru.yandex.practicum.filmorate.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
        if (localDate == null) {
            jsonWriter.value((String) null);
            return;
        }
        jsonWriter.value(localDate.format(DateTimeFormatter.ISO_DATE));
    }

    @Override
    public LocalDate read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        return LocalDate.parse(jsonReader.nextString(), DateTimeFormatter.ISO_DATE);
    }
}
