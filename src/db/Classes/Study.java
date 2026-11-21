package db.Classes;

import java.sql.Date;



/**
 *
 * @author joheras
 */
public class Study {
    
    private int id;
    private String name;
    private Date date;
    private String path;
    private String comments;
    private User user;

    public Study(int id, String name, Date date, String path, String comments, User user) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.path = path;
        this.comments = comments;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Study{" + "name=" + name + ", date=" + date + '}';
    }
    
    
    
    
    

}
