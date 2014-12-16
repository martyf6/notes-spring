package com.thejungleblog.notes.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "NOTE")
public class NoteEntity {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
    @Column(name = "ID", unique = true, nullable = false)
    private Integer id;
    
	@Column(name = "TITLE")
	private String title;
	
	@Column(name = "ENTRY", columnDefinition = "TEXT")
	private String entry;
	
	@Column(name = "AUTHOR", nullable = false)
	private Integer author;
	
	@Column(name = "LOCATION")
	private Integer location;
	
	public int getId() {
    	return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
    	return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getEntry() {
    	return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }
    
    public int getAuthor() {
    	return author;
    }

    public void setAuthor(int author) {
        this.author = author;
    }
    
    public int getLocation() {
    	return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }
    
    @Override
    public String toString(){
    	String truncatedEntry = (entry.length() > 35) ? entry.substring(0, 35) + "..." : entry;
    	return "Note {id: " + id + 
    			", title: " + title + 
    			", entry: " + truncatedEntry + 
    			", author: " + author + 
    			", location: " + location + "}";
    }
}
