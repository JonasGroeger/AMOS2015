package de.fau.amos4.persistance;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import de.fau.amos4.domain.Employee;

public class HibernateUtil
{

    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new AnnotationConfiguration()
                    .addAnnotatedClass(Employee.class)
                    .configure()
                    .buildSessionFactory();
        } catch (Throwable ex) {
            // Log exception!
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession()
            throws HibernateException
    {
        return sessionFactory.openSession();
    }
}