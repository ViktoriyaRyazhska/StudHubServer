package com.softserve.academy.studhub.service;

import com.softserve.academy.studhub.entity.Question;
import com.softserve.academy.studhub.entity.Tag;

import java.security.Principal;
import java.util.List;

public interface IQuestionService {

    Question save(Question question, Principal principal);

    Question saveNoUser(Question question);

    Question update(Integer questionId, Question question);

    List<Question> findAll();

    Question findById(Integer questionId);

    String deleteById(Integer questionId);

    List<Question> sortByAge();

    List<Question> sortByTag(List<Tag> tags);

}
