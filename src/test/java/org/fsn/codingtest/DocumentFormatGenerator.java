package org.fsn.codingtest;


import org.springframework.restdocs.snippet.Attributes;

import static org.springframework.restdocs.snippet.Attributes.key;

public interface DocumentFormatGenerator {

    static Attributes.Attribute getDateFormat() {
        return key("format").value("yyyy-MM-dd");
    }
    static Attributes.Attribute getTimeFormat() {
        return key("format").value("HH");
    }
}
