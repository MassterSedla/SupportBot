package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityModel {
    private String language;
    private String question;
    private String answer;
    private List<byte[]> photos;

    public EntityModel(String answer, String question, String language) {
        this.language = language;
        this.question = question;
        this.answer = answer;
    }
}
