package com.e_commerce.e_commerce.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class ImageListConverter implements AttributeConverter<List<String>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> imageList) {
        return imageList != null && !imageList.isEmpty() ? String.join(DELIMITER, imageList) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String imageString) {
        return imageString != null && !imageString.isEmpty()
                ? Arrays.asList(imageString.split(DELIMITER))
                : new ArrayList<>();
    }
}
