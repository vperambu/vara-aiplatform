package com.vara.org.vara_copilotai.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Topic {

    @Id
    private String id;
    private String content;
    private String type; // "static" or "generative"
}
