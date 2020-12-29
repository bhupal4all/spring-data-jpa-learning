package com.ranga.spring.data.jpa.learning.entity;

import com.ranga.spring.data.jpa.learning.annotation.AuditHistory;
import com.ranga.spring.data.jpa.learning.annotation.AuditHistoryIdentifier;
import com.ranga.spring.data.jpa.learning.annotation.AuditHistoryIgnore;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BOOKS")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = false, includeFieldNames = true)
@AuditHistory
public class Books {

    @Id
    @GeneratedValue
    Integer id;

    @AuditHistoryIdentifier
    String name;

    String author;

    Integer year;

    @CreatedDate
    Date createdOn;

    @AuditHistoryIgnore
    @LastModifiedDate
    Date modifiedOn;
}