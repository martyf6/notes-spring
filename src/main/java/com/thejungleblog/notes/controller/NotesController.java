package com.thejungleblog.notes.controller;

import java.security.Principal;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thejungleblog.notes.domain.NoteEntity;
import com.thejungleblog.notes.persistence.INoteDao;
import com.thejungleblog.notes.persistence.IUserDao;

/**
 * Controller used for handling application specific requests
 * 
 * @author marty
 *
 */
@Controller
public class NotesController {
	
	private static Logger logger = Logger.getLogger(NotesController.class);
	
	@Autowired
	private INoteDao noteDao;
	
	@Autowired
	private IUserDao userDao;
	
	@RequestMapping(value = {"/", "/home"}, method = RequestMethod.GET)
	public String getHomePage(Model model, Principal principal) {
		
		// get the active user
		String username = principal.getName();
		int userId = userDao.getUserByUsername(username).getId();
		
		// get all the user's notes
		List<NoteEntity> notes = noteDao.getNotesByAuthor(userId);
		logger.info("Home with user '" + username + "'");
		
		model.addAttribute("username", username);
		model.addAttribute("notes", notes);
		
		return "home";
	}
	
	@RequestMapping(value = "/get_note", method = RequestMethod.GET)
	public @ResponseBody String getNote(@RequestParam("note") int noteId, Model model, Principal principal) {

		logger.info("Getting note with id: " + noteId);
		
		// get the active user
		String username = principal.getName();
		int userId = userDao.getUserByUsername(username).getId();
		
		// attempt to get the specific note requested
		try {
			NoteEntity note = noteDao.getNoteById(noteId);
			if (note.getAuthor() == userId) {
				JSONObject noteJson = convertNoteToJson(note);
				createJsonResponse(true, "", noteJson);
				return noteJson.toString();
			} else {
				// the active user is not the author of this note
				logger.warn("User '" + username + "' attempting to access note " + noteId + " without ownership.");
				return createJsonResponse(false, "This note does not belong to you.").toString();
			}
		} catch (NullPointerException e) {
			// no note by this id exists
			logger.warn("Failing to get note with id: " + noteId + ". Note does not exist.");
			return createJsonResponse(false, "Note not found.").toString();
		}
	}
	
	@RequestMapping(value = "/new_note", method = RequestMethod.POST)
	public @ResponseBody String newNote(@RequestParam("note") String title, 
			@RequestParam("entry") String entry, 
			Model model, Principal principal) {
		
		// get the active user
		String username = principal.getName();
		int userId = userDao.getUserByUsername(username).getId();
		
		// make sure the new note name isn't already being used
		// for another note by this user
		List<NoteEntity> notes = noteDao.getNotesByAuthor(userId);
		
		for (NoteEntity note : notes) {
			if (note.getTitle().equals(title)) {
				logger.info("New note creation failed due to note name conflict.");
				return createJsonResponse(false, "A note with this title already exists.").toString();
			}
		}
		
		// create the new note
		NoteEntity newNote = new NoteEntity();
		newNote.setAuthor(userId);
		newNote.setEntry(entry);
		newNote.setTitle(title);
		noteDao.addNote(newNote);
		
		// return the new note's details
		logger.info("New note '" + title + "' created for user '" + username + "'");
		JSONObject noteJson = convertNoteToJson(newNote);
		createJsonResponse(true, "", noteJson);
		return noteJson.toString();
	}
	
	@RequestMapping(value = "/save_note", method = RequestMethod.POST)
	public @ResponseBody String saveNote(@RequestParam("id") int noteId,
			@RequestParam("newTitle") String newTitle,
			@RequestParam("entry") String entry,
			Model model, Principal principal) {
		
		// get the active user
		String username = principal.getName();
		int userId = userDao.getUserByUsername(username).getId();
		
		// what to do if the note doesn't exist...
		NoteEntity note = noteDao.getNoteById(noteId);
		
		// check if the note doesn't exist or doesn't belong to the user
		if (note == null || note.getAuthor() != userId) {
			
			// could return a failure
			//response.put("success", false);
			
			// create a new note
			
			// make sure the new note name isn't already taken
			List<NoteEntity> notes = noteDao.getNotesByAuthor(userId);
			for (NoteEntity userNote : notes) {
				if (userNote.getTitle().equals(newTitle)) {
					logger.info("New note creation failed due to note name conflict.");
					return createJsonResponse(false, "A note with this title already exists.").toString();
				}
			}
						
			// new note title isn't taken, so go ahead with the update
			NoteEntity newNote = new NoteEntity();
			newNote.setAuthor(userId);
			newNote.setEntry(entry);
			newNote.setTitle(newTitle);
			noteDao.addNote(newNote);
			
			// return success
			logger.info("New note '" + newTitle + "' created for user '" + username + "'");
			JSONObject noteJson = convertNoteToJson(newNote);
			createJsonResponse(true, "", noteJson);
			return noteJson.toString();
		}
		
		// check if the title has been updated
		if (note.getTitle().equals(newTitle)) {
			
			// update the existing note
			note.setEntry(entry);
			noteDao.saveNote(note);
			
			// return success
			logger.info("Note '" + newTitle + "' updated for user '" + username + "'");
			JSONObject noteJson = convertNoteToJson(note);
			createJsonResponse(true, "", noteJson);
			return noteJson.toString();
			
		} else {
			
			// make sure the new note name isn't already taken
			List<NoteEntity> notes = noteDao.getNotesByAuthor(userId);
			for (NoteEntity userNote : notes) {
				if (userNote.getTitle().equals(newTitle)) {
					logger.info("Note update for note '" + newTitle + "' failed due to note name conflict.");
					return createJsonResponse(false, "A note with this title already exists.").toString();
				}
			}
			
			// new note title isn't taken, so go ahead with the update
			note.setTitle(newTitle);
			note.setEntry(entry);
			noteDao.saveNote(note);
			
			// return success
			logger.info("Note '" + newTitle + "' updated for user '" + username + "'");
			JSONObject noteJson = convertNoteToJson(note);
			createJsonResponse(true, "", noteJson);
			return noteJson.toString();
		}
	}
	
