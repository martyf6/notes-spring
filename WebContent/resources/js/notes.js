// keep track of changes to the note being edited
var noteChanged = false;

function isNoteChanged() {
    return noteChanged;
}

// update whether or not the note reflects changed data
function handleNoteChanged(changed) {
    
    // default changed value is 'true'
    changed = typeof changed !== 'undefined' ? changed : true;
    
    // save the initial values if the note is unchanged
    if (!changed) {
	var $noteTitle = $("#note-title");
	$noteTitle.data('initial-value', $noteTitle.val());
	var $noteText = $("#note-area");
	$noteText.data('initial-value', $noteText.val());
    }
    
    // update the state of the save button
    $("#save-button").attr("disabled", !changed);
    noteChanged = changed;
}

function getActiveNote() {
    return $("#note-names .notename.active");
}

function getNoteId($elt) {
    if ($elt && $elt.data('note-id'))
	return $elt.data('note-id');
    else 
	return "-1";
}

function getNoteById(noteId) {
    return $("#note-names").find('[data-note-id="' + noteId + '"]');
}

function getActiveNoteTitle() {
    return $("#note-title").val();
}

function getActiveNoteText() {
    return $("#note-area").val();
}

function getActiveNoteId() {
    return getNoteId(getActiveNote());
}

function deactivateSelection() {
    var $oldSelected = getActiveNote();
    if ($oldSelected) {
	$oldSelected.removeClass("active");
    }
}

function activateSelection(noteId) {
    deactivateSelection();
    // get the handle of the clicked element
    var $selectedNote = getNoteById(noteId);
    // set its class to 'active'
    if ($selectedNote)
	$selectedNote.addClass("active");
}

function alertMsg(msgClass, msg) {
    var noteMsg = '<div class="alert '
	    + msgClass
	    + ' alert-dismissable">'
	    + '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>'
	    + msg + '</div>';
    $("#note-msg").html(noteMsg);
}

function clearMsg() {
    $("#note-msg").html('');
}

function setNoteTitle(noteTitle) {
    $("#note-title").val(noteTitle);
}

function setNoteText(noteText) {
    $("#note-area").val(noteText);
}

function createNoteLIHtml(noteTitle, noteId) {
    var newNoteItem = '<li class="notename" title="'
	    + noteTitle
	    + '" data-note-id="'
	    + noteId
	    + '"><a href="#"><span class="glyphicon glyphicon-file"></span> '
	    + noteTitle
	    + '</a></li>';
    return newNoteItem;
}

function setDefaultSelection() {
    deactivateSelection();
    var defaultTitle = "";
    var defaultText = "Type your note here.";
    setNoteTitle(defaultTitle);
    setNoteText(defaultText);
    clearMsg();
    handleNoteChanged(false);
}

// display the note with id 'noteId' for editing
function selectNote(noteId) {
    if (!isNoteChanged()
	    || confirm("Are you sure you want to discard your changes?")) {

	clearMsg();
	activateSelection(noteId);
	
	// set note title field
	var $selectedNote = getActiveNote();
	var noteName = $selectedNote.attr("title");
	setNoteTitle(noteName);
	
	// set the note area text
	$("#note-area").val("Loading content...");
	var request = $.ajax({
	    type: "GET",
	    url: "get_note", 
	    data: { 
		note : noteId 
	    }
	});
	request.done(function(resp) {
	    var respJson = JSON.parse(resp);
	    
	    // check if the request succeeded
	    if (respJson.success) {
		// check if the title happened to have changed
		if (respJson.noteTitle != noteName) {
		    // update the note in the list
		    var newNoteItem = createNoteLIHtml(respJson.noteTitle, respJson.noteId);
		    $selectedNote.replaceWith(newNoteItem);
		    // new note in the list needs to be set to active
		    activateSelection(respJson.noteId);
		}
		// update the text area with the note contents
		setNoteText(respJson.noteEntry);
		// reset the editing defaults
		handleNoteChanged(false);
		$("#note-area").focus();
	    } else {
		setDefaultSelection();
		alertMsg('alert-danger', respJson.message);
	    }
	});
	request.fail(function(jqXHR, status){
	    alertMsg('alert-danger', 'Failed to retrieve note.');
	    console.log("Note retrieval failed with: " + status);
	});
    }
}

function saveSelection() {
    var $selected = getActiveNote();
    var newTitle = getActiveNoteTitle();
    var noteData = getActiveNoteText();
    
    if ($selected.length == 0) {
	
	// create a new note
	var request = $.ajax({
	    type: "POST",
	    url: "new_note", 
	    data: { 
		note : newTitle, 
		entry : noteData 
	    }
	});
	request.done(function(resp) {
	    var newNoteJson = JSON.parse(resp);
	    if (newNoteJson.success) {
		var newNoteItem = createNoteLIHtml(newNoteJson.noteTitle, newNoteJson.noteId);
		$("#note-names").prepend(newNoteItem);
		handleNoteChanged(false);
		selectNote(newNoteJson.noteId);
		alertMsg('alert-success', 'Note saved.');
	    } else
		alertMsg('alert-danger', newNoteJson.message);
	});
	request.fail(function(jqXHR, status){
	    alertMsg('alert-danger', 'Failed to save note.');
	    console.log("Note save failed with: " + status);
	});
	
    } else {
	var noteId = getActiveNoteId();
	var request = $.ajax({
	    type: "POST",
	    url: "save_note", 
	    data: {
		id : noteId,
		newTitle : newTitle,
		entry : noteData
	    }
	});
	request.done(function(resp) {
	    var respJson = JSON.parse(resp);
	    if (respJson.success) {
		// check if the title happened to have changed
		if (respJson.noteTitle != $selected.attr("title")) {
		    // update the note in the list
		    var newNoteItem = createNoteLIHtml(respJson.noteTitle, respJson.noteId);
		    $selected.replaceWith(newNoteItem);
		    // new note in the list needs to be set to active
		    activateSelection(respJson.noteId);
		}
		handleNoteChanged(false);
		alertMsg('alert-success', 'Note saved.');
	    } else alertMsg('alert-danger', respJson.message);
	});
	request.fail(function(jqXHR, status){
	    alertMsg('alert-danger', 'Failed to save note.');
	    console.log("Note save failed with: " + status);
	});
    }
}

