package com.ranga.spring.data.jpa.learning.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BOOKS_UPDATE_HISTORY")
@EntityListeners(AuditingEntityListener.class)
@Data
@ToString(includeFieldNames = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@RequiredArgsConstructor
@AllArgsConstructor
public class BooksUpdateHistory {
    @Id
    @GeneratedValue
    Integer id;

    @NonNull
    Integer bookId;

    @NonNull
    String history;

    @CreatedDate
	Date createdOn;

}
