package ece1779.appengine.datastore;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Entity(name = "Book")
public class Book {
    @Id
    Key isbn;
    //private String isbn;

    private String title;
    private String author;
    private int copyrightYear;
    private Date authorBirthdate;

    public Book(String isbn) {
        this.isbn = KeyFactory.createKey("Book",isbn);
    }

    public String getIsbn() {
        return KeyFactory.keyToString(isbn);
    }

    public void setIsbn(com.google.appengine.api.datastore.Key isbn) {
		this.isbn = isbn;
	}

	public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    public String getAuthor() {
        return author;
    }

    public void setCopyrightYear(int copyrightYear) {
        this.copyrightYear = copyrightYear;
    }
    public int getCopyrightYear() {
        return copyrightYear;
    }

    public void setAuthorBirthdate(Date authorBirthdate) {
        this.authorBirthdate = authorBirthdate;
    }
    public Date getAuthorBirthdate() {
        return authorBirthdate;
    }
}
