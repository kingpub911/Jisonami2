package org.jisonami.dao.impl.hibernate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.jisonami.dao.IUserDao;
import org.jisonami.entity.User;
import org.jisonami.util.DBUtils;

public class UserDao implements IUserDao {

	@Override
	public boolean insert(User user) throws SQLException {
		Session session = initSession(true);
		session.save(user);
		closeSession(session, true);
		return true;
	}

	@Override
	public boolean delete(String id) throws SQLException {
		Session session = initSession(true);
		User user = new User();
		user.setId(id);
		session.delete(user);
		closeSession(session, true);
		return true;
	}

	@Override
	public boolean update(User user) throws SQLException {
		Session session = initSession(true);
		session.update(user);
		closeSession(session, true);
		return true;
	}

	@Override
	public User select(String id) throws SQLException {
		Session session = initSession(false);
		User user = session.get(User.class, id);
		closeSession(session, false);
		return user;
	}

	@Override
	public List<User> select() throws SQLException {
		Session session = initSession(false);
		String hql = "from User";
		@SuppressWarnings("unchecked")
		List<User> list = session.createQuery(hql).list();
		closeSession(session, false);
		return list;
	}

	public <T> List<T> query(String hql, Map<String, Object> queryParam) {
		Session session = initSession(false);
		Query query = session.createQuery(hql);
		for (String key : queryParam.keySet()) {
			query.setParameter(key, queryParam.get(key));
		}
		@SuppressWarnings("unchecked")
		List<T> list = query.list();
		closeSession(session, false);
		return list;
	}
	
	@Override
	public boolean selectByNameAndPassword(User user) throws SQLException {
		String hql = "from User as u where u.name = :name and u.password = :password";
		Map<String, Object> queryParam = new HashMap<String, Object>();
		queryParam.put("name", user.getName());
		queryParam.put("password", user.getPassword());
		List<User> users = this.query(hql, queryParam);
		if(users!=null && users.size()>0){
			return true;
		}
		return false;
	}

	@Override
	public boolean selectByName(String name) throws SQLException {
		String hql = "from User as u where u.name = :name";
		Map<String, Object> queryParam = new HashMap<String, Object>();
		queryParam.put("name", name);
		List<User> users = this.query(hql, queryParam);
		if(users!=null && users.size()>0){
			return true;
		}
		return false;
	}

	private Session initSession(boolean transactionFlag) {
		Configuration configuration = new Configuration().
				addProperties(getDBProperties()).
				addAnnotatedClass(User.class).
				configure(); 
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()  
		         .applySettings(configuration.getProperties()).build();  
		SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry); 
		Session session = sessionFactory.openSession();
	
		if (transactionFlag) {
			session.beginTransaction();
		}
		return session;
	}

	private boolean closeSession(Session session, boolean transactionFlag) {
		if (transactionFlag) {
			Transaction transaction = session.getTransaction();
			transaction.commit();
		}
		SessionFactory sessionFactory = session.getSessionFactory();
		session.close();
		sessionFactory.close();
		return true;
	}
	
	private Properties getDBProperties(){
		Properties properties = new Properties();
		properties.setProperty("hibernate.connection.driver_class", DBUtils.getDriver());
		properties.setProperty("hibernate.connection.url", DBUtils.getUrl());
		properties.setProperty("hibernate.connection.username", DBUtils.getUserName());
		properties.setProperty("hibernate.connection.password", DBUtils.getPassword());
		return properties;
	}
}