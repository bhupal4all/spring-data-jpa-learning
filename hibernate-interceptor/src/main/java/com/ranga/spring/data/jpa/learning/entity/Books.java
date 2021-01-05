package com.ranga.spring.data.jpa.learning.entity;

import com.ranga.spring.data.jpa.learning.annotation.*;
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
@AuditHistory(AuditHistoryType.ALL)
public class Books {

    @Id
    @GeneratedValue
    Integer id;

    String name;

    String author;

    Integer year;

    @AuditHistoryFieldExtensionHook
    String customField1;

    @CreatedDate
    Date createdOn;

    @AuditHistoryIgnore
    @LastModifiedDate
    Date modifiedOn;

    public Integer getId() {
        return id;
    }

    @AuditHistoryIdentifier
    public String getName() {
        return name;
    }
}