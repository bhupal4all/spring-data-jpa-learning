package com.ranga.spring.data.jpa.learning;

import com.ranga.spring.data.jpa.learning.entity.Books;
import com.ranga.spring.data.jpa.learning.entity.BooksUpdateHistory;
import com.ranga.spring.data.jpa.learning.service.BooksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@Slf4j
@EnableJpaAuditing
@EnableJpaRepositories
@EnableConfigurationProperties
public class LearningApplication implements CommandLineRunner {

    @Autowired
    BooksService booksService;

    public static void main(String[] args) {
        SpringApplication.run(LearningApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("**************************************************");

        booksService.save(Books.builder().name("Spring MVC").author("Josh Long").year(2010).customField1("Nothing").build());
        booksService.save(Books.builder().name("Spring MVC").author("Ranga").year(2012).build());
        booksService.save(Books.builder().name("Spring Boot").author("Josh Long").year(2015).build());

        Optional<Books> firstBook = booksService.getById(1);
        if (firstBook.isPresent()){
            firstBook.get().setYear(2020);
            firstBook.get().setCustomField1("Published");
            booksService.save(firstBook.get());
        }

        booksService.deleteById(1);

        List<Books> books = booksService.getAll();
        List<BooksUpdateHistory> booksHistory = booksService.getBooksHistory();

//        books.forEach(book -> log.info(book.toString()));
        booksHistory.forEach(history -> log.info("Book ID: {}, History: {}",history.getBookId(),history.getHistory()));

        System.out.println("**************************************************");
    }
}
