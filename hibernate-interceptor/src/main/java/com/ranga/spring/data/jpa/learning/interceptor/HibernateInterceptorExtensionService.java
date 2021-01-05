package com.ranga.spring.data.jpa.learning.interceptor;

import com.ranga.spring.data.jpa.learning.entity.Books;
import com.ranga.spring.data.jpa.learning.service.BooksService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HibernateInterceptorExtensionService {

    @Autowired
    BooksService booksService;

    public Map<Object, Object> fieldExtension(final Object entity,
                                              final String propertyName,
                                              final Object previousValue,
                                              final Object currentValue) {
        System.out.println(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Field Extn: Start");
        Map<Object, Object> updatedMap = new HashMap<>();

        // TODO: Modify as per the requirement for given Property
        System.out.println(
                String.format(">>>> [%s.%s] - %s to %s",
                        entity.getClass().getSimpleName(), propertyName, previousValue, currentValue)
        );

        if (entity instanceof Books) {
            String updatedPropertyName = booksService.updateNameFor(propertyName);

            if (Strings.isNotEmpty(updatedPropertyName)) {
                updatedMap.put(propertyName, updatedPropertyName);
            }
        }

        System.out.println(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Field Extn: End");
        return updatedMap;
    }

    public void historyExtension(final Object entity,
                                 final Object[] currentState,
                                 final Object[] previousState,
                                 final String[] propertyNames,
                                 final List<String> historyList) {
        System.out.println(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> History Extn: Start");
        historyList.forEach(System.out::println);

        // TODO: Modify as per the requirement
        if (entity instanceof Books) {
            booksService.writeHistory(entity, historyList);
        }

        System.out.println(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> History Extn: End");
    }
}
