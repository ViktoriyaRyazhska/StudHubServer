package com.softserve.academy.studhub.service.impl;

import com.softserve.academy.studhub.constants.ErrorMessage;
import com.softserve.academy.studhub.constants.SuccessMessage;
import com.softserve.academy.studhub.entity.News;
import com.softserve.academy.studhub.exceptions.NotFoundException;
import com.softserve.academy.studhub.repository.NewsRepository;
import com.softserve.academy.studhub.service.NewsService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@Service
@Slf4j
public class NewsServiceImpl implements NewsService {

    private NewsRepository newsRepository;

    private ContentParser parser;

    @Override
    public News save(News news) {

        News resultNews = newsRepository.saveAndFlush(news);
        return resultNews;
    }

    @Override
    public News update(Integer newsId, News news) {
        News updatable = findById(newsId);
        updatable.setTitle(news.getTitle());
        updatable.setBody(news.getBody());
        updatable.setSourceUrl(news.getSourceUrl());

        return newsRepository.saveAndFlush(updatable);
    }

    @Override
    public News findById(Integer newsId) {
        return newsRepository.findById(newsId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.NEWS_NOTFOUND + newsId));
    }

    @Override
    public String deleteById(Integer newsId) {
        News newsToDelete = findById(newsId);
        newsRepository.deleteById(newsId);
        return SuccessMessage.NEWS_DELETED_SUCCESSFULLY;
    }

    @Override
    public Page<News> findAllSortedByAge(Pageable pageable) {

        return newsRepository.findAllByOrderByCreationDateDesc(pageable);
    }

    @Override
    public Boolean existByUrl(String url) {
        return newsRepository.existsBySourceUrl(url);
    }

    @Override
    @Scheduled(fixedDelay = 1000000)
    public void parseAndSave() {
        Set<String> linkSet = parser.parseLinks("https://ain.ua/en");
        for (String link : linkSet) {
            if (!existByUrl(link)) {
                String title = parser.parseTitle(link);
                String body = parser.parseBody(link);
                log.info("News body " + body);
                News news = new News();
                news.setTitle(title);
                news.setBody(body);
                news.setCreationDate(LocalDateTime.now());
                news.setSourceUrl(link);
                newsRepository.saveAndFlush(news);
            }
        }
    }
}
