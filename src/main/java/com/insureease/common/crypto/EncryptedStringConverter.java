package com.insureease.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return PiiCryptoService.encryptStatic(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return PiiCryptoService.decryptStatic(dbData);
    }
}
