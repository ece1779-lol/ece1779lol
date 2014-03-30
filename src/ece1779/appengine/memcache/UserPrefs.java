package ece1779.appengine.memcache;

import java.io.Serializable;
import java.util.logging.*;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Transient;
import com.google.appengine.api.users.User;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import ece1779.appengine.EMF;

  
@SuppressWarnings("serial")
@Entity(name = "UserPrefs")
public class UserPrefs implements Serializable {
    @Transient
    private static Logger logger = Logger.getLogger(UserPrefs.class.getName());

    @Id
    private String userId;

    private int tzOffset;
    
    
    public UserPrefs(User user) {
        this.userId = user.getUserId();
    }

    public String getUserId() {
        return userId;
    }

    public int getTzOffset() {
        return tzOffset;
    }

    public void setTzOffset(int tzOffset) {
        this.tzOffset = tzOffset;
    }

    
    @SuppressWarnings("unchecked")
    public static UserPrefs getPrefsForUser(User user) {
        UserPrefs userPrefs = null;

        String cacheKey = getCacheKeyForUser(user);

        try {
            MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
            if (memcache.contains(cacheKey)) {
                logger.warning("CACHE HIT: " + cacheKey);
                userPrefs = (UserPrefs) memcache.get(cacheKey);
                return userPrefs;
            }
            logger.warning("CACHE MISS: " + cacheKey);
            // If the UserPrefs object isn't in memcache,
            // fall through to the datastore.
        } catch (MemcacheServiceException e) {
            // If there is a problem with the cache,
            // fall through to the datastore.
        }

        EntityManager em = EMF.get().createEntityManager();
        try {
            userPrefs = em.find(UserPrefs.class, user.getUserId());
            if (userPrefs == null) {
                userPrefs = new UserPrefs(user);
                userPrefs.save();
            } else {
                userPrefs.cacheSet();
            }
        } finally {
            em.close();
        }

        return userPrefs;
    }

    public static String getCacheKeyForUser(User user) {
        return "UserPrefs:" + user.getUserId();
    }

    public String getCacheKey() {
        return "UserPrefs:" + getUserId();
    }

    public void save() {
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.merge(this);
            cacheDelete();
        } finally {
            em.close();
        }
    }

    public void cacheSet() {
        try {
            MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
            String cacheKey = getCacheKey();
            logger.warning("CACHE SET: " + cacheKey);
            memcache.put(cacheKey, this);
        } catch (MemcacheServiceException e) {
            // Ignore cache problems, nothing we can do.
        }
    }
    
    public void cacheDelete() {
        try {
            MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
            String cacheKey = getCacheKey();
            logger.warning("CACHE DELETE: " + cacheKey);
            memcache.delete(cacheKey);
        } catch (MemcacheServiceException e) {
            // Ignore cache problems, nothing we can do.
        }
    }

    
}
