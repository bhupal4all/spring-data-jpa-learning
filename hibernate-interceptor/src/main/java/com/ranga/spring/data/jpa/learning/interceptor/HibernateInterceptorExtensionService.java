package com.ranga.spring.data.jpa.learning.interceptor;

import com.ranga.spring.data.jpa.learning.entity.Books;
import com.ranga.spring.data.jpa.learning.service.BooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HibernateInterceptorExtensionService {

    @Autowired
    BooksService booksService;

    public void writeHistoryExtension(final Object entity,
                                      final Object[] currentState,
                                      final Object[] previousState,
                                      final String[] propertyNames,
                                      final List<String> historyList) {

        // TODO: Modify as per the requirement
        if (entity instanceof Books) {
            booksService.writeHistory(entity, historyList);
        }

    }
}
