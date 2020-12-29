package com.ranga.spring.data.jpa.learning.service;


import com.ranga.spring.data.jpa.learning.entity.Books;
import com.ranga.spring.data.jpa.learning.entity.BooksUpdateHistory;
import com.ranga.spring.data.jpa.learning.repository.BooksRepository;
import com.ranga.spring.data.jpa.learning.repository.BooksUpdateHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BooksService {

	@Autowired
	BooksRepository booksRepository;

	@Autowired
	BooksUpdateHistoryRepository historyRepository;

	public List<Books> getAll() {
		return booksRepository.findAll();
	}

	public Optional<Books> getById(Integer id) {
		return booksRepository.findById(id);
	}

	public Books save(Books book) {
		return booksRepository.save(book);
	}

	public void deleteById(Integer id) {
		booksRepository.deleteById(id);
	}

	public List<BooksUpdateHistory> getBooksHistory() {
		return historyRepository.findAll();
	}

	public BooksUpdateHistory createHistory(Integer bookId, String history) {
		return historyRepository.save(new BooksUpdateHistory(bookId, history));
	}
}