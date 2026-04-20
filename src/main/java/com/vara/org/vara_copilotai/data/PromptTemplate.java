package com.vara.org.vara_copilotai.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class PromptTemplate {

    @Id
    private String id;
    private String template;
    private String type;
}
