package org.example;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EntityModel {
    private String language;
    private String question;
    private String answer;
    private List<byte[]> photos;

    public EntityModel(String questionAnswer, String language) {
        this.language = language;
        this.question = questionAnswer.split("\n")[0];
        this.answer = questionAnswer;
    }
}