// called when a note in the list is clicked
function openNote() {
    // get the handle of the clicked element
    var $selectedNote = ($(this)).parent();
    var id = getNoteId($selectedNote);
    selectNote(id);
}

function newNote() {
    var newNoteIndex = 1;
    var newNoteName = "New Note";
    while ($("#note-names li.notename[title='" + newNoteName + "']").length > 0) {
	newNoteIndex++;
	newNoteName = "New Note (" + newNoteIndex + ")";
    }
    var defaultNote = "Type your note here.";
    var request = $.ajax({
	type: "POST",
	url: "new_note", 
	data: {
	    note : newNoteName,
	    entry : defaultNote
	}
    });
    request.done(function(resp) {
	var newNoteJson = JSON.parse(resp);
	if (newNoteJson.success) {
	    var newNoteItem = createNoteLIHtml(newNoteJson.noteTitle, newNoteJson.noteId);
	    $("#note-names").prepend(newNoteItem);
	    selectNote(newNoteJson.noteId);
	} else alertMsg('alert-danger', newNoteJson.message);
    });
    request.fail(function(jqXHR, status){
	alertMsg('alert-danger', 'Failed to create new note.');
	console.log("Note creation failed with: " + status);
    });
}

function deleteNote(){
    var $selected = getActiveNote();
    if ($selected.length == 0) {
    	alert("No note selected.");
    } else {
	var noteId = getActiveNoteId();
	var noteName = getActiveNoteTitle();
	var response = confirm("Are you sure you want to delete note '" + noteName + "'?");
	if (response == true) {
	    var request = $.ajax({
		type: "POST",
		url: "delete_note", 
		data: {
		    note: noteId 
		}
	    });
	    request.done(function (resp) {
		var respJson = JSON.parse(resp);
		if (respJson.success) {
		    // var index = $('#note-names li').index($('.notename-selected'));
		    $selected.remove();
		    // var newSelected = $("#note-names li:nth-child(" + index + ")").attr("id");
		    // selectNote(newSelected);
		    setDefaultSelection();
		} else alertMsg('alert-danger', respJson.message);
	    });
	    request.fail(function(jqXHR, status){
		alertMsg('alert-danger', 'Failed to delete note.');
		console.log("Note deletion failed with: " + status);
	    });
	}
    }
}

function handleKey(e) {

    // catch a tab (key code is 9 in chrome)
    if (e.keyCode === 9) {
	// get caret position/selection
	var start = this.selectionStart;
	var end = this.selectionEnd;

	var newText = $(this).val().substring(0, start) + "\t"
		+ $(this).val().substring(end);

	// set text area value to: text before caret + tab + text after caret
	$(this).val(newText);

	// put caret at right position again
	this.selectionStart = this.selectionEnd = start + 1;

	// prevent the focus loss
	return false;

	// ctrl + s to save
    } else if (!(String.fromCharCode(e.which).toLowerCase() == 's' && e.ctrlKey)
	    && !(e.which == 19)) {
	return true;
    } else {
	saveSelection();
	event.preventDefault();
	return false;
    }
}

//check if the value of an element has changed
function checkChangedValue() {
    var elt = $(this);
    if (!isNoteChanged() || elt.val() != elt.data('initial-value')) {
	handleNoteChanged(true);
    }
}

function initPage() {
	
    $("#note-names").sortable({ 
	items: "> li.notename",
	update : function (event, ui) {
	    var neworder = new Array(); // []
	    $("#note-names li.notename").each(function(index) {
                //get the id
                var id  = getNoteId($(this));
                var item = new Object(); // or var map = {};
                item["id"] = id;
                item["location"] = index;
                //push the object into the array
                neworder.push(item);
            });
            var neworderStr = JSON.stringify(neworder);
            var request = $.ajax({
		type: "POST",
		url: "update_note_order", 
		data: {
		    neworder: neworderStr 
		}
	    });
            request.done(function(resp) {});
            request.fail(function(jqXHR, status){
        	alertMsg('alert-danger', 'Failed to update note ordering.');
        	console.log("Note ordering update failed with: " + status);
	    });
	}
    });
    
    $("#note-names").disableSelection( { items: "> li.notename" } );

    // opening notes
    $('#note-names').on('click','li.notename a', openNote);
	
    // saving notes
    $('#save-button').on('click', saveSelection);

    // new note
    $('#new-note').on('click', newNote);
    
    // deleting notes
    $('#delete-button').on('click', deleteNote);
	
    // catching special commands in the text area
    $("#note-area").keydown(handleKey);
	
    // handle checking if the note has been edited
    $("#note-area").keyup(checkChangedValue);
    $("#note-title").keyup(checkChangedValue);
	
    // set the default note initial values
    setDefaultSelection();
}

$(document).ready(initPage);