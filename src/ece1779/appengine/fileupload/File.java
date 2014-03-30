package ece1779.appengine.fileupload;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Blob;


@Entity(name = "File")
public class File {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)    
    private Key id;
	
	private String name;
	private Blob data;
	
	public Key getId() {
		return id;
	}
	public void setId(Key id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte[] getData() {
		return data.getBytes();
	}
	public void setData(byte[] data) {
		this.data = new Blob(data);
	}
}
