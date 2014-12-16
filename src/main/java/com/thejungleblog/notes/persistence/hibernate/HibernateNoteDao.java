package com.thejungleblog.notes.persistence.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.thejungleblog.notes.domain.NoteEntity;
import com.thejungleblog.notes.persistence.INoteDao;

@Repository
public class HibernateNoteDao implements INoteDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private Session currentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	@Transactional
	public void addNote(NoteEntity noteEntity) {
		currentSession().save(noteEntity);
	}

	@Override
	@Transactional
	public NoteEntity getNoteById(int id) {
		try {
			return (NoteEntity) currentSession().get(NoteEntity.class, id);
		} catch (ObjectNotFoundException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<NoteEntity> getNotesByAuthor(int author) {
		Criteria criteria = currentSession().createCriteria(NoteEntity.class);
		return criteria.add(Restrictions.eq("author", author))
				       .addOrder(Order.asc("location")) // order notes by location
				       .addOrder(Order.desc("id")) // when two notes conflict (location 0), put newer notes on top
				       .list();
	}

	@Override
	@Transactional
	public void saveNote(NoteEntity noteEntity) {
		currentSession().update(noteEntity);
	}

	@Override
	@Transactional
	public void deleteNote(NoteEntity noteEntity) {
		currentSession().delete(noteEntity);
	}
}
