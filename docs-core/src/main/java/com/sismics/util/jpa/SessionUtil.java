package com.sismics.util.jpa;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;

/**
 * Hibernate session utilities.
 * 
 * @author jtremeaux
 */
public final class SessionUtil {
    /**
     * Private constructor.
     */
    private SessionUtil() {
    }

    /**
     * Returns an instance of the current session.
     * 
     * @return Instance of the current session
     */
    public static Session getCurrentSession() {
        SessionFactory sessionFactory = ((HibernateEntityManagerFactory) EMF.get()).getSessionFactory();
        return sessionFactory.getCurrentSession();
    }
}