	@RequestMapping(value = "/delete_note", method = RequestMethod.POST)
	public @ResponseBody String deleteNote(@RequestParam("note") int noteId, Model model, Principal principal) {
		
		// get the active user
		String username = principal.getName();
		int userId = userDao.getUserByUsername(username).getId();
		
		NoteEntity note = noteDao.getNoteById(noteId);
		
		// check if the requested note does not exist
		if (note == null) {
			logger.warn("Failing to delete note with id: " + noteId + ". Note does not exist.");
			return createJsonResponse(false, "Note not found.").toString();
		}
			
		// make sure the user owns the note
		if (note.getAuthor() != userId) {
			logger.warn("User '" + username + "' attempting to delete note " + noteId + " without ownership.");
			return createJsonResponse(false, "This note does not belong to you.").toString();
		}
			
		noteDao.deleteNote(note);
		
		// return success
		logger.info("Deleted note '" + noteId + "' for user '" + username + "'");
		return createJsonResponse(true, "").toString();
	}
	
	/**
	 * Updates the ordering of a user's notes
	 * 
	 * @param newOrder - a JSON list of note entries, where each element contains a note id and list location
	 * @param model
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "/update_note_order", method = RequestMethod.POST)
	public @ResponseBody String updateNoteOrder(@RequestParam("neworder") String newOrder, Model model, Principal principal) {
		
		// get the active user
		String username = principal.getName();
		int userId = userDao.getUserByUsername(username).getId();
		
		// parse the JSON representing the new note ordering
		JSONArray notes = new JSONArray(newOrder);
		for (int i = 0; i < notes.length(); i++) {
			try {
				
				// get the note id and location
				JSONObject locUpdate = notes.getJSONObject(i);
				int noteId = Integer.parseInt(locUpdate.get("id").toString());
				int noteLocation = Integer.parseInt(locUpdate.get("location").toString());
				
				// get the associated note by id
				NoteEntity note = noteDao.getNoteById(noteId);
				
				// make sure the user owns the note
				if (note.getAuthor() != userId) {
					return createJsonResponse(true, "").toString();
				}
				
				// update the note location
				note.setLocation(noteLocation);
				noteDao.saveNote(note);
			} catch (JSONException e) {
				logger.error(e);
				return createJsonResponse(false, "Note order udpate failed.").toString();
			}
		}
		
		// return success
		return createJsonResponse(true, "").toString();
	}
	
	/**
	 * Creates a new JSONObject encoding note data
	 * @param note
	 * @return
	 */
	private static JSONObject convertNoteToJson(NoteEntity note) {
		JSONObject noteJson = new JSONObject();
		
		noteJson.put("noteId", note.getId());
		noteJson.put("noteTitle", note.getTitle());
		noteJson.put("noteEntry", note.getEntry());
		
		return noteJson;
	}
	
	/**
	 * Creates a JSONObject response with success and message attributes
	 * @param success response success boolean
	 * @param message response message string
	 * @return JSONObject containing response attributes
	 */
	private static JSONObject createJsonResponse(boolean success, String message) {
		JSONObject response = new JSONObject();
		createJsonResponse(success, message, response);
		return response;
	}
	
	/**
	 * Modifies the response param to include success and message attributes
	 * @param success response success boolean
	 * @param message response message string
	 * @param response JSONObject modified to include response attributes
	 */
	private static void createJsonResponse(boolean success, String message, JSONObject response) {
		response.put("success", success);
		response.put("message", message);
	}
}