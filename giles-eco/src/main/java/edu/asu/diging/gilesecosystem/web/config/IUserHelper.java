package edu.asu.diging.gilesecosystem.web.config;

import org.springframework.social.connect.Connection;

import edu.asu.diging.gilesecosystem.web.users.User;

public interface IUserHelper {

    public abstract User createUser(Connection<?> connection);

}