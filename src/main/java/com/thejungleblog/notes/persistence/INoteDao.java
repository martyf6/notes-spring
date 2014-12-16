package com.thejungleblog.notes.persistence;

import java.util.List;

import com.thejungleblog.notes.domain.NoteEntity;

public interface INoteDao {

	public void addNote(NoteEntity noteEntity);

	/**
	 * Get note by it's associated id
	 * @param id
	 * @return {@link NoteEntity}, null if one isn't found
	 */
	public NoteEntity getNoteById(int id);

	public List<NoteEntity> getNotesByAuthor(int author);

	public void saveNote(NoteEntity noteEntity);

	public void deleteNote(NoteEntity noteEntity);
}
