package io.robe.convert.xml.parsers;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.lang.reflect.Field;

import static java.lang.Enum.valueOf;

public class ParseEnum implements IsParser {
    @Override
    public Object parse(JsonParser parser, Field field) throws IOException {
        Class<? extends Enum> enumClass = (Class<? extends Enum>) field.getType();
        return valueOf(enumClass, parser.getValueAsString());
    }
}
