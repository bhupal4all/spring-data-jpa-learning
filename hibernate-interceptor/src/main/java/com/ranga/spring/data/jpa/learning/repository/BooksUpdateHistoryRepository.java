package com.ranga.spring.data.jpa.learning.repository;

import com.ranga.spring.data.jpa.learning.entity.BooksUpdateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BooksUpdateHistoryRepository extends JpaRepository<BooksUpdateHistory, Integer> {

}